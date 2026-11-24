package com.campus.coworkhub.config;

import com.campus.coworkhub.business.dto.ErrorResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Swagger UI (módulo 06): grupos, descripciones, respuestas de error con ejemplos y botón Authorize (JWT).
 * UI: http://localhost:8080/doc/swagger-ui.html · JSON: http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER = "bearerAuth";
    private static final String ERROR_REF = "#/components/schemas/ErrorResponse";

    @Bean
    public OpenAPI coworkHubOpenApi() {
        Components components = new Components()
                .addSecuritySchemes(BEARER, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Pegue el token que devuelve POST /auth/login (sin la palabra Bearer)"));
        ModelConverters.getInstance().read(ErrorResponse.class).forEach(components::addSchemas);

        return new OpenAPI()
                .info(new Info()
                        .title("CoWorkHub API")
                        .version("1.0.0")
                        .description("""
                                API REST para la gestión de una red de coworking: sedes, salas, equipamiento, \
                                servicios adicionales, planes, miembros y reservas.

                                **Cómo probar:** 1) `POST /auth/login` con un usuario de prueba, 2) copie el `token`, \
                                3) pulse **Authorize** y péguelo.

                                Usuarios de prueba: `admin / Admin123`, `recepcion / Recep123`, `ana / Ana12345`, `luis / Luis12345`.

                                Todo error responde `{"error": "...", "codigo": "...", "timestamp": "..."}`.""")
                        .contact(new Contact().name("CoWorkHub")))
                .servers(List.of(new Server().url("http://localhost:8080").description("Local")))
                .components(components)
                .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }

    /** Agrega a cada operación las respuestas de error estándar con el cuerpo de error y un ejemplo. */
    @Bean
    public OpenApiCustomizer standardErrorResponses() {
        Map<String, String[]> errors = new LinkedHashMap<>();
        errors.put("400", new String[]{"Datos faltantes o con formato inválido", "nombre: no debe estar vacío", "VALIDACION"});
        errors.put("401", new String[]{"Sin token, o con un token alterado o vencido", "El token es inválido, fue alterado o está vencido", "NO_AUTENTICADO"});
        errors.put("403", new String[]{"El usuario no tiene permiso", "No tiene permiso para realizar esta acción", "ACCESO_DENEGADO"});
        errors.put("404", new String[]{"El recurso no existe", "Reserva con id 99 no existe", "NO_ENCONTRADO"});
        errors.put("409", new String[]{"Se viola una regla de negocio o hay un duplicado", "La sala ya está reservada en ese horario", "RN-01"});

        return openApi -> openApi.getPaths().forEach((path, item) -> item.readOperations().forEach(op -> {
            ApiResponses responses = op.getResponses();
            boolean isPublic = op.getSecurity() != null && op.getSecurity().isEmpty();
            errors.forEach((code, data) -> {
                if (responses.containsKey(code)) {
                    return;
                }
                if (isPublic && (code.equals("403") || code.equals("404") || code.equals("409"))) {
                    return;
                }
                responses.addApiResponse(code, errorResponse(data[0], data[1], data[2]));
            });
        }));
    }

    private static ApiResponse errorResponse(String description, String message, String code) {
        Map<String, Object> example = new LinkedHashMap<>();
        example.put("error", message);
        example.put("codigo", code);
        example.put("timestamp", "2030-06-03T13:00:00Z");
        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType("application/json", new MediaType()
                        .schema(new Schema<>().$ref(ERROR_REF))
                        .addExamples("ejemplo", new Example().value(example))));
    }

    // ---------- Grupos ----------

    @Bean
    public GroupedOpenApi allGroup() {
        return GroupedOpenApi.builder().group("0-todos").displayName("Toda la API")
                .pathsToMatch("/**").addOpenApiCustomizer(standardErrorResponses()).build();
    }

    @Bean
    public GroupedOpenApi authGroup() {
        return GroupedOpenApi.builder().group("1-autenticacion").displayName("Autenticación")
                .pathsToMatch("/auth/**").addOpenApiCustomizer(standardErrorResponses()).build();
    }

    @Bean
    public GroupedOpenApi catalogGroup() {
        return GroupedOpenApi.builder().group("2-catalogos").displayName("Catálogos")
                .pathsToMatch("/api/sedes/**", "/api/salas/**", "/api/equipamientos/**",
                        "/api/servicios-adicionales/**", "/api/planes/**")
                .addOpenApiCustomizer(standardErrorResponses()).build();
    }

    @Bean
    public GroupedOpenApi memberGroup() {
        return GroupedOpenApi.builder().group("3-miembros").displayName("Miembros")
                .pathsToMatch("/api/miembros/**").addOpenApiCustomizer(standardErrorResponses()).build();
    }

    @Bean
    public GroupedOpenApi bookingGroup() {
        return GroupedOpenApi.builder().group("4-reservas").displayName("Disponibilidad y reservas")
                .pathsToMatch("/api/disponibilidad/**", "/api/reservas/**")
                .addOpenApiCustomizer(standardErrorResponses()).build();
    }
}
