package com.campus.coworkhub.business.service;

import com.campus.coworkhub.business.dto.catalog.SalaResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

public interface AvailabilityService {

    /** RF-03: salas libres de una sede en una fecha y rango horario. */
    List<SalaResponse> findAvailableRooms(Long locationId, LocalDate date, LocalTime from, LocalTime to,
                                          Integer minCapacity, Set<Long> equipmentIds);
}
