package com.campus.coworkhub.business.dto.catalog;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para crear o actualizar un equipamiento")
public record EquipamientoRequest(
        @Schema(example = "Proyector") @NotBlank @Size(max = 80) String nombre
) {
}
