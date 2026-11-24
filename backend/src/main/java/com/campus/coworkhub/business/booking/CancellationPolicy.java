package com.campus.coworkhub.business.booking;

import com.campus.coworkhub.persistence.entity.Booking;

import java.math.BigDecimal;

/** Política de cobro por cancelación. */
public interface CancellationPolicy {

    /** Resultado de evaluar una cancelación: el cargo a cobrar y si se devuelven las horas del plan. */
    record Outcome(BigDecimal fee, boolean hoursRefunded) {
    }

    /**
     * Evalúa la cancelación de una reserva.
     *
     * @throws com.campus.coworkhub.business.exception.BusinessRuleException si ya no se puede cancelar
     */
    Outcome evaluate(Booking booking);
}
