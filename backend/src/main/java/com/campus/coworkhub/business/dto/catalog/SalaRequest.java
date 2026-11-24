package com.campus.coworkhub.business.dto.catalog;

import com.campus.coworkhub.persistence.entity.RoomType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Set;

@Schema(description = "Datos para crear o actualizar una sala")
public record SalaRequest(
        @Schema(example = "Sala Andes") @NotBlank @Size(max = 100) String nombre,
        @Schema(example = "MEETING_ROOM") @NotNull RoomType tipo,
        @Schema(example = "8") @NotNull @Positive Integer capacidad,
        @Schema(example = "40000") @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal tarifaHora,
        @Schema(example = "true", description = "Si se omite, la sala queda activa") Boolean activa,
        @Schema(example = "1") @NotNull Long sedeId,
        @Schema(example = "[1, 2]") Set<Long> equipamientoIds
) {
}
