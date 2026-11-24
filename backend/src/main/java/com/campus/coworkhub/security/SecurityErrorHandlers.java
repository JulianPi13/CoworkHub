package com.campus.coworkhub.security;

import com.campus.coworkhub.business.dto.ErrorResponse;
import com.campus.coworkhub.business.exception.BusinessRules;
import com.campus.coworkhub.config.AppConfig;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Optional;

/** Respuestas 401 y 403 de la cadena de seguridad con el mismo cuerpo de error que el resto de la API. */
@Component
public class SecurityErrorHandlers {

    private final ObjectMapper objectMapper;
    private final Clock clock;

    public SecurityErrorHandlers(ObjectMapper objectMapper, @Qualifier(AppConfig.UTC_CLOCK) Clock clock) {
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, ex) -> {
            String message = Optional.ofNullable(request.getAttribute(JwtAuthenticationFilter.JWT_ERROR_ATTRIBUTE))
                    .map(Object::toString)
                    .orElse("Se requiere un token válido (encabezado Authorization: Bearer <token>)");
            write(response, HttpStatus.UNAUTHORIZED, message, BusinessRules.UNAUTHORIZED);
        };
    }

    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, ex) ->
                write(response, HttpStatus.FORBIDDEN, "No tiene permiso para realizar esta acción", BusinessRules.FORBIDDEN);
    }

    private void write(HttpServletResponse response, HttpStatus status, String message, String code) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getOutputStream(), new ErrorResponse(message, code, clock.instant()));
    }
}
