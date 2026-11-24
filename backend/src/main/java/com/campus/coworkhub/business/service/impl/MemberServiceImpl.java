package com.campus.coworkhub.business.service.impl;

import com.campus.coworkhub.business.booking.BookingCostCalculator;
import com.campus.coworkhub.business.dto.member.ConsumoResponse;
import com.campus.coworkhub.business.dto.member.MiembroCreateRequest;
import com.campus.coworkhub.business.dto.member.MiembroResponse;
import com.campus.coworkhub.business.dto.member.MiembroUpdateRequest;
import com.campus.coworkhub.business.exception.BusinessRuleException;
import com.campus.coworkhub.business.exception.BusinessRules;
import com.campus.coworkhub.business.exception.ResourceNotFoundException;
import com.campus.coworkhub.business.mapper.DtoMapper;
import com.campus.coworkhub.business.service.MemberService;
import com.campus.coworkhub.persistence.entity.*;
import com.campus.coworkhub.persistence.repository.BookingRepository;
import com.campus.coworkhub.persistence.repository.MemberRepository;
import com.campus.coworkhub.persistence.repository.MembershipPlanRepository;
import com.campus.coworkhub.persistence.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MembershipPlanRepository planRepository;
    private final UserAccountRepository userAccountRepository;
    private final BookingRepository bookingRepository;
    private final BookingCostCalculator costCalculator;
    private final PasswordEncoder passwordEncoder;

    public MemberServiceImpl(MemberRepository memberRepository, MembershipPlanRepository planRepository,
                             UserAccountRepository userAccountRepository, BookingRepository bookingRepository,
                             BookingCostCalculator costCalculator, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.planRepository = planRepository;
        this.userAccountRepository = userAccountRepository;
        this.bookingRepository = bookingRepository;
        this.costCalculator = costCalculator;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<MiembroResponse> search(String nombre, MemberStatus estado) {
        String filter = Optional.ofNullable(nombre).map(String::trim).filter(s -> !s.isEmpty()).orElse(null);
        return memberRepository.search(filter, estado).stream().map(DtoMapper::toResponse).toList();
    }

    @Override
    public MiembroResponse findById(Long id) {
        return DtoMapper.toResponse(getOrThrow(id));
    }

    /** RF-02: registra el miembro y su cuenta (rol MEMBER) en la misma transacción. */
    @Override
    @Transactional
    public MiembroResponse create(MiembroCreateRequest request) {
        String document = request.documento().trim();
        String email = request.email().trim().toLowerCase();
        String username = request.usuario().trim();
        if (memberRepository.existsByDocumentNumber(document)) {
            throw new BusinessRuleException(BusinessRules.RN_11, "Ya existe un miembro con el documento " + document);
        }
        if (memberRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessRuleException(BusinessRules.RN_11, "Ya existe un miembro con el email " + email);
        }
        if (userAccountRepository.existsByUsername(username)) {
            throw new BusinessRuleException(BusinessRules.RN_11, "El usuario '" + username + "' ya está en uso");
        }

        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(request.contrasena()));
        account.setRole(Role.MEMBER);

        Member member = new Member();
        member.setDocumentNumber(document);
        member.setFullName(request.nombreCompleto().trim());
        member.setEmail(email);
        member.setStatus(MemberStatus.ACTIVE);
        member.setPlan(getPlan(request.planId()));
        member.setUserAccount(account); // cascade: se guarda la cuenta con el miembro

        return DtoMapper.toResponse(memberRepository.save(member));
    }

    @Override
    @Transactional
    public MiembroResponse update(Long id, MiembroUpdateRequest request) {
        Member member = getOrThrow(id);
        String email = request.email().trim().toLowerCase();
        if (memberRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new BusinessRuleException(BusinessRules.RN_11, "Ya existe un miembro con el email " + email);
        }
        member.setFullName(request.nombreCompleto().trim());
        member.setEmail(email);
        member.setPlan(getPlan(request.planId()));
        return DtoMapper.toResponse(member);
    }

    @Override
    @Transactional
    public MiembroResponse changeStatus(Long id, MemberStatus status) {
        Member member = getOrThrow(id);
        member.setStatus(status);
        return DtoMapper.toResponse(member);
    }

    /** RN-12: no se elimina un miembro con reservas. Su cuenta se elimina con él (orphanRemoval). */
    @Override
    @Transactional
    public void delete(Long id) {
        Member member = getOrThrow(id);
        if (bookingRepository.existsByMemberId(id)) {
            throw new BusinessRuleException(BusinessRules.RN_12, "No se puede eliminar el miembro: tiene reservas registradas");
        }
        memberRepository.delete(member);
    }

    /** RF-10: horas incluidas, usadas, restantes y excedentes del mes. */
    @Override
    public ConsumoResponse monthlyConsumption(Long id, YearMonth month) {
        Member member = getOrThrow(id);
        long usedMinutes = costCalculator.minutesUsedInMonth(id, month, null);
        long includedMinutes = member.getPlan().getIncludedHoursPerMonth() * 60L;
        BigDecimal included = BookingCostCalculator.minutesToHours(includedMinutes);
        BigDecimal used = BookingCostCalculator.minutesToHours(usedMinutes);
        BigDecimal remaining = BookingCostCalculator.minutesToHours(Math.max(0, includedMinutes - usedMinutes));
        BigDecimal overage = BookingCostCalculator.minutesToHours(Math.max(0, usedMinutes - includedMinutes));
        return new ConsumoResponse(id, month.toString(), member.getPlan().getName(), included, used, remaining, overage);
    }

    private Member getOrThrow(Long id) {
        return memberRepository.findWithDetailsById(id).orElseThrow(() -> new ResourceNotFoundException("Miembro", id));
    }

    private MembershipPlan getPlan(Long planId) {
        return planRepository.findById(planId).orElseThrow(() -> new ResourceNotFoundException("Plan", planId));
    }
}
