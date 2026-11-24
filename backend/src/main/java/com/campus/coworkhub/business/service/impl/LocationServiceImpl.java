package com.campus.coworkhub.business.service.impl;

import com.campus.coworkhub.business.dto.catalog.SedeRequest;
import com.campus.coworkhub.business.dto.catalog.SedeResponse;
import com.campus.coworkhub.business.exception.BusinessRuleException;
import com.campus.coworkhub.business.exception.BusinessRules;
import com.campus.coworkhub.business.exception.InvalidRequestException;
import com.campus.coworkhub.business.exception.ResourceNotFoundException;
import com.campus.coworkhub.business.mapper.DtoMapper;
import com.campus.coworkhub.business.service.LocationService;
import com.campus.coworkhub.persistence.entity.Location;
import com.campus.coworkhub.persistence.repository.LocationRepository;
import com.campus.coworkhub.persistence.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;
    private final RoomRepository roomRepository;

    public LocationServiceImpl(LocationRepository locationRepository, RoomRepository roomRepository) {
        this.locationRepository = locationRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    public List<SedeResponse> findAll() {
        return locationRepository.findAllByOrderByNameAsc().stream().map(DtoMapper::toResponse).toList();
    }

    @Override
    public SedeResponse findById(Long id) {
        return DtoMapper.toResponse(getOrThrow(id));
    }

    @Override
    @Transactional
    public SedeResponse create(SedeRequest request) {
        Location location = new Location();
        apply(location, request);
        return DtoMapper.toResponse(locationRepository.save(location));
    }

    @Override
    @Transactional
    public SedeResponse update(Long id, SedeRequest request) {
        Location location = getOrThrow(id);
        apply(location, request);
        return DtoMapper.toResponse(location);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Location location = getOrThrow(id);
        if (roomRepository.existsByLocationId(id)) {
            throw new BusinessRuleException(BusinessRules.RN_12, "No se puede eliminar la sede: tiene salas registradas");
        }
        locationRepository.delete(location);
    }

    private void apply(Location location, SedeRequest request) {
        if (!request.horaApertura().isBefore(request.horaCierre())) {
            throw new InvalidRequestException("La hora de apertura debe ser anterior a la hora de cierre");
        }
        location.setName(request.nombre().trim());
        location.setCity(request.ciudad().trim());
        location.setAddress(request.direccion().trim());
        location.setOpeningTime(request.horaApertura());
        location.setClosingTime(request.horaCierre());
    }

    private Location getOrThrow(Long id) {
        return locationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Sede", id));
    }
}
