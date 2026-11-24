package com.campus.coworkhub.security;

import com.campus.coworkhub.persistence.entity.Role;

/** Usuario autenticado que viaja en el SecurityContext (se arma a partir de los claims del JWT). */
public record AuthenticatedUser(String username, Role role, Long memberId) {

    public boolean isMember() {
        return role == Role.MEMBER;
    }
}
