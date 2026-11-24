package com.campus.coworkhub.business.mapper;

import com.campus.coworkhub.business.dto.booking.ItemReservaResponse;
import com.campus.coworkhub.business.dto.booking.ReservaResponse;
import com.campus.coworkhub.business.dto.catalog.*;
import com.campus.coworkhub.business.dto.member.MiembroResponse;
import com.campus.coworkhub.persistence.entity.*;

import java.util.Comparator;
import java.util.Optional;

/** Convierte entidades en DTO de salida. La API nunca devuelve entidades. */
public final class DtoMapper {

    private DtoMapper() {
    }

    public static SedeResponse toResponse(Location l) {
        return new SedeResponse(l.getId(), l.getName(), l.getCity(), l.getAddress(),
                l.getOpeningTime(), l.getClosingTime());
    }

    public static EquipamientoResponse toResponse(Equipment e) {
        return new EquipamientoResponse(e.getId(), e.getName());
    }

    public static SalaResponse toResponse(Room r) {
        return new SalaResponse(r.getId(), r.getName(), r.getType(), r.getCapacity(), r.getHourlyRate(),
                r.isActive(), r.getLocation().getId(), r.getLocation().getName(),
                r.getEquipment().stream()
                        .sorted(Comparator.comparing(Equipment::getId))
                        .map(DtoMapper::toResponse)
                        .toList());
    }

    public static ServicioAdicionalResponse toResponse(AdditionalService s) {
        return new ServicioAdicionalResponse(s.getId(), s.getName(), s.getUnitPrice());
    }

    public static PlanResponse toResponse(MembershipPlan p) {
        return new PlanResponse(p.getId(), p.getName(), p.getIncludedHoursPerMonth(),
                p.getOverageDiscount(), p.getMaxActiveBookings());
    }

    public static MiembroResponse toResponse(Member m) {
        String username = Optional.ofNullable(m.getUserAccount()).map(UserAccount::getUsername).orElse(null);
        return new MiembroResponse(m.getId(), m.getDocumentNumber(), m.getFullName(), m.getEmail(),
                m.getStatus(), m.getPlan().getId(), m.getPlan().getName(), username);
    }

    public static ReservaResponse toResponse(Booking b) {
        return new ReservaResponse(b.getId(), b.getRoom().getId(), b.getRoom().getName(),
                b.getMember().getId(), b.getMember().getFullName(),
                b.getStartTime(), b.getEndTime(), b.getAttendees(), b.getStatus(),
                b.getRoomCost(), b.getServicesCost(), b.getTotalCost(), b.getCancellationFee(),
                b.isHoursRefunded(),
                b.getItems().stream()
                        .sorted(Comparator.comparing(i -> i.getService().getId()))
                        .map(i -> new ItemReservaResponse(i.getService().getId(), i.getService().getName(),
                                i.getQuantity(), i.getAppliedUnitPrice(), i.subtotal()))
                        .toList());
    }
}
