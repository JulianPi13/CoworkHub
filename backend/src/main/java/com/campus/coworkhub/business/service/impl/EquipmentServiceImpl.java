package com.campus.coworkhub.business.service.impl;

import com.campus.coworkhub.business.dto.catalog.EquipamientoRequest;
import com.campus.coworkhub.business.dto.catalog.EquipamientoResponse;
import com.campus.coworkhub.business.exception.BusinessRuleException;
import com.campus.coworkhub.business.exception.BusinessRules;
import com.campus.coworkhub.business.exception.ResourceNotFoundException;
import com.campus.coworkhub.business.mapper.DtoMapper;
import com.campus.coworkhub.business.service.EquipmentService;
import com.campus.coworkhub.persistence.entity.Equipment;
import com.campus.coworkhub.persistence.repository.EquipmentRepository;
import com.campus.coworkhub.persistence.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final RoomRepository roomRepository;

    public EquipmentServiceImpl(EquipmentRepository equipmentRepository, RoomRepository roomRepository) {
        this.equipmentRepository = equipmentRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    public List<EquipamientoResponse> findAll() {
        return equipmentRepository.findAllByOrderByNameAsc().stream().map(DtoMapper::toResponse).toList();
    }

    @Override
    public EquipamientoResponse findById(Long id) {
        return DtoMapper.toResponse(getOrThrow(id));
    }

    @Override
    @Transactional
    public EquipamientoResponse create(EquipamientoRequest request) {
        String name = request.nombre().trim();
        if (equipmentRepository.existsByNameIgnoreCase(name)) {
            throw duplicate(name);
        }
        Equipment equipment = new Equipment();
        equipment.setName(name);
        return DtoMapper.toResponse(equipmentRepository.save(equipment));
    }

    @Override
    @Transactional
    public EquipamientoResponse update(Long id, EquipamientoRequest request) {
        Equipment equipment = getOrThrow(id);
        String name = request.nombre().trim();
        if (equipmentRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw duplicate(name);
        }
        equipment.setName(name);
        return DtoMapper.toResponse(equipment);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Equipment equipment = getOrThrow(id);
        if (roomRepository.existsByEquipmentId(id)) {
            throw new BusinessRuleException(BusinessRules.RN_12, "No se puede eliminar el equipamiento: está asignado a una o más salas");
        }
        equipmentRepository.delete(equipment);
    }

    private Equipment getOrThrow(Long id) {
        return equipmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Equipamiento", id));
    }

    private BusinessRuleException duplicate(String name) {
        return new BusinessRuleException(BusinessRules.RN_11, "Ya existe un equipamiento llamado '" + name + "'");
    }
}
