package com.campus.coworkhub.security;

import com.campus.coworkhub.business.service.BookingService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * Reglas de "dueño del recurso" que se usan en las expresiones @PreAuthorize, por ejemplo:
 * {@code @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST') or @authz.isSelf(#id)")}.
 */
@Component("authz")
public class Authz {

    private final BookingService bookingService;

    public Authz(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /** true si el usuario actual es un MEMBER y el id pedido es el de su propio miembro. */
    public boolean isSelf(Long memberId) {
        return currentUser()
                .filter(AuthenticatedUser::isMember)
                .map(u -> u.memberId() != null && Objects.equals(u.memberId(), memberId))
                .orElse(false);
    }

    /**
     * true si el usuario actual es un MEMBER y la reserva es suya.
     * Si la reserva no existe devuelve true para que el servicio responda 404 en lugar de 403.
     */
    public boolean ownsBooking(Long bookingId) {
        return currentUser()
                .filter(AuthenticatedUser::isMember)
                .map(u -> bookingService.findOwnerMemberId(bookingId)
                        .map(ownerId -> Objects.equals(ownerId, u.memberId()))
                        .orElse(true))
                .orElse(false);
    }

    public static Optional<AuthenticatedUser> currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedUser user) {
            return Optional.of(user);
        }
        return Optional.empty();
    }
}
