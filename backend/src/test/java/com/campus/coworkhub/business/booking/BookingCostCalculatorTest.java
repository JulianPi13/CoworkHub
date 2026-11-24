package com.campus.coworkhub.business.booking;

import com.campus.coworkhub.persistence.entity.AdditionalService;
import com.campus.coworkhub.persistence.entity.Booking;
import com.campus.coworkhub.persistence.entity.BookingItem;
import com.campus.coworkhub.persistence.entity.BookingStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/** RN-07: horas incluidas sin costo, excedentes con descuento y servicios por cantidad × precio aplicado. */
class BookingCostCalculatorTest {

    private static final BigDecimal RATE = new BigDecimal("40000");
    private static final BigDecimal DISCOUNT_10 = new BigDecimal("10");

    @Test
    void dentroDeLasHorasIncluidas_noSeCobra() {
        // Plan de 10 h, lleva 6 h usadas, reserva de 2 h -> 0
        assertThat(BookingCostCalculator.calculateRoomCost(10, 6 * 60, 120, RATE, DISCOUNT_10))
                .isEqualByComparingTo("0");
    }

    @Test
    void parteIncluidaYParteExcedente() {
        // Plan de 10 h, lleva 9 h, reserva de 3 h -> 2 h excedentes × 40000 × 0.9 = 72000
        assertThat(BookingCostCalculator.calculateRoomCost(10, 9 * 60, 180, RATE, DISCOUNT_10))
                .isEqualByComparingTo("72000.00");
    }

    @Test
    void todoExcedenteConMediaHora() {
        // Sin horas restantes, reserva de 1.5 h -> 1.5 × 40000 × 0.9 = 54000
        assertThat(BookingCostCalculator.calculateRoomCost(10, 12 * 60, 90, RATE, DISCOUNT_10))
                .isEqualByComparingTo("54000.00");
    }

    @Test
    void serviciosSumanCantidadPorPrecioAplicado() {
        Booking booking = new Booking();
        booking.setRoomCost(new BigDecimal("72000.00"));
        booking.addItem(item(2, "8000"));
        booking.addItem(item(3, "15000"));
        BookingCostCalculator.refreshTotals(booking);
        assertThat(booking.getServicesCost()).isEqualByComparingTo("61000.00");
        assertThat(booking.getTotalCost()).isEqualByComparingTo("133000.00");
    }

    @Test
    void canceladaConCargo_sigueContandoEnElConsumo() {
        Booking cancelledWithFee = new Booking();
        cancelledWithFee.setStatus(BookingStatus.CANCELLED);
        cancelledWithFee.setHoursRefunded(false);
        Booking cancelledFree = new Booking();
        cancelledFree.setStatus(BookingStatus.CANCELLED);
        cancelledFree.setHoursRefunded(true);
        assertThat(BookingCostCalculator.countsForConsumption(cancelledWithFee)).isTrue();
        assertThat(BookingCostCalculator.countsForConsumption(cancelledFree)).isFalse();
    }

    private static BookingItem item(int quantity, String price) {
        AdditionalService service = new AdditionalService();
        service.setId((long) quantity);
        BookingItem item = new BookingItem();
        item.setService(service);
        item.setQuantity(quantity);
        item.setAppliedUnitPrice(new BigDecimal(price));
        return item;
    }
}
