package com.campus.coworkhub.business.service;

import com.campus.coworkhub.business.dto.catalog.ServicioAdicionalRequest;
import com.campus.coworkhub.business.dto.catalog.ServicioAdicionalResponse;

import java.util.List;

/** Catálogo de servicios adicionales. */
public interface AdditionalServiceCatalogService {
    List<ServicioAdicionalResponse> findAll();
    ServicioAdicionalResponse findById(Long id);
    ServicioAdicionalResponse create(ServicioAdicionalRequest request);
    ServicioAdicionalResponse update(Long id, ServicioAdicionalRequest request);
    void delete(Long id);
}
