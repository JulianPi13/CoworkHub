package com.campus.coworkhub.business.service.impl;

import com.campus.coworkhub.business.dto.catalog.SalaRequest;
import com.campus.coworkhub.business.dto.catalog.SalaResponse;
import com.campus.coworkhub.business.exception.BusinessRuleException;
import com.campus.coworkhub.business.exception.BusinessRules;
import com.campus.coworkhub.business.exception.ResourceNotFoundException;
import com.campus.coworkhub.business.mapper.DtoMapper;
import com.campus.coworkhub.business.service.RoomService;
import com.campus.coworkhub.persistence.entity.Equipment;
import com.campus.coworkhub.persistence.entity.Location;
import com.campus.coworkhub.persistence.entity.Room;
import com.campus.coworkhub.persistence.repository.BookingRepository;
import com.campus.coworkhub.persistence.repository.EquipmentRepository;
import com.campus.coworkhub.persistence.repository.LocationRepository;
import com.campus.coworkhub.persistence.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final LocationRepository locationRepository;
    private final EquipmentRepository equipmentRepository;
    private final BookingRepository bookingRepository;

    public RoomServiceImpl(RoomRepository roomRepository, LocationRepository locationRepository,
                           EquipmentRepository equipmentRepository, BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.locationRepository = locationRepository;
        this.equipmentRepository = equipmentRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public List<SalaResponse> findAll(Long locationId) {
        List<Room> rooms = Optional.ofNullable(locationId)
                .map(roomRepository::findByLocationIdOrderByIdAsc)
                .orElseGet(roomRepository::findAllByOrderByIdAsc);
        return rooms.stream().map(DtoMapper::toResponse).toList();
    }

    @Override
    public SalaResponse findById(Long id) {
        return DtoMapper.toResponse(roomRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sala", id)));
    }

    @Override
    @Transactional
    public SalaResponse create(SalaRequest request) {
        if (roomRepository.existsByLocationIdAndNameIgnoreCase(request.sedeId(), request.nombre().trim())) {
            throw duplicateName(request.nombre());
        }
        Room room = new Room();
        apply(room, request);
        return DtoMapper.toResponse(roomRepository.save(room));
    }

    @Override
    @Transactional
    public SalaResponse update(Long id, SalaRequest request) {
        Room room = roomRepository.findWithDetailsById(id).orElseThrow(() -> new ResourceNotFoundException("Sala", id));
        if (roomRepository.existsByLocationIdAndNameIgnoreCaseAndIdNot(request.sedeId(), request.nombre().trim(), id)) {
            throw duplicateName(request.nombre());
        }
        apply(room, request);
        return DtoMapper.toResponse(room);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Sala", id));
        if (bookingRepository.existsByRoomId(id)) {
            throw new BusinessRuleException(BusinessRules.RN_12, "No se puede eliminar la sala: tiene reservas registradas");
        }
        roomRepository.delete(room);
    }

    private void apply(Room room, SalaRequest request) {
        Location location = locationRepository.findById(request.sedeId())
                .orElseThrow(() -> new ResourceNotFoundException("Sede", request.sedeId()));
        room.setName(request.nombre().trim());
        room.setType(request.tipo());
        room.setCapacity(request.capacidad());
        room.setHourlyRate(request.tarifaHora());
        room.setActive(Optional.ofNullable(request.activa()).orElse(true));
        room.setLocation(location);
        room.setEquipment(resolveEquipment(request.equipamientoIds()));
    }

    private Set<Equipment> resolveEquipment(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new LinkedHashSet<>();
        }
        List<Equipment> found = equipmentRepository.findAllById(ids);
        if (found.size() != ids.size()) {
            Set<Long> foundIds = new HashSet<>(found.stream().map(Equipment::getId).toList());
            Long missing = ids.stream().filter(i -> !foundIds.contains(i)).findFirst().orElse(null);
            throw new ResourceNotFoundException("Equipamiento", missing);
        }
        return new LinkedHashSet<>(found);
    }

    private BusinessRuleException duplicateName(String name) {
        return new BusinessRuleException(BusinessRules.RN_11, "Ya existe una sala llamada '" + name.trim() + "' en esa sede");
    }
}
