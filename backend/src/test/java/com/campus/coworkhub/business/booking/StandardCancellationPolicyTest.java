package com.campus.coworkhub.business.booking;

import com.campus.coworkhub.business.exception.BusinessRuleException;
import com.campus.coworkhub.persistence.entity.Booking;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** RN-08 con la hora fijada: "ahora" es 2030-06-03 08:00. */
class StandardCancellationPolicyTest {

    private static final ZoneId ZONE = ZoneId.of("America/Bogota");
    private static final LocalDateTime NOW = LocalDateTime.of(2030, 6, 3, 8, 0);
    private final StandardCancellationPolicy policy =
            new StandardCancellationPolicy(Clock.fixed(NOW.atZone(ZONE).toInstant(), ZONE));

    private static Booking bookingStartingIn(long hours) {
        Booking b = new Booking();
        b.setStartTime(NOW.plusHours(hours));
        b.setEndTime(NOW.plusHours(hours + 2));
        b.setRoomCost(new BigDecimal("80000.00"));
        return b;
    }

    @Test
    void con30Horas_sinCargoYDevuelveHoras() {
        CancellationPolicy.Outcome outcome = policy.evaluate(bookingStartingIn(30));
        assertThat(outcome.fee()).isEqualByComparingTo("0");
        assertThat(outcome.hoursRefunded()).isTrue();
    }

    @Test
    void conExactamente24Horas_sinCargo() {
        assertThat(policy.evaluate(bookingStartingIn(24)).fee()).isEqualByComparingTo("0");
    }

    @Test
    void con5Horas_cobra50PorCientoDeLaSala() {
        CancellationPolicy.Outcome outcome = policy.evaluate(bookingStartingIn(5));
        assertThat(outcome.fee()).isEqualByComparingTo("40000.00");
        assertThat(outcome.hoursRefunded()).isFalse();
    }

    @Test
    void con1Hora_noSePuedeCancelar() {
        assertThatThrownBy(() -> policy.evaluate(bookingStartingIn(1)))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("code").isEqualTo("RN-08");
    }

    @Test
    void siYaEmpezo_noSePuedeCancelar() {
        assertThatThrownBy(() -> policy.evaluate(bookingStartingIn(-1)))
                .extracting("code").isEqualTo("RN-08");
    }
}
