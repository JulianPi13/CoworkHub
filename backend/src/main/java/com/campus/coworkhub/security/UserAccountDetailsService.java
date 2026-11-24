package com.campus.coworkhub.security;

import com.campus.coworkhub.persistence.repository.UserAccountRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Carga las cuentas (UserAccount) para que Spring Security valide usuario y contraseña (BCrypt). */
@Service
public class UserAccountDetailsService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    public UserAccountDetailsService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userAccountRepository.findByUsername(username)
                .map(account -> User.withUsername(account.getUsername())
                        .password(account.getPassword())
                        .roles(account.getRole().name())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
}
