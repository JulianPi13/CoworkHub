package com.campus.coworkhub.business.dto.booking;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Datos para crear una reserva con sus servicios adicionales")
public record ReservaRequest(
        @Schema(example = "1") @NotNull Long salaId,
        @Schema(example = "1", description = "Un MEMBER solo puede reservar a su propio nombre") @NotNull Long miembroId,
        @Schema(example = "2030-06-03T10:00:00") @NotNull LocalDateTime inicio,
        @Schema(example = "2030-06-03T12:00:00") @NotNull LocalDateTime fin,
        @Schema(example = "4") @NotNull @Positive Integer asistentes,
        @Valid List<ServicioReservaRequest> servicios
) {
}
