package com.campus.coworkhub.presentation.controller;

import com.campus.coworkhub.business.dto.booking.ReservaRequest;
import com.campus.coworkhub.business.dto.booking.ReservaResponse;
import com.campus.coworkhub.business.dto.booking.ServicioReservaRequest;
import com.campus.coworkhub.business.service.BookingService;
import com.campus.coworkhub.persistence.entity.BookingStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservas")
@Tag(name = "Reservas", description = "Creación, consulta y ciclo de vida de las reservas (RF-04 a RF-09)")
public class ReservaController {

    private static final String STAFF = "hasAnyRole('ADMIN','RECEPTIONIST')";
    private static final String STAFF_OR_OWNER = STAFF + " or @authz.ownsBooking(#id)";

    private final BookingService service;

    public ReservaController(BookingService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize(STAFF + " or @authz.isSelf(#request.miembroId())")
    @Operation(summary = "Crear reserva (RF-04)",
            description = "Crea la reserva con sus servicios y devuelve el costo calculado. Aplica RN-01 a RN-07. "
                    + "Un MEMBER solo puede reservar a su propio nombre")
    public ResponseEntity<ReservaResponse> create(@Valid @RequestBody ReservaRequest request) {
        ReservaResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/reservas/" + created.id())).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize(STAFF_OR_OWNER)
    @Operation(summary = "Consultar una reserva (RF-05)", description = "Un MEMBER solo puede consultar las suyas (403)")
    public ReservaResponse get(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping
    @PreAuthorize(STAFF + " or (#miembroId != null and @authz.isSelf(#miembroId))")
    @Operation(summary = "Listar reservas (RF-05)",
            description = "Usar un filtro: ?miembroId= · ?salaId=&fecha= · ?estado=. Un MEMBER solo puede usar su propio miembroId")
    public List<ReservaResponse> list(
            @Parameter(example = "1") @RequestParam(required = false) Long miembroId,
            @Parameter(example = "1") @RequestParam(required = false) Long salaId,
            @Parameter(example = "2030-06-03") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @Parameter(example = "CONFIRMED") @RequestParam(required = false) BookingStatus estado) {
        return service.search(miembroId, salaId, fecha, estado);
    }

    @PostMapping("/{id}/servicios")
    @PreAuthorize(STAFF_OR_OWNER)
    @Operation(summary = "Agregar un servicio (RF-06)", description = "Solo con la reserva en PENDING (RN-10). Recalcula el costo")
    public ReservaResponse addService(@PathVariable Long id, @Valid @RequestBody ServicioReservaRequest request) {
        return service.addService(id, request);
    }

    @DeleteMapping("/{id}/servicios/{servicioId}")
    @PreAuthorize(STAFF_OR_OWNER)
    @Operation(summary = "Quitar un servicio (RF-06)", description = "Solo con la reserva en PENDING (RN-10). Recalcula el costo")
    public ReservaResponse removeService(@PathVariable Long id, @PathVariable Long servicioId) {
        return service.removeService(id, servicioId);
    }

    @PostMapping("/{id}/confirmar")
    @PreAuthorize(STAFF)
    @Operation(summary = "Confirmar reserva (RF-07)", description = "PENDING → CONFIRMED (RN-09)")
    public ReservaResponse confirm(@PathVariable Long id) {
        return service.confirm(id);
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize(STAFF_OR_OWNER)
    @Operation(summary = "Cancelar reserva (RF-08)",
            description = "RN-08: con 24 h o más sin cargo; entre 24 h y 2 h cobra el 50 % del costo de la sala; "
                    + "con menos de 2 h o ya iniciada no se puede (409). El cargo se devuelve en cargoCancelacion")
    public ReservaResponse cancel(@PathVariable Long id) {
        return service.cancel(id);
    }

    @PostMapping("/{id}/completar")
    @PreAuthorize(STAFF)
    @Operation(summary = "Completar reserva (RF-09)", description = "CONFIRMED → COMPLETED (RN-09)")
    public ReservaResponse complete(@PathVariable Long id) {
        return service.complete(id);
    }
}
