package com.campus.coworkhub.business.service;

import com.campus.coworkhub.business.dto.member.ConsumoResponse;
import com.campus.coworkhub.business.dto.member.MiembroCreateRequest;
import com.campus.coworkhub.business.dto.member.MiembroResponse;
import com.campus.coworkhub.business.dto.member.MiembroUpdateRequest;
import com.campus.coworkhub.persistence.entity.MemberStatus;

import java.time.YearMonth;
import java.util.List;

public interface MemberService {
    List<MiembroResponse> search(String nombre, MemberStatus estado);
    MiembroResponse findById(Long id);
    MiembroResponse create(MiembroCreateRequest request);
    MiembroResponse update(Long id, MiembroUpdateRequest request);
    MiembroResponse changeStatus(Long id, MemberStatus status);
    void delete(Long id);
    ConsumoResponse monthlyConsumption(Long id, YearMonth month);
}
