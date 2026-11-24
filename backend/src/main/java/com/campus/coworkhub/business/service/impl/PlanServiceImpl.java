package com.campus.coworkhub.business.service.impl;

import com.campus.coworkhub.business.dto.catalog.PlanRequest;
import com.campus.coworkhub.business.dto.catalog.PlanResponse;
import com.campus.coworkhub.business.exception.BusinessRuleException;
import com.campus.coworkhub.business.exception.BusinessRules;
import com.campus.coworkhub.business.exception.ResourceNotFoundException;
import com.campus.coworkhub.business.mapper.DtoMapper;
import com.campus.coworkhub.business.service.PlanService;
import com.campus.coworkhub.persistence.entity.MembershipPlan;
import com.campus.coworkhub.persistence.repository.MemberRepository;
import com.campus.coworkhub.persistence.repository.MembershipPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PlanServiceImpl implements PlanService {

    private final MembershipPlanRepository planRepository;
    private final MemberRepository memberRepository;

    public PlanServiceImpl(MembershipPlanRepository planRepository, MemberRepository memberRepository) {
        this.planRepository = planRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    public List<PlanResponse> findAll() {
        return planRepository.findAllByOrderByNameAsc().stream().map(DtoMapper::toResponse).toList();
    }

    @Override
    public PlanResponse findById(Long id) {
        return DtoMapper.toResponse(getOrThrow(id));
    }

    @Override
    @Transactional
    public PlanResponse create(PlanRequest request) {
        String name = request.nombre().trim();
        if (planRepository.existsByNameIgnoreCase(name)) {
            throw duplicate(name);
        }
        MembershipPlan plan = new MembershipPlan();
        apply(plan, request);
        return DtoMapper.toResponse(planRepository.save(plan));
    }

    @Override
    @Transactional
    public PlanResponse update(Long id, PlanRequest request) {
        MembershipPlan plan = getOrThrow(id);
        if (planRepository.existsByNameIgnoreCaseAndIdNot(request.nombre().trim(), id)) {
            throw duplicate(request.nombre().trim());
        }
        apply(plan, request);
        return DtoMapper.toResponse(plan);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        MembershipPlan plan = getOrThrow(id);
        if (memberRepository.existsByPlanId(id)) {
            throw new BusinessRuleException(BusinessRules.RN_12, "No se puede eliminar el plan: tiene miembros asignados");
        }
        planRepository.delete(plan);
    }

    private void apply(MembershipPlan plan, PlanRequest request) {
        plan.setName(request.nombre().trim());
        plan.setIncludedHoursPerMonth(request.horasIncluidasMes());
        plan.setOverageDiscount(request.descuentoExcedente());
        plan.setMaxActiveBookings(request.maxReservasActivas());
    }

    private MembershipPlan getOrThrow(Long id) {
        return planRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Plan", id));
    }

    private BusinessRuleException duplicate(String name) {
        return new BusinessRuleException(BusinessRules.RN_11, "Ya existe un plan llamado '" + name + "'");
    }
}
