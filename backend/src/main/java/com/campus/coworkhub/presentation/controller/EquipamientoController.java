package com.campus.coworkhub.presentation.controller;

import com.campus.coworkhub.business.dto.catalog.EquipamientoRequest;
import com.campus.coworkhub.business.dto.catalog.EquipamientoResponse;
import com.campus.coworkhub.business.service.EquipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/equipamientos")
@Tag(name = "Equipamientos", description = "Catálogo de equipamiento de las salas (RF-01)")
public class EquipamientoController {

    private final EquipmentService service;

    public EquipamientoController(EquipmentService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar equipamientos")
    public List<EquipamientoResponse> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un equipamiento")
    public EquipamientoResponse get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear equipamiento (ADMIN)")
    public ResponseEntity<EquipamientoResponse> create(@Valid @RequestBody EquipamientoRequest request) {
        EquipamientoResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/equipamientos/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar equipamiento (ADMIN)")
    public EquipamientoResponse update(@PathVariable Long id, @Valid @RequestBody EquipamientoRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar equipamiento (ADMIN)", description = "RN-12: no se elimina si una sala lo usa")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }
}
