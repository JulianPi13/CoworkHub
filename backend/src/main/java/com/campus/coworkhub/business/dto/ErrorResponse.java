package com.campus.coworkhub.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/** Cuerpo único de error de toda la API. */
@Schema(description = "Cuerpo de error común a toda la API")
public record ErrorResponse(
        @Schema(example = "La sala ya está reservada en ese horario") String error,
        @Schema(example = "RN-01", description = "Regla violada o tipo de error") String codigo,
        @Schema(example = "2030-06-03T13:00:00Z") Instant timestamp
) {
}
