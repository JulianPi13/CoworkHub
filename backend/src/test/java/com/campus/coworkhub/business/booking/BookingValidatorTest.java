package com.campus.coworkhub.business.booking;

import com.campus.coworkhub.business.exception.BusinessRuleException;
import com.campus.coworkhub.persistence.entity.*;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Pruebas con la hora fijada mediante Clock: "ahora" es 2030-06-03 08:00. */
class BookingValidatorTest {

    private static final ZoneId ZONE = ZoneId.of("America/Bogota");
    private static final LocalDateTime NOW = LocalDateTime.of(2030, 6, 3, 8, 0);
    private final BookingValidator validator =
            new BookingValidator(Clock.fixed(NOW.atZone(ZONE).toInstant(), ZONE));

    private static Location location() {
        Location l = new Location();
        l.setOpeningTime(LocalTime.of(7, 0));
        l.setClosingTime(LocalTime.of(20, 0));
        return l;
    }

    private static LocalDateTime at(int day, int hour, int minute) {
        return LocalDateTime.of(2030, 6, day, hour, minute);
    }

    @Test
    void duracionValida_de30MinA8HorasEnMultiplosDe30() {
        assertThatCode(() -> validator.validateDuration(at(4, 10, 0), at(4, 10, 30))).doesNotThrowAnyException();
        assertThatCode(() -> validator.validateDuration(at(4, 9, 0), at(4, 17, 0))).doesNotThrowAnyException();
    }

    @Test
    void duracionDe45Min_violaRN02() {
        assertThatThrownBy(() -> validator.validateDuration(at(4, 10, 0), at(4, 10, 45)))
                .isInstanceOf(BusinessRuleException.class)
                .extracting("code").isEqualTo("RN-02");
    }

    @Test
    void duracionMayorA8Horas_violaRN02() {
        assertThatThrownBy(() -> validator.validateDuration(at(4, 8, 0), at(4, 16, 30)))
                .extracting("code").isEqualTo("RN-02");
    }

    @Test
    void reservaQueTerminaOtroDia_violaRN02() {
        assertThatThrownBy(() -> validator.validateDuration(at(4, 23, 0), at(5, 0, 30)))
                .extracting("code").isEqualTo("RN-02");
    }

    @Test
    void fueraDelHorarioDeLaSede_violaRN03() {
        assertThatThrownBy(() -> validator.validateSchedule(location(), at(4, 19, 30), at(4, 20, 30)))
                .extracting("code").isEqualTo("RN-03");
    }

    @Test
    void conMenosDe1HoraDeAnticipacion_violaRN03() {
        assertThatThrownBy(() -> validator.validateSchedule(location(), at(3, 8, 30), at(3, 9, 30)))
                .extracting("code").isEqualTo("RN-03");
        assertThatCode(() -> validator.validateSchedule(location(), at(3, 9, 0), at(3, 10, 0)))
                .doesNotThrowAnyException();
    }

    @Test
    void salaInactivaOCapacidadSuperada_violaRN04() {
        Room room = new Room();
        room.setName("Sala");
        room.setCapacity(4);
        room.setActive(false);
        assertThatThrownBy(() -> validator.validateRoom(room, 2)).extracting("code").isEqualTo("RN-04");
        room.setActive(true);
        assertThatThrownBy(() -> validator.validateRoom(room, 5)).extracting("code").isEqualTo("RN-04");
        assertThatCode(() -> validator.validateRoom(room, 4)).doesNotThrowAnyException();
    }

    @Test
    void miembroSuspendido_violaRN05() {
        Member member = new Member();
        member.setStatus(MemberStatus.SUSPENDED);
        assertThatThrownBy(() -> validator.validateMember(member)).extracting("code").isEqualTo("RN-05");
    }

    @Test
    void transicionesDeEstado_RN09() {
        assertThatCode(() -> validator.validateTransition(BookingStatus.PENDING, BookingStatus.CONFIRMED)).doesNotThrowAnyException();
        assertThatCode(() -> validator.validateTransition(BookingStatus.CONFIRMED, BookingStatus.COMPLETED)).doesNotThrowAnyException();
        assertThatCode(() -> validator.validateTransition(BookingStatus.CONFIRMED, BookingStatus.CANCELLED)).doesNotThrowAnyException();
        assertThatThrownBy(() -> validator.validateTransition(BookingStatus.CANCELLED, BookingStatus.CONFIRMED))
                .extracting("code").isEqualTo("RN-09");
        assertThatThrownBy(() -> validator.validateTransition(BookingStatus.PENDING, BookingStatus.COMPLETED))
                .extracting("code").isEqualTo("RN-09");
    }

    @Test
    void serviciosSoloEnPending_RN10() {
        Booking booking = new Booking();
        booking.setStatus(BookingStatus.CONFIRMED);
        assertThatThrownBy(() -> validator.validateServicesEditable(booking)).extracting("code").isEqualTo("RN-10");
        booking.setStatus(BookingStatus.PENDING);
        assertThatCode(() -> validator.validateServicesEditable(booking)).doesNotThrowAnyException();
    }

    @Test
    void nowUsaElRelojInyectado() {
        assertThat(validator.now()).isEqualTo(NOW);
    }
}
