package com.campus.coworkhub.business.service;

import com.campus.coworkhub.business.dto.auth.LoginRequest;
import com.campus.coworkhub.business.dto.auth.LoginResponse;

public interface AuthService {

    /** RF-11: valida usuario y contraseña y devuelve un JWT que vence en 60 min. */
    LoginResponse login(LoginRequest request);
}
