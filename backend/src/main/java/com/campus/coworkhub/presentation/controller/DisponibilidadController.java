package com.campus.coworkhub.presentation.controller;

import com.campus.coworkhub.business.dto.catalog.SalaResponse;
import com.campus.coworkhub.business.service.AvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@RestController
@Validated
@RequestMapping("/api/disponibilidad")
@Tag(name = "Disponibilidad", description = "Salas libres de una sede (RF-03)")
public class DisponibilidadController {

    private final AvailabilityService service;

    public DisponibilidadController(AvailabilityService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Salas libres en una fecha y rango horario",
            description = "Devuelve las salas activas de la sede sin reservas PENDING o CONFIRMED que se crucen con el rango. "
                    + "Filtros opcionales: capacidadMinima y equipamientoIds (la sala debe tener todos)")
    public List<SalaResponse> available(
            @Parameter(example = "1") @RequestParam Long sedeId,
            @Parameter(example = "2030-06-03") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @Parameter(example = "10:00") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaInicio,
            @Parameter(example = "12:00") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaFin,
            @Parameter(example = "4") @RequestParam(required = false) @Positive Integer capacidadMinima,
            @Parameter(example = "1,2") @RequestParam(required = false) Set<Long> equipamientoIds) {
        return service.findAvailableRooms(sedeId, fecha, horaInicio, horaFin, capacidadMinima, equipamientoIds);
    }
}
