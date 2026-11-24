package com.campus.coworkhub.persistence.repository;

import com.campus.coworkhub.persistence.entity.BookingItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingItemRepository extends JpaRepository<BookingItem, Long> {

    boolean existsByServiceId(Long serviceId);
}
