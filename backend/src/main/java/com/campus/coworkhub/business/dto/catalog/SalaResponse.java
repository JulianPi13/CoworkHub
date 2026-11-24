package com.campus.coworkhub.business.dto.catalog;

import com.campus.coworkhub.persistence.entity.RoomType;

import java.math.BigDecimal;
import java.util.List;

public record SalaResponse(
        Long id,
        String nombre,
        RoomType tipo,
        Integer capacidad,
        BigDecimal tarifaHora,
        boolean activa,
        Long sedeId,
        String sedeNombre,
        List<EquipamientoResponse> equipamientos
) {
}
