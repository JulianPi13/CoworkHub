package com.campus.coworkhub.business.dto.auth;

import com.campus.coworkhub.persistence.entity.Role;

/** RF-14: datos del usuario que inició sesión. miembroId es null para empleados. */
public record PerfilResponse(String usuario, Role rol, Long miembroId) {
}
