package com.campus.coworkhub.business.service.impl;

import com.campus.coworkhub.business.dto.auth.LoginRequest;
import com.campus.coworkhub.business.dto.auth.LoginResponse;
import com.campus.coworkhub.business.service.AuthService;
import com.campus.coworkhub.persistence.entity.Member;
import com.campus.coworkhub.persistence.entity.UserAccount;
import com.campus.coworkhub.persistence.repository.UserAccountRepository;
import com.campus.coworkhub.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserAccountRepository userAccountRepository;
    private final JwtService jwtService;

    public AuthServiceImpl(AuthenticationManager authenticationManager, UserAccountRepository userAccountRepository,
                           JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userAccountRepository = userAccountRepository;
        this.jwtService = jwtService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        // Lanza BadCredentialsException (401) si el usuario no existe o la contraseña no coincide con el hash BCrypt
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.usuario(), request.contrasena()));

        UserAccount account = userAccountRepository.findByUsername(request.usuario())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));
        Long memberId = Optional.ofNullable(account.getMember()).map(Member::getId).orElse(null);

        String token = jwtService.generateToken(account.getUsername(), account.getRole(), memberId);
        return new LoginResponse(token, "Bearer", jwtService.getExpiration().toSeconds(),
                account.getUsername(), account.getRole(), memberId);
    }
}
