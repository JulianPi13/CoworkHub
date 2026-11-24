package com.campus.coworkhub.presentation.controller;

import com.campus.coworkhub.business.dto.catalog.SedeRequest;
import com.campus.coworkhub.business.dto.catalog.SedeResponse;
import com.campus.coworkhub.business.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/sedes")
@Tag(name = "Sedes", description = "Catálogo de sedes (RF-01). Consultar: autenticado. Crear, modificar y eliminar: ADMIN")
public class SedeController {

    private final LocationService service;

    public SedeController(LocationService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar sedes")
    public List<SedeResponse> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar una sede")
    public SedeResponse get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear sede (ADMIN)")
    public ResponseEntity<SedeResponse> create(@Valid @RequestBody SedeRequest request) {
        SedeResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/sedes/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar sede (ADMIN)")
    public SedeResponse update(@PathVariable Long id, @Valid @RequestBody SedeRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar sede (ADMIN)", description = "RN-12: no se elimina una sede con salas")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }
}
