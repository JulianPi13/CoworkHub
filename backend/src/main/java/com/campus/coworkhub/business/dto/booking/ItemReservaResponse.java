package com.campus.coworkhub.business.dto.booking;

import java.math.BigDecimal;

public record ItemReservaResponse(
        Long servicioId,
        String servicioNombre,
        Integer cantidad,
        BigDecimal precioUnitarioAplicado,
        BigDecimal subtotal
) {
}
