package com.campus.coworkhub.presentation.controller;

import com.campus.coworkhub.business.dto.catalog.ServicioAdicionalRequest;
import com.campus.coworkhub.business.dto.catalog.ServicioAdicionalResponse;
import com.campus.coworkhub.business.service.AdditionalServiceCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/servicios-adicionales")
@Tag(name = "Servicios adicionales", description = "Catálogo de servicios que se agregan a las reservas (RF-01)")
public class ServicioAdicionalController {

    private final AdditionalServiceCatalogService service;

    public ServicioAdicionalController(AdditionalServiceCatalogService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar servicios adicionales")
    public List<ServicioAdicionalResponse> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar un servicio adicional")
    public ServicioAdicionalResponse get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear servicio adicional (ADMIN)")
    public ResponseEntity<ServicioAdicionalResponse> create(@Valid @RequestBody ServicioAdicionalRequest request) {
        ServicioAdicionalResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/servicios-adicionales/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar servicio adicional (ADMIN)",
            description = "Cambiar el precio no modifica las reservas existentes (guardan appliedUnitPrice)")
    public ServicioAdicionalResponse update(@PathVariable Long id, @Valid @RequestBody ServicioAdicionalRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar servicio adicional (ADMIN)", description = "RN-12: no se elimina si ya se usó")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }
}
