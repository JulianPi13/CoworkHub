package com.campus.coworkhub.business.dto.catalog;

import java.time.LocalTime;

public record SedeResponse(
        Long id,
        String nombre,
        String ciudad,
        String direccion,
        LocalTime horaApertura,
        LocalTime horaCierre
) {
}
