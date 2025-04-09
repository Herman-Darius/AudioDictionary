package com.example.SCDProiectv2.Services;

import com.example.SCDProiectv2.Models.Courier;
import com.example.SCDProiectv2.Models.Role;
import com.example.SCDProiectv2.Repositories.CourierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class AdminUserInitializer {

    @Autowired
    private CourierRepository courierRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void createAdminUser() {
        String adminEmail = "admin@system.com";
        String adminPassword = "admin";

        // Check if the admin already exists
        if (!courierRepository.existsByEmail(adminEmail)) {
            Courier adminCourier = new Courier();
            adminCourier.setFirstName("Admin");
            adminCourier.setLastName("Courier");
            adminCourier.setUsername("admin");
            adminCourier.setEmail(adminEmail);
            adminCourier.setPassword(passwordEncoder.encode(adminPassword));
            adminCourier.setRole(Role.ADMIN); // Or use an Enum for roles
            courierRepository.save(adminCourier);

            System.out.println("Admin user created with email: " + adminEmail);
        } else {
            System.out.println("Admin user already exists.");
        }
    }
}
