package com.campus.coworkhub.presentation.controller;

import com.campus.coworkhub.business.dto.member.ConsumoResponse;
import com.campus.coworkhub.business.dto.member.MiembroCreateRequest;
import com.campus.coworkhub.business.dto.member.MiembroResponse;
import com.campus.coworkhub.business.dto.member.MiembroUpdateRequest;
import com.campus.coworkhub.business.service.MemberService;
import com.campus.coworkhub.persistence.entity.MemberStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Clock;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/miembros")
@Tag(name = "Miembros", description = "Gestión de miembros y su consumo (RF-02, RF-10)")
public class MiembroController {

    private static final String STAFF = "hasAnyRole('ADMIN','RECEPTIONIST')";

    private final MemberService service;
    private final Clock clock;

    public MiembroController(MemberService service, Clock clock) {
        this.service = service;
        this.clock = clock;
    }

    @GetMapping
    @PreAuthorize(STAFF)
    @Operation(summary = "Listar miembros (ADMIN, RECEPTIONIST)", description = "Filtros opcionales por nombre (contiene) y estado")
    public List<MiembroResponse> list(
            @Parameter(example = "ana") @RequestParam(required = false) String nombre,
            @Parameter(example = "SUSPENDED") @RequestParam(required = false) MemberStatus estado) {
        return service.search(nombre, estado);
    }

    @GetMapping("/{id}")
    @PreAuthorize(STAFF + " or @authz.isSelf(#id)")
    @Operation(summary = "Consultar un miembro", description = "ADMIN y RECEPTIONIST cualquiera; MEMBER solo el suyo")
    public MiembroResponse get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @PreAuthorize(STAFF)
    @Operation(summary = "Registrar miembro y su usuario (ADMIN, RECEPTIONIST)",
            description = "Crea el Member y su UserAccount con rol MEMBER. RN-11: documento, email y usuario únicos")
    public ResponseEntity<MiembroResponse> create(@Valid @RequestBody MiembroCreateRequest request) {
        MiembroResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/miembros/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize(STAFF)
    @Operation(summary = "Actualizar nombre, email y plan (ADMIN, RECEPTIONIST)")
    public MiembroResponse update(@PathVariable Long id, @Valid @RequestBody MiembroUpdateRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/suspender")
    @PreAuthorize(STAFF)
    @Operation(summary = "Suspender miembro (ADMIN, RECEPTIONIST)", description = "Un miembro suspendido no puede reservar (RN-05)")
    public MiembroResponse suspend(@PathVariable Long id) {
        return service.changeStatus(id, MemberStatus.SUSPENDED);
    }

    @PatchMapping("/{id}/activar")
    @PreAuthorize(STAFF)
    @Operation(summary = "Reactivar miembro (ADMIN, RECEPTIONIST)")
    public MiembroResponse activate(@PathVariable Long id) {
        return service.changeStatus(id, MemberStatus.ACTIVE);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(STAFF)
    @Operation(summary = "Eliminar miembro (ADMIN, RECEPTIONIST)", description = "RN-12: no se elimina un miembro con reservas")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/consumo")
    @PreAuthorize(STAFF + " or @authz.isSelf(#id)")
    @Operation(summary = "Consumo del mes (RF-10)",
            description = "Horas incluidas, usadas, restantes y excedentes. Si no se envía mes, usa el mes actual")
    public ConsumoResponse consumption(
            @PathVariable Long id,
            @Parameter(description = "Mes con formato yyyy-MM", example = "2030-06")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes) {
        return service.monthlyConsumption(id, Optional.ofNullable(mes).orElseGet(() -> YearMonth.now(clock)));
    }
}
