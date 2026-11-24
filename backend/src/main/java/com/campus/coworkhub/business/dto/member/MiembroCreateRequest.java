package com.campus.coworkhub.business.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Registro de un miembro junto con su cuenta de usuario (rol MEMBER)")
public record MiembroCreateRequest(
        @Schema(example = "1098765432") @NotBlank @Size(max = 30) String documento,
        @Schema(example = "Ana Gómez") @NotBlank @Size(max = 120) String nombreCompleto,
        @Schema(example = "ana.gomez@correo.com") @NotBlank @Email @Size(max = 120) String email,
        @Schema(example = "1") @NotNull Long planId,
        @Schema(example = "ana") @NotBlank @Size(min = 3, max = 50)
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "solo letras, números, punto, guion y guion bajo") String usuario,
        @Schema(example = "Ana12345") @NotBlank @Size(min = 6, max = 72) String contrasena
) {
}
