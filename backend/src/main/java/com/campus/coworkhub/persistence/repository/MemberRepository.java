package com.campus.coworkhub.persistence.repository;

import com.campus.coworkhub.persistence.entity.Member;
import com.campus.coworkhub.persistence.entity.MemberStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    /** Listado con filtros opcionales, sin N+1 (plan y cuenta en la misma consulta). */
    @EntityGraph(attributePaths = {"plan", "userAccount"})
    @Query("""
            select m from Member m
            where (:nombre is null or lower(m.fullName) like lower(concat('%', :nombre, '%')))
              and (:estado is null or m.status = :estado)
            order by m.id
            """)
    List<Member> search(@Param("nombre") String nombre, @Param("estado") MemberStatus estado);

    @EntityGraph(attributePaths = {"plan", "userAccount"})
    Optional<Member> findWithDetailsById(Long id);

    Optional<Member> findByUserAccountUsername(String username);

    boolean existsByDocumentNumber(String documentNumber);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByPlanId(Long planId);
}
