package com.campus.coworkhub.business.dto.auth;

import com.campus.coworkhub.persistence.entity.Role;

public record LoginResponse(
        String token,
        String tipo,
        long expiraEnSegundos,
        String usuario,
        Role rol,
        Long miembroId
) {
}
