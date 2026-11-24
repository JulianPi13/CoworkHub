package com.campus.coworkhub.business.dto.catalog;

import java.math.BigDecimal;

public record PlanResponse(
        Long id,
        String nombre,
        Integer horasIncluidasMes,
        BigDecimal descuentoExcedente,
        Integer maxReservasActivas
) {
}
