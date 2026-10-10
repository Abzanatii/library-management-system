package com.abz.library.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.abz.library.user.entity.Role;
import com.abz.library.user.entity.User;
import com.abz.library.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j 
@Component 
@RequiredArgsConstructor 
public class Seeder implements ApplicationRunner{
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Value ("${seed.admin.email:}")
    private String adminEmail;

    @Value ("${seed.admin.password:}")
    private String adminPassword;

    @Value ("${seed.librarian.email:}")
    private String librarianEmail;

    @Value ("${seed.librarian.password:}")
    private String librarianPassword;

    private void seed(String email, String password, String firstName, String lastName, Role role){
        if (email.isBlank() || password.isBlank())
            return;

        String nEmail = email.trim().toLowerCase();
        if (userRepository.existsByEmail(nEmail)) {
            return;
        }

        userRepository.save(
            User.builder()
            .firstName(firstName)
            .lastName(lastName)
            .email(nEmail)
            .passwordHash(passwordEncoder.encode(password))
            .role(role)
            .build());
        log.info("Created {} account", role);
    }
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        seed(adminEmail, adminPassword, "Library", "Admin", Role.ADMIN);
        seed(librarianEmail, librarianPassword, "Library", "Librarian", Role.LIBRARIAN);
    }

}
