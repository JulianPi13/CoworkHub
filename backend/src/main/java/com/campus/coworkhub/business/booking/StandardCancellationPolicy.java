package com.campus.coworkhub.business.booking;

import com.campus.coworkhub.business.exception.BusinessRuleException;
import com.campus.coworkhub.business.exception.BusinessRules;
import com.campus.coworkhub.persistence.entity.Booking;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * RN-08. Con 24 h o más de anticipación: sin cargo y se devuelven las horas.
 * Entre 24 h y 2 h: se cobra el 50 % del costo de la sala. Con menos de 2 h, o si ya empezó: no se puede cancelar.
 */
@Component
public class StandardCancellationPolicy implements CancellationPolicy {

    static final Duration FREE_LIMIT = Duration.ofHours(24);
    static final Duration MIN_LIMIT = Duration.ofHours(2);
    static final BigDecimal LATE_FEE_RATE = new BigDecimal("0.50");

    private final Clock clock;

    public StandardCancellationPolicy(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Outcome evaluate(Booking booking) {
        LocalDateTime now = LocalDateTime.now(clock);
        if (!now.isBefore(booking.getStartTime())) {
            throw new BusinessRuleException(BusinessRules.RN_08, "La reserva ya empezó y no se puede cancelar");
        }
        Duration advance = Duration.between(now, booking.getStartTime());
        if (advance.compareTo(FREE_LIMIT) >= 0) {
            return new Outcome(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), true);
        }
        if (advance.compareTo(MIN_LIMIT) >= 0) {
            BigDecimal fee = booking.getRoomCost().multiply(LATE_FEE_RATE).setScale(2, RoundingMode.HALF_UP);
            return new Outcome(fee, false);
        }
        throw new BusinessRuleException(BusinessRules.RN_08,
                "Con menos de 2 h de anticipación la reserva no se puede cancelar");
    }
}
