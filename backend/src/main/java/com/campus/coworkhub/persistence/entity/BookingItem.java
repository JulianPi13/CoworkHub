package com.campus.coworkhub.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

/** Detalle de una reserva: un servicio adicional con su cantidad y el precio vigente al agregarlo. */
@Entity
@Table(name = "booking_items",
        uniqueConstraints = @UniqueConstraint(name = "uk_booking_item_service", columnNames = {"booking_id", "service_id"}))
public class BookingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer quantity;

    /** Precio unitario vigente al momento de agregar el servicio (RN-07). */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal appliedUnitPrice;

    /** Lado dueño de N → 1 con Booking. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    /** Lado dueño de N → 1 con AdditionalService. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private AdditionalService service;

    public BigDecimal subtotal() {
        return appliedUnitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getAppliedUnitPrice() { return appliedUnitPrice; }
    public void setAppliedUnitPrice(BigDecimal appliedUnitPrice) { this.appliedUnitPrice = appliedUnitPrice; }
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
    public AdditionalService getService() { return service; }
    public void setService(AdditionalService service) { this.service = service; }
}
