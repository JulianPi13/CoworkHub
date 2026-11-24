package com.campus.coworkhub.persistence.repository;

import com.campus.coworkhub.persistence.entity.Booking;
import com.campus.coworkhub.persistence.entity.BookingStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // ---------- Consultas con EntityGraph (evitan el problema N+1 en los listados) ----------

    @EntityGraph(attributePaths = {"room", "room.location", "member", "items", "items.service"})
    Optional<Booking> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"room", "member", "items", "items.service"})
    List<Booking> findByMemberIdOrderByStartTimeDesc(Long memberId);

    @EntityGraph(attributePaths = {"room", "member", "items", "items.service"})
    List<Booking> findByStatusOrderByStartTimeDesc(BookingStatus status);

    @EntityGraph(attributePaths = {"room", "member", "items", "items.service"})
    List<Booking> findByRoomIdAndStartTimeGreaterThanEqualAndStartTimeLessThanOrderByStartTime(
            Long roomId, LocalDateTime from, LocalDateTime to);

    @EntityGraph(attributePaths = {"room", "member", "items", "items.service"})
    List<Booking> findAllByOrderByStartTimeDesc();

    // ---------- Reglas de negocio ----------

    /** RN-01: hay cruce si (inicioExistente < finNuevo) y (finExistente > inicioNuevo). Si una termina justo cuando empieza la otra, no se cruzan. */
    @Query("""
            select count(b) > 0 from Booking b
            where b.room.id = :roomId
              and b.status in :statuses
              and b.startTime < :end
              and b.endTime > :start
            """)
    boolean existsOverlap(@Param("roomId") Long roomId,
                          @Param("start") LocalDateTime start,
                          @Param("end") LocalDateTime end,
                          @Param("statuses") Collection<BookingStatus> statuses);

    /** RF-03: ids de las salas ocupadas en una sede dentro de un rango horario. */
    @Query("""
            select distinct b.room.id from Booking b
            where b.room.location.id = :locationId
              and b.status in :statuses
              and b.startTime < :end
              and b.endTime > :start
            """)
    List<Long> findBusyRoomIds(@Param("locationId") Long locationId,
                               @Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end,
                               @Param("statuses") Collection<BookingStatus> statuses);

    /** RN-06: reservas activas futuras de un miembro. */
    long countByMemberIdAndStatusInAndStartTimeAfter(Long memberId, Collection<BookingStatus> statuses, LocalDateTime now);

    /** RN-07 / RF-10: reservas de un miembro que empiezan dentro de un mes. */
    List<Booking> findByMemberIdAndStartTimeGreaterThanEqualAndStartTimeLessThan(
            Long memberId, LocalDateTime from, LocalDateTime to);

    /** Id del miembro dueño de una reserva (para validar que un MEMBER solo acceda a las suyas). */
    @Query("select b.member.id from Booking b where b.id = :id")
    Optional<Long> findMemberIdById(@Param("id") Long id);

    boolean existsByRoomId(Long roomId);

    boolean existsByMemberId(Long memberId);
}
