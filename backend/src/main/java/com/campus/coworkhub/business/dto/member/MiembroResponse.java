package com.campus.coworkhub.business.dto.member;

import com.campus.coworkhub.persistence.entity.MemberStatus;

public record MiembroResponse(
        Long id,
        String documento,
        String nombreCompleto,
        String email,
        MemberStatus estado,
        Long planId,
        String planNombre,
        String usuario
) {
}
