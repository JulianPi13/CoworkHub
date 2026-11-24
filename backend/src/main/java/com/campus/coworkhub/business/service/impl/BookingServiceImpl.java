package com.campus.coworkhub.business.service.impl;

import com.campus.coworkhub.business.booking.BookingCostCalculator;
import com.campus.coworkhub.business.booking.BookingValidator;
import com.campus.coworkhub.business.booking.CancellationPolicy;
import com.campus.coworkhub.business.dto.booking.ReservaRequest;
import com.campus.coworkhub.business.dto.booking.ReservaResponse;
import com.campus.coworkhub.business.dto.booking.ServicioReservaRequest;
import com.campus.coworkhub.business.exception.BusinessRuleException;
import com.campus.coworkhub.business.exception.InvalidRequestException;
import com.campus.coworkhub.business.exception.ResourceNotFoundException;
import com.campus.coworkhub.business.mapper.DtoMapper;
import com.campus.coworkhub.business.service.BookingService;
import com.campus.coworkhub.persistence.entity.*;
import com.campus.coworkhub.persistence.repository.AdditionalServiceRepository;
import com.campus.coworkhub.persistence.repository.BookingRepository;
import com.campus.coworkhub.persistence.repository.MemberRepository;
import com.campus.coworkhub.persistence.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.campus.coworkhub.business.exception.BusinessRules.*;

