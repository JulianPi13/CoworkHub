package com.campus.coworkhub.business.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Actualización de nombre, email y plan de un miembro")
public record MiembroUpdateRequest(
        @Schema(example = "Ana María Gómez") @NotBlank @Size(max = 120) String nombreCompleto,
        @Schema(example = "ana.gomez@correo.com") @NotBlank @Email @Size(max = 120) String email,
        @Schema(example = "2") @NotNull Long planId
) {
}
