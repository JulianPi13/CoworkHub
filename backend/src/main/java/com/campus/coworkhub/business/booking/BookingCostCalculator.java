package com.campus.coworkhub.business.booking;

import com.campus.coworkhub.persistence.entity.*;
import com.campus.coworkhub.persistence.repository.BookingRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Objects;

/**
 * RN-07 (costo) y RF-10 (consumo).
 * <ul>
 *   <li>Las horas que el plan incluye en el mes no se cobran.</li>
 *   <li>Las horas excedentes valen hourlyRate × (1 − overageDiscount/100).</li>
 *   <li>Los servicios suman quantity × appliedUnitPrice.</li>
 * </ul>
 * Cuentan para el consumo las reservas PENDING, CONFIRMED y COMPLETED, y las CANCELLED a las que
 * no se les devolvieron las horas (cancelación con cargo, RN-08).
 */
@Component
public class BookingCostCalculator {

    private static final BigDecimal SIXTY = BigDecimal.valueOf(60);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final BookingRepository bookingRepository;

    public BookingCostCalculator(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    /** Costo de la sala para una reserva nueva o existente (excludeBookingId evita contarse a sí misma). */
    public BigDecimal roomCost(Member member, Room room, LocalDateTime start, LocalDateTime end, Long excludeBookingId) {
        long usedMinutes = minutesUsedInMonth(member.getId(), YearMonth.from(start), excludeBookingId);
        long bookingMinutes = java.time.Duration.between(start, end).toMinutes();
        MembershipPlan plan = member.getPlan();
        return calculateRoomCost(plan.getIncludedHoursPerMonth(), usedMinutes, bookingMinutes,
                room.getHourlyRate(), plan.getOverageDiscount());
    }

    /** Minutos consumidos por un miembro en un mes. */
    public long minutesUsedInMonth(Long memberId, YearMonth month, Long excludeBookingId) {
        LocalDateTime from = month.atDay(1).atStartOfDay();
        LocalDateTime to = month.plusMonths(1).atDay(1).atStartOfDay();
        return bookingRepository.findByMemberIdAndStartTimeGreaterThanEqualAndStartTimeLessThan(memberId, from, to)
                .stream()
                .filter(b -> !Objects.equals(b.getId(), excludeBookingId))
                .filter(BookingCostCalculator::countsForConsumption)
                .mapToLong(Booking::durationMinutes)
                .sum();
    }

    public static boolean countsForConsumption(Booking booking) {
        return booking.getStatus() != BookingStatus.CANCELLED || !booking.isHoursRefunded();
    }

    /** Cálculo puro (sin base de datos) para poder probarlo de forma aislada. */
    public static BigDecimal calculateRoomCost(int includedHours, long usedMinutes, long bookingMinutes,
                                               BigDecimal hourlyRate, BigDecimal overageDiscountPercent) {
        long includedMinutes = includedHours * 60L;
        long remainingIncluded = Math.max(0, includedMinutes - usedMinutes);
        long overageMinutes = Math.max(0, bookingMinutes - remainingIncluded);
        if (overageMinutes == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal factor = BigDecimal.ONE.subtract(overageDiscountPercent.divide(HUNDRED, 6, RoundingMode.HALF_UP));
        return hourlyRate
                .multiply(BigDecimal.valueOf(overageMinutes))
                .divide(SIXTY, 6, RoundingMode.HALF_UP)
                .multiply(factor)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal servicesCost(Booking booking) {
        return booking.getItems().stream()
                .map(BookingItem::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /** Recalcula costo de servicios y total a partir de los detalles actuales. */
    public static void refreshTotals(Booking booking) {
        booking.setServicesCost(servicesCost(booking));
        booking.setTotalCost(booking.getRoomCost().add(booking.getServicesCost()).setScale(2, RoundingMode.HALF_UP));
    }

    public static BigDecimal minutesToHours(long minutes) {
        return BigDecimal.valueOf(minutes).divide(SIXTY, 2, RoundingMode.HALF_UP);
    }
}
