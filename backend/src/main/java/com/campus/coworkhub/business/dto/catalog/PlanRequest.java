package com.campus.coworkhub.business.dto.catalog;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "Datos para crear o actualizar un plan de membresía")
public record PlanRequest(
        @Schema(example = "Plan Básico") @NotBlank @Size(max = 80) String nombre,
        @Schema(example = "10") @NotNull @PositiveOrZero Integer horasIncluidasMes,
        @Schema(example = "10", description = "Porcentaje de descuento sobre horas excedentes (0 a 100)")
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal descuentoExcedente,
        @Schema(example = "3") @NotNull @Positive Integer maxReservasActivas
) {
}
