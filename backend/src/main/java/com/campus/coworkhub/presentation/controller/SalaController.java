package com.campus.coworkhub.presentation.controller;

import com.campus.coworkhub.business.dto.catalog.SalaRequest;
import com.campus.coworkhub.business.dto.catalog.SalaResponse;
import com.campus.coworkhub.business.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/salas")
@Tag(name = "Salas", description = "Catálogo de salas con su equipamiento (RF-01)")
public class SalaController {

    private final RoomService service;

    public SalaController(RoomService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar salas", description = "Acepta ?sedeId= para filtrar por sede")
    public List<SalaResponse> list(@Parameter(description = "Id de la sede", example = "1")
                                   @RequestParam(required = false) Long sedeId) {
        return service.findAll(sedeId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar una sala")
    public SalaResponse get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear sala (ADMIN)", description = "RN-11: el nombre es único dentro de la sede")
    public ResponseEntity<SalaResponse> create(@Valid @RequestBody SalaRequest request) {
        SalaResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/salas/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar sala (ADMIN)")
    public SalaResponse update(@PathVariable Long id, @Valid @RequestBody SalaRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar sala (ADMIN)", description = "RN-12: no se elimina una sala con reservas")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }
}
