package com.campus.coworkhub.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Plan de membresía: horas incluidas al mes, descuento sobre excedentes y máximo de reservas activas. */
@Entity
@Table(name = "membership_plans")
public class MembershipPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String name;

    @Column(nullable = false)
    private Integer includedHoursPerMonth;

    /** Porcentaje de descuento (0 a 100) sobre las horas excedentes. */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal overageDiscount;

    @Column(nullable = false)
    private Integer maxActiveBookings;

    /** Lado inverso de 1 → N con Member. */
    @OneToMany(mappedBy = "plan", fetch = FetchType.LAZY)
    private List<Member> members = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getIncludedHoursPerMonth() { return includedHoursPerMonth; }
    public void setIncludedHoursPerMonth(Integer includedHoursPerMonth) { this.includedHoursPerMonth = includedHoursPerMonth; }
    public BigDecimal getOverageDiscount() { return overageDiscount; }
    public void setOverageDiscount(BigDecimal overageDiscount) { this.overageDiscount = overageDiscount; }
    public Integer getMaxActiveBookings() { return maxActiveBookings; }
    public void setMaxActiveBookings(Integer maxActiveBookings) { this.maxActiveBookings = maxActiveBookings; }
    public List<Member> getMembers() { return members; }
}
