package com.campus.coworkhub.persistence.repository;

import com.campus.coworkhub.persistence.entity.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    /** Listado sin N+1: trae la sede y el equipamiento en la misma consulta. */
    @EntityGraph(attributePaths = {"location", "equipment"})
    List<Room> findAllByOrderByIdAsc();

    @EntityGraph(attributePaths = {"location", "equipment"})
    List<Room> findByLocationIdOrderByIdAsc(Long locationId);

    @EntityGraph(attributePaths = {"location", "equipment"})
    List<Room> findByLocationIdAndActiveTrueAndCapacityGreaterThanEqual(Long locationId, Integer capacity);

    @EntityGraph(attributePaths = {"location", "equipment"})
    Optional<Room> findWithDetailsById(Long id);

    boolean existsByLocationIdAndNameIgnoreCase(Long locationId, String name);

    boolean existsByLocationIdAndNameIgnoreCaseAndIdNot(Long locationId, String name, Long id);

    boolean existsByLocationId(Long locationId);

    boolean existsByEquipmentId(Long equipmentId);

    /**
     * Bloquea la fila de la sala (SELECT ... FOR UPDATE) durante la transacción de la reserva.
     * Así dos solicitudes simultáneas sobre la misma sala se atienden de a una (RN-01).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Room r join fetch r.location where r.id = :id")
    Optional<Room> findByIdForUpdate(@Param("id") Long id);
}
