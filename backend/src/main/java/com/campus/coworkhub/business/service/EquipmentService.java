package com.campus.coworkhub.business.service;

import com.campus.coworkhub.business.dto.catalog.EquipamientoRequest;
import com.campus.coworkhub.business.dto.catalog.EquipamientoResponse;

import java.util.List;

public interface EquipmentService {
    List<EquipamientoResponse> findAll();
    EquipamientoResponse findById(Long id);
    EquipamientoResponse create(EquipamientoRequest request);
    EquipamientoResponse update(Long id, EquipamientoRequest request);
    void delete(Long id);
}
