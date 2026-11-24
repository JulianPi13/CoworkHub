package com.campus.coworkhub.presentation.controller;

import com.campus.coworkhub.business.dto.catalog.PlanRequest;
import com.campus.coworkhub.business.dto.catalog.PlanResponse;
import com.campus.coworkhub.business.service.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/planes")
@Tag(name = "Planes", description = "Catálogo de planes de membresía (RF-01)")
public class PlanController {

    private final PlanService service;

    public PlanController(PlanService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar planes")
    public List<PlanResponse> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un plan")
    public PlanResponse get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear plan (ADMIN)")
    public ResponseEntity<PlanResponse> create(@Valid @RequestBody PlanRequest request) {
        PlanResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/planes/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar plan (ADMIN)")
    public PlanResponse update(@PathVariable Long id, @Valid @RequestBody PlanRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar plan (ADMIN)", description = "RN-12: no se elimina un plan con miembros")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }
}
