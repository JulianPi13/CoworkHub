package com.campus.coworkhub.business.service;

import com.campus.coworkhub.business.dto.catalog.SedeRequest;
import com.campus.coworkhub.business.dto.catalog.SedeResponse;

import java.util.List;

public interface LocationService {
    List<SedeResponse> findAll();
    SedeResponse findById(Long id);
    SedeResponse create(SedeRequest request);
    SedeResponse update(Long id, SedeRequest request);
    void delete(Long id);
}
