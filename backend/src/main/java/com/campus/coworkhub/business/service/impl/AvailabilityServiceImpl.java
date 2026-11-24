package com.campus.coworkhub.business.service.impl;

import com.campus.coworkhub.business.dto.catalog.SalaResponse;
import com.campus.coworkhub.business.exception.InvalidRequestException;
import com.campus.coworkhub.business.exception.ResourceNotFoundException;
import com.campus.coworkhub.business.mapper.DtoMapper;
import com.campus.coworkhub.business.service.AvailabilityService;
import com.campus.coworkhub.persistence.entity.BookingStatus;
import com.campus.coworkhub.persistence.entity.Equipment;
import com.campus.coworkhub.persistence.entity.Location;
import com.campus.coworkhub.persistence.repository.BookingRepository;
import com.campus.coworkhub.persistence.repository.LocationRepository;
import com.campus.coworkhub.persistence.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AvailabilityServiceImpl implements AvailabilityService {

    static final List<BookingStatus> BLOCKING_STATUSES = List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);

    private final LocationRepository locationRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    public AvailabilityServiceImpl(LocationRepository locationRepository, RoomRepository roomRepository,
                                   BookingRepository bookingRepository) {
        this.locationRepository = locationRepository;
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public List<SalaResponse> findAvailableRooms(Long locationId, LocalDate date, LocalTime from, LocalTime to,
                                                 Integer minCapacity, Set<Long> equipmentIds) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Sede", locationId));
        if (!from.isBefore(to)) {
            throw new InvalidRequestException("horaInicio debe ser anterior a horaFin");
        }
        // Fuera del horario de la sede no hay salas disponibles
        if (from.isBefore(location.getOpeningTime()) || to.isAfter(location.getClosingTime())) {
            return List.of();
        }
        LocalDateTime start = date.atTime(from);
        LocalDateTime end = date.atTime(to);
        Set<Long> requiredEquipment = Optional.ofNullable(equipmentIds).orElseGet(Set::of);
        int capacity = Optional.ofNullable(minCapacity).orElse(1);

        // Dos consultas en total: salas (con sede y equipamiento) + ids de salas ocupadas
        Set<Long> busy = new HashSet<>(bookingRepository.findBusyRoomIds(locationId, start, end, BLOCKING_STATUSES));

        return roomRepository.findByLocationIdAndActiveTrueAndCapacityGreaterThanEqual(locationId, capacity).stream()
                .filter(room -> !busy.contains(room.getId()))
                .filter(room -> room.getEquipment().stream()
                        .map(Equipment::getId)
                        .collect(Collectors.toSet())
                        .containsAll(requiredEquipment))
                .sorted(Comparator.comparing(r -> r.getId()))
                .map(DtoMapper::toResponse)
                .toList();
    }
}
