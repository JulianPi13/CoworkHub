package com.campus.coworkhub.business.dto.booking;

import com.campus.coworkhub.persistence.entity.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ReservaResponse(
        Long id,
        Long salaId,
        String salaNombre,
        Long miembroId,
        String miembroNombre,
        LocalDateTime inicio,
        LocalDateTime fin,
        Integer asistentes,
        BookingStatus estado,
        BigDecimal costoSala,
        BigDecimal costoServicios,
        BigDecimal costoTotal,
        BigDecimal cargoCancelacion,
        boolean horasDevueltas,
        List<ItemReservaResponse> servicios
) {
}
