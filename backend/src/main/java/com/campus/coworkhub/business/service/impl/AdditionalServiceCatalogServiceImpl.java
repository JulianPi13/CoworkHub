package com.campus.coworkhub.business.service.impl;

import com.campus.coworkhub.business.dto.catalog.ServicioAdicionalRequest;
import com.campus.coworkhub.business.dto.catalog.ServicioAdicionalResponse;
import com.campus.coworkhub.business.exception.BusinessRuleException;
import com.campus.coworkhub.business.exception.BusinessRules;
import com.campus.coworkhub.business.exception.ResourceNotFoundException;
import com.campus.coworkhub.business.mapper.DtoMapper;
import com.campus.coworkhub.business.service.AdditionalServiceCatalogService;
import com.campus.coworkhub.persistence.entity.AdditionalService;
import com.campus.coworkhub.persistence.repository.AdditionalServiceRepository;
import com.campus.coworkhub.persistence.repository.BookingItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AdditionalServiceCatalogServiceImpl implements AdditionalServiceCatalogService {

    private final AdditionalServiceRepository serviceRepository;
    private final BookingItemRepository bookingItemRepository;

    public AdditionalServiceCatalogServiceImpl(AdditionalServiceRepository serviceRepository,
                                               BookingItemRepository bookingItemRepository) {
        this.serviceRepository = serviceRepository;
        this.bookingItemRepository = bookingItemRepository;
    }

    @Override
    public List<ServicioAdicionalResponse> findAll() {
        return serviceRepository.findAllByOrderByNameAsc().stream().map(DtoMapper::toResponse).toList();
    }

    @Override
    public ServicioAdicionalResponse findById(Long id) {
        return DtoMapper.toResponse(getOrThrow(id));
    }

    @Override
    @Transactional
    public ServicioAdicionalResponse create(ServicioAdicionalRequest request) {
        String name = request.nombre().trim();
        if (serviceRepository.existsByNameIgnoreCase(name)) {
            throw duplicate(name);
        }
        AdditionalService service = new AdditionalService();
        service.setName(name);
        service.setUnitPrice(request.precioUnitario());
        return DtoMapper.toResponse(serviceRepository.save(service));
    }

    /** Cambiar el precio no altera las reservas existentes: cada detalle guarda su appliedUnitPrice (RN-07). */
    @Override
    @Transactional
    public ServicioAdicionalResponse update(Long id, ServicioAdicionalRequest request) {
        AdditionalService service = getOrThrow(id);
        String name = request.nombre().trim();
        if (serviceRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw duplicate(name);
        }
        service.setName(name);
        service.setUnitPrice(request.precioUnitario());
        return DtoMapper.toResponse(service);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        AdditionalService service = getOrThrow(id);
        if (bookingItemRepository.existsByServiceId(id)) {
            throw new BusinessRuleException(BusinessRules.RN_12, "No se puede eliminar el servicio: ya se usó en reservas");
        }
        serviceRepository.delete(service);
    }

    private AdditionalService getOrThrow(Long id) {
        return serviceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Servicio adicional", id));
    }

    private BusinessRuleException duplicate(String name) {
        return new BusinessRuleException(BusinessRules.RN_11, "Ya existe un servicio adicional llamado '" + name + "'");
    }
}
