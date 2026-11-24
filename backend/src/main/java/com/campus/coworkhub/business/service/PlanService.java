package com.campus.coworkhub.business.service;

import com.campus.coworkhub.business.dto.catalog.PlanRequest;
import com.campus.coworkhub.business.dto.catalog.PlanResponse;

import java.util.List;

public interface PlanService {
    List<PlanResponse> findAll();
    PlanResponse findById(Long id);
    PlanResponse create(PlanRequest request);
    PlanResponse update(Long id, PlanRequest request);
    void delete(Long id);
}
