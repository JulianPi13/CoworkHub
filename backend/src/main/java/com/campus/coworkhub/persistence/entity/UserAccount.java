package com.campus.coworkhub.persistence.entity;

import jakarta.persistence.*;

/**
 * Cuenta para iniciar sesión (de un miembro o de un empleado).
 * Se llama UserAccount y no User para no confundirse con la clase User de Spring Security.
 */
@Entity
@Table(name = "user_accounts")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /** Contraseña codificada con BCrypt. */
    @Column(nullable = false, length = 100)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /** Lado inverso del 1 ↔ 1 (el dueño es Member.userAccount). Es null para empleados. */
    @OneToOne(mappedBy = "userAccount", fetch = FetchType.LAZY)
    private Member member;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }
}
