package com.campus.coworkhub.business.dto.member;

import java.math.BigDecimal;

/** RF-10: consumo mensual de horas de un miembro. */
public record ConsumoResponse(
        Long miembroId,
        String mes,
        String plan,
        BigDecimal horasIncluidas,
        BigDecimal horasUsadas,
        BigDecimal horasRestantes,
        BigDecimal horasExcedentes
) {
}
