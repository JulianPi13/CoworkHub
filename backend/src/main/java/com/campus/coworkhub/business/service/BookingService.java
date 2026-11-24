package com.campus.coworkhub.business.service;

import com.campus.coworkhub.business.dto.booking.ReservaRequest;
import com.campus.coworkhub.business.dto.booking.ReservaResponse;
import com.campus.coworkhub.business.dto.booking.ServicioReservaRequest;
import com.campus.coworkhub.persistence.entity.BookingStatus;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {
    ReservaResponse create(ReservaRequest request);
    ReservaResponse findById(Long id);
    List<ReservaResponse> search(Long memberId, Long roomId, LocalDate date, BookingStatus status);
    ReservaResponse addService(Long bookingId, ServicioReservaRequest request);
    ReservaResponse removeService(Long bookingId, Long serviceId);
    ReservaResponse confirm(Long id);
    ReservaResponse cancel(Long id);
    ReservaResponse complete(Long id);
    /** Id del miembro dueño de la reserva (para validar acceso). Vacío si la reserva no existe. */
    java.util.Optional<Long> findOwnerMemberId(Long bookingId);
}
