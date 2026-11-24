package com.campus.coworkhub.presentation.controller;

import com.campus.coworkhub.business.dto.auth.LoginRequest;
import com.campus.coworkhub.business.dto.auth.LoginResponse;
import com.campus.coworkhub.business.dto.auth.PerfilResponse;
import com.campus.coworkhub.business.service.AuthService;
import com.campus.coworkhub.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación", description = "Inicio de sesión con JWT y perfil del usuario actual")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Iniciar sesión (RF-11)",
            description = "Valida usuario y contraseña y devuelve un JWT que vence en 60 minutos. Endpoint público.")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/perfil")
    @Operation(summary = "Perfil del usuario autenticado (RF-14)",
            description = "Devuelve usuario, rol y miembroId a partir del token. miembroId es null para empleados.")
    public PerfilResponse perfil(@AuthenticationPrincipal AuthenticatedUser user) {
        return new PerfilResponse(user.username(), user.role(), user.memberId());
    }
}