@Service
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private static final List<BookingStatus> ACTIVE_STATUSES = List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;
    private final AdditionalServiceRepository serviceRepository;
    private final BookingValidator validator;
    private final BookingCostCalculator costCalculator;
    private final CancellationPolicy cancellationPolicy;

    public BookingServiceImpl(BookingRepository bookingRepository, RoomRepository roomRepository,
                              MemberRepository memberRepository, AdditionalServiceRepository serviceRepository,
                              BookingValidator validator, BookingCostCalculator costCalculator,
                              CancellationPolicy cancellationPolicy) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.memberRepository = memberRepository;
        this.serviceRepository = serviceRepository;
        this.validator = validator;
        this.costCalculator = costCalculator;
        this.cancellationPolicy = cancellationPolicy;
    }

    /**
     * RF-04. Crea la reserva aplicando RN-01 a RN-07.
     * <p>
     * Concurrencia (RN-01): revisar el cruce y después guardar no alcanza si llegan dos solicitudes a la vez.
     * Por eso la transacción bloquea la fila de la sala con SELECT ... FOR UPDATE: la segunda solicitud espera
     * a que la primera confirme, y como el nivel de aislamiento es READ_COMMITTED, al continuar ve la reserva
     * recién guardada y recibe 409.
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ReservaResponse create(ReservaRequest request) {
        LocalDateTime start = request.inicio();
        LocalDateTime end = request.fin();
        validator.validateInterval(start, end);
        validator.validateDuration(start, end);                                   // RN-02

        Room room = roomRepository.findByIdForUpdate(request.salaId())           // bloqueo de la sala
                .orElseThrow(() -> new ResourceNotFoundException("Sala", request.salaId()));
        Member member = memberRepository.findWithDetailsById(request.miembroId())
                .orElseThrow(() -> new ResourceNotFoundException("Miembro", request.miembroId()));

        validator.validateSchedule(room.getLocation(), start, end);               // RN-03
        validator.validateRoom(room, request.asistentes());                        // RN-04
        validator.validateMember(member);                                         // RN-05

        long active = bookingRepository.countByMemberIdAndStatusInAndStartTimeAfter(
                member.getId(), ACTIVE_STATUSES, validator.now());
        if (active >= member.getPlan().getMaxActiveBookings()) {                   // RN-06
            throw new BusinessRuleException(RN_06, "El miembro ya tiene " + active
                    + " reservas activas futuras, el máximo de su plan es " + member.getPlan().getMaxActiveBookings());
        }

        if (bookingRepository.existsOverlap(room.getId(), start, end, ACTIVE_STATUSES)) {  // RN-01
            throw new BusinessRuleException(RN_01, "La sala ya está reservada en ese horario");
        }

        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setMember(member);
        booking.setStartTime(start);
        booking.setEndTime(end);
        booking.setAttendees(request.asistentes());
        booking.setStatus(BookingStatus.PENDING);
        booking.setRoomCost(costCalculator.roomCost(member, room, start, end, null));   // RN-07

        mergeServiceRequests(Optional.ofNullable(request.servicios()).orElseGet(List::of))
                .forEach((serviceId, quantity) -> addOrIncrease(booking, serviceId, quantity));
        BookingCostCalculator.refreshTotals(booking);

        return DtoMapper.toResponse(bookingRepository.save(booking));
    }

    @Override
    public ReservaResponse findById(Long id) {
        return DtoMapper.toResponse(getOrThrow(id));
    }

    /** RF-05: listar por miembro, por sala y fecha, o por estado. Sin filtros devuelve todas. */
    @Override
    public List<ReservaResponse> search(Long memberId, Long roomId, LocalDate date, BookingStatus status) {
        List<Booking> result;
        if (memberId != null) {
            result = bookingRepository.findByMemberIdOrderByStartTimeDesc(memberId);
        } else if (roomId != null || date != null) {
            if (roomId == null || date == null) {
                throw new InvalidRequestException("Para filtrar por sala hay que enviar salaId y fecha juntos");
            }
            result = bookingRepository.findByRoomIdAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTime(
                    roomId, date.atStartOfDay(), date.plusDays(1).atStartOfDay());
        } else if (status != null) {
            result = bookingRepository.findByStatusOrderByStartTimeDesc(status);
        } else {
            result = bookingRepository.findAllByOrderByStartTimeDesc();
        }
        return result.stream()
                .filter(b -> status == null || b.getStatus() == status)
                .map(DtoMapper::toResponse)
                .toList();
    }

    /** RF-06: agrega un servicio (o suma cantidad si ya estaba) y recalcula el costo. Solo PENDING (RN-10). */
    @Override
    @Transactional
    public ReservaResponse addService(Long bookingId, ServicioReservaRequest request) {
        Booking booking = getOrThrow(bookingId);
        validator.validateServicesEditable(booking);
        addOrIncrease(booking, request.servicioId(), request.cantidad());
        BookingCostCalculator.refreshTotals(booking);
        return DtoMapper.toResponse(booking);
    }

    /** RF-06: quita un servicio y recalcula el costo. Solo PENDING (RN-10). */
    @Override
    @Transactional
    public ReservaResponse removeService(Long bookingId, Long serviceId) {
        Booking booking = getOrThrow(bookingId);
        validator.validateServicesEditable(booking);
        BookingItem item = booking.findItemByServiceId(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio de la reserva", serviceId));
        booking.removeItem(item); // orphanRemoval elimina el detalle
        BookingCostCalculator.refreshTotals(booking);
        return DtoMapper.toResponse(booking);
    }

    /** RF-07: PENDING → CONFIRMED. */
    @Override
    @Transactional
    public ReservaResponse confirm(Long id) {
        return transition(id, BookingStatus.CONFIRMED);
    }

    /** RF-09: CONFIRMED → COMPLETED. */
    @Override
    @Transactional
    public ReservaResponse complete(Long id) {
        return transition(id, BookingStatus.COMPLETED);
    }

    /** RF-08: cancela aplicando RN-08 y devuelve el cargo en cargoCancelacion. */
    @Override
    @Transactional
    public ReservaResponse cancel(Long id) {
        Booking booking = getOrThrow(id);
        validator.validateTransition(booking.getStatus(), BookingStatus.CANCELLED);   // RN-09
        CancellationPolicy.Outcome outcome = cancellationPolicy.evaluate(booking);   // RN-08
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationFee(outcome.fee());
        booking.setHoursRefunded(outcome.hoursRefunded());
        return DtoMapper.toResponse(booking);
    }

    @Override
    public Optional<Long> findOwnerMemberId(Long bookingId) {
        return bookingRepository.findMemberIdById(bookingId);
    }

    // ---------------------------------------------------------------------------------

    private ReservaResponse transition(Long id, BookingStatus target) {
        Booking booking = getOrThrow(id);
        validator.validateTransition(booking.getStatus(), target);
        booking.setStatus(target);
        return DtoMapper.toResponse(booking);
    }

    private void addOrIncrease(Booking booking, Long serviceId, int quantity) {
        AdditionalService service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio adicional", serviceId));
        booking.findItemByServiceId(serviceId).ifPresentOrElse(
                item -> item.setQuantity(item.getQuantity() + quantity),
                () -> {
                    BookingItem item = new BookingItem();
                    item.setService(service);
                    item.setQuantity(quantity);
                    item.setAppliedUnitPrice(service.getUnitPrice()); // precio vigente al agregarlo (RN-07)
                    booking.addItem(item);
                });
    }

    /** Si el mismo servicio viene repetido en la solicitud, se suman las cantidades. */
    private static Map<Long, Integer> mergeServiceRequests(List<ServicioReservaRequest> requests) {
        return requests.stream().collect(Collectors.toMap(
                ServicioReservaRequest::servicioId, ServicioReservaRequest::cantidad, Integer::sum, LinkedHashMap::new));
    }

    private Booking getOrThrow(Long id) {
        return bookingRepository.findWithDetailsById(id).orElseThrow(() -> new ResourceNotFoundException("Reserva", id));
    }
}
