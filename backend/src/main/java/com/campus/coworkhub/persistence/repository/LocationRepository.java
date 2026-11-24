package com.campus.coworkhub.persistence.repository;

import com.campus.coworkhub.persistence.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {

    List<Location> findAllByOrderByNameAsc();
}
