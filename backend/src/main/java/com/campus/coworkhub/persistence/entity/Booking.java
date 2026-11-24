package com.campus.coworkhub.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Reserva de una sala a nombre de un miembro. */
@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_booking_room_time", columnList = "room_id, start_time, end_time"),
        @Index(name = "idx_booking_member", columnList = "member_id")
})
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private Integer attendees;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal roomCost = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal servicesCost = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCost = BigDecimal.ZERO;

    /** Cargo cobrado al cancelar (RN-08). Es null mientras la reserva no se cancele. */
    @Column(precision = 12, scale = 2)
    private BigDecimal cancellationFee;

    /**
     * true si al cancelar se devolvieron las horas del plan (cancelación con 24 h o más, RN-08).
     * Una reserva cancelada con cargo sigue contando en el consumo del mes.
     */
    @Column(nullable = false)
    private boolean hoursRefunded = false;

    /** Lado dueño de N → 1 con Room. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    /** Lado dueño de N → 1 con Member. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /** 1 → N con BookingItem: los detalles se guardan y se eliminan con la reserva. */
    @OneToMany(mappedBy = "booking", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingItem> items = new ArrayList<>();

    // ---------- Métodos de dominio ----------

    public void addItem(BookingItem item) {
        item.setBooking(this);
        items.add(item);
    }

    public void removeItem(BookingItem item) {
        items.remove(item);
        item.setBooking(null);
    }

    public Optional<BookingItem> findItemByServiceId(Long serviceId) {
        return items.stream()
                .filter(i -> i.getService().getId().equals(serviceId))
                .findFirst();
    }

    public long durationMinutes() {
        return Duration.between(startTime, endTime).toMinutes();
    }

    // ---------- Getters y setters ----------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public Integer getAttendees() { return attendees; }
    public void setAttendees(Integer attendees) { this.attendees = attendees; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public BigDecimal getRoomCost() { return roomCost; }
    public void setRoomCost(BigDecimal roomCost) { this.roomCost = roomCost; }
    public BigDecimal getServicesCost() { return servicesCost; }
    public void setServicesCost(BigDecimal servicesCost) { this.servicesCost = servicesCost; }
    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }
    public BigDecimal getCancellationFee() { return cancellationFee; }
    public void setCancellationFee(BigDecimal cancellationFee) { this.cancellationFee = cancellationFee; }
    public boolean isHoursRefunded() { return hoursRefunded; }
    public void setHoursRefunded(boolean hoursRefunded) { this.hoursRefunded = hoursRefunded; }
    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }
    public List<BookingItem> getItems() { return items; }
}
