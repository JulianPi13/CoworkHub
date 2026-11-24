package com.campus.coworkhub.business.dto.catalog;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Datos para crear o actualizar un servicio adicional")
public record ServicioAdicionalRequest(
        @Schema(example = "Café ilimitado") @NotBlank @Size(max = 80) String nombre,
        @Schema(example = "8000") @NotNull @DecimalMin(value = "0.0") BigDecimal precioUnitario
) {
}
