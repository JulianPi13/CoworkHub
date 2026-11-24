package com.campus.coworkhub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.ZoneOffset;

/**
 * Beans de infraestructura (módulo 02).
 * <ul>
 *   <li>{@link Clock}: la "hora actual" se inyecta en lugar de llamar a LocalDateTime.now(),
 *       así las pruebas pueden fijarla (RN-03 y RN-08 dependen de "ahora").
 *       Hay dos relojes: el de negocio (zona local, {@code @Primary}) y uno en UTC
 *       ({@code @Qualifier("utcClock")}) para los timestamps de errores y tokens.</li>
 *   <li>{@link PasswordEncoder}: BCrypt para las contraseñas de UserAccount.</li>
 * </ul>
 */
@Configuration
public class AppConfig {

    public static final String UTC_CLOCK = "utcClock";

    /** Reloj de negocio: horario de las sedes y de las reservas (hora local). */
    @Bean
    @Primary
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    /** Reloj en UTC para marcas de tiempo técnicas (timestamp del cuerpo de error, emisión del JWT). */
    @Bean(UTC_CLOCK)
    public Clock utcClock() {
        return Clock.system(ZoneOffset.UTC);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
