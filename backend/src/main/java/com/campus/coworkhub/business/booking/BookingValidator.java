package com.campus.coworkhub.business.booking;

import com.campus.coworkhub.business.exception.BusinessRuleException;
import com.campus.coworkhub.business.exception.InvalidRequestException;
import com.campus.coworkhub.persistence.entity.*;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static com.campus.coworkhub.business.exception.BusinessRules.*;

/**
 * Validaciones de una reserva que no dependen de la base de datos (RN-02, RN-03, RN-04, RN-05, RN-09, RN-10).
 * Recibe el {@link Clock} inyectado, así las pruebas fijan la hora actual.
 */
@Component
public class BookingValidator {

    public static final int MIN_MINUTES = 30;
    public static final int MAX_MINUTES = 8 * 60;
    public static final int STEP_MINUTES = 30;
    public static final Duration MIN_ADVANCE = Duration.ofHours(1);

    private final Clock clock;

    public BookingValidator(Clock clock) {
        this.clock = clock;
    }

    public LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    /** Formato básico: el fin debe ser posterior al inicio (400). */
    public void validateInterval(LocalDateTime start, LocalDateTime end) {
        if (!end.isAfter(start)) {
            throw new InvalidRequestException("La fecha de fin debe ser posterior a la de inicio");
        }
    }

    /** RN-02: entre 30 min y 8 h, en múltiplos de 30 min, y el mismo día. */
    public void validateDuration(LocalDateTime start, LocalDateTime end) {
        long minutes = Duration.between(start, end).toMinutes();
        if (!start.toLocalDate().equals(end.toLocalDate())) {
            throw new BusinessRuleException(RN_02, "La reserva debe empezar y terminar el mismo día");
        }
        if (minutes < MIN_MINUTES || minutes > MAX_MINUTES || minutes % STEP_MINUTES != 0) {
            throw new BusinessRuleException(RN_02,
                    "La reserva debe durar entre 30 min y 8 h, en múltiplos de 30 min (duración pedida: " + minutes + " min)");
        }
    }

    /** RN-03: dentro del horario de la sede, futura y con al menos 1 h de anticipación. */
    public void validateSchedule(Location location, LocalDateTime start, LocalDateTime end) {
        LocalTime opening = location.getOpeningTime();
        LocalTime closing = location.getClosingTime();
        if (start.toLocalTime().isBefore(opening) || end.toLocalTime().isAfter(closing)) {
            throw new BusinessRuleException(RN_03,
                    "La reserva debe estar dentro del horario de la sede (" + opening + " a " + closing + ")");
        }
        if (start.isBefore(now().plus(MIN_ADVANCE))) {
            throw new BusinessRuleException(RN_03, "La reserva debe ser futura y hacerse con al menos 1 h de anticipación");
        }
    }

    /** RN-04: la sala debe estar activa y los asistentes no pueden superar su capacidad. */
    public void validateRoom(Room room, int attendees) {
        if (!room.isActive()) {
            throw new BusinessRuleException(RN_04, "La sala '" + room.getName() + "' no está activa");
        }
        if (attendees > room.getCapacity()) {
            throw new BusinessRuleException(RN_04,
                    "Los asistentes (" + attendees + ") superan la capacidad de la sala (" + room.getCapacity() + ")");
        }
    }

    /** RN-05: solo reserva un miembro ACTIVE. */
    public void validateMember(Member member) {
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessRuleException(RN_05, "El miembro está suspendido y no puede reservar");
        }
    }

    /** RN-09: PENDING → CONFIRMED → COMPLETED, o PENDING/CONFIRMED → CANCELLED. Ninguna otra transición. */
    public void validateTransition(BookingStatus from, BookingStatus to) {
        boolean allowed = switch (to) {
            case CONFIRMED -> from == BookingStatus.PENDING;
            case COMPLETED -> from == BookingStatus.CONFIRMED;
            case CANCELLED -> from == BookingStatus.PENDING || from == BookingStatus.CONFIRMED;
            case PENDING -> false;
        };
        if (!allowed) {
            throw new BusinessRuleException(RN_09, "No se puede pasar una reserva de " + from + " a " + to);
        }
    }

    /** RN-10: los servicios solo se cambian mientras la reserva está PENDING. */
    public void validateServicesEditable(Booking booking) {
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BusinessRuleException(RN_10,
                    "Los servicios solo se pueden cambiar mientras la reserva está PENDING (estado actual: " + booking.getStatus() + ")");
        }
    }
}
