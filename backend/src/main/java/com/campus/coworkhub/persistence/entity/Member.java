package com.campus.coworkhub.persistence.entity;

import jakarta.persistence.*;

/** Miembro: cliente del coworking con un plan de membresía. */
@Entity
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String documentNumber;

    @Column(nullable = false, length = 120)
    private String fullName;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status = MemberStatus.ACTIVE;

    /** Lado dueño de N → 1 con MembershipPlan. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private MembershipPlan plan;

    /**
     * Lado dueño del 1 ↔ 1 con UserAccount. La cuenta se crea y se elimina junto con el
     * miembro (cascade + orphanRemoval). Es null si el miembro no tiene cuenta.
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_account_id", unique = true)
    private UserAccount userAccount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public MemberStatus getStatus() { return status; }
    public void setStatus(MemberStatus status) { this.status = status; }
    public MembershipPlan getPlan() { return plan; }
    public void setPlan(MembershipPlan plan) { this.plan = plan; }
    public UserAccount getUserAccount() { return userAccount; }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
        if (userAccount != null) {
            userAccount.setMember(this);
        }
    }
}
