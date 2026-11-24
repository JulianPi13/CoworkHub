package com.campus.coworkhub.business.dto.catalog;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

@Schema(description = "Datos para crear o actualizar una sede")
public record SedeRequest(
        @Schema(example = "CoWork Centro") @NotBlank @Size(max = 100) String nombre,
        @Schema(example = "Bucaramanga") @NotBlank @Size(max = 80) String ciudad,
        @Schema(example = "Calle 36 # 15-20") @NotBlank @Size(max = 150) String direccion,
        @Schema(example = "07:00", type = "string") @NotNull LocalTime horaApertura,
        @Schema(example = "20:00", type = "string") @NotNull LocalTime horaCierre
) {
}
