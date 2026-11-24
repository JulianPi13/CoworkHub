package com.campus.coworkhub.business.service;

import com.campus.coworkhub.business.dto.catalog.SalaRequest;
import com.campus.coworkhub.business.dto.catalog.SalaResponse;

import java.util.List;

public interface RoomService {
    List<SalaResponse> findAll(Long locationId);
    SalaResponse findById(Long id);
    SalaResponse create(SalaRequest request);
    SalaResponse update(Long id, SalaRequest request);
    void delete(Long id);
}
