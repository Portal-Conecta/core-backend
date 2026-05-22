package com.portal.conecta.hub.shared.config;

import com.portal.conecta.hub.module.user.domain.model.TypeUser;
import com.portal.conecta.hub.module.user.domain.model.UserEntity;
import com.portal.conecta.hub.module.user.domain.port.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("dev")
public class DevDataInitializer {

    private static final String ADMIN_EMAIL = "admin@portal.test";
    private static final String ADMIN_PASSWORD = "123456";

    @Bean
    public CommandLineRunner createDevAdminUser(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (userRepository.existsByEmailIgnoreCase(ADMIN_EMAIL)) {
                return;
            }

            UserEntity admin = UserEntity.create(
                    "Admin",
                    ADMIN_EMAIL,
                    ADMIN_PASSWORD,
                    TypeUser.ADMIN,
                    null,
                    passwordEncoder
            );

            userRepository.save(admin);
        };
    }
}
