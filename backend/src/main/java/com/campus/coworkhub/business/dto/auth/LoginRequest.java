package com.campus.coworkhub.business.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciales para iniciar sesión")
public record LoginRequest(
        @Schema(example = "admin") @NotBlank String usuario,
        @Schema(example = "Admin123") @NotBlank String contrasena
) {
}
