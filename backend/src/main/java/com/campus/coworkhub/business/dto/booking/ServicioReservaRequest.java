package com.campus.coworkhub.business.dto.booking;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Servicio adicional para una reserva")
public record ServicioReservaRequest(
        @Schema(example = "1") @NotNull Long servicioId,
        @Schema(example = "2") @NotNull @Positive @Max(1000) Integer cantidad
) {
}
