package com.shopflow.shopflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.shopflow.shopflow.entity.User;
import com.shopflow.shopflow.enums.Role;
import com.shopflow.shopflow.repository.UserRepository;
import com.shopflow.shopflow.repository.CartRepository;

import java.time.LocalDateTime;

@SpringBootApplication
public class ShopflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopflowApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(
			UserRepository userRepository,
			CartRepository cartRepository,
			PasswordEncoder passwordEncoder
	) {
		return args -> {
			if (!userRepository.existsByEmail("admin@shopflow.com")) {
				User admin = User.builder()
						.email("admin@shopflow.com")
						.password(passwordEncoder.encode("admin123"))
						.firstName("System")
						.lastName("Admin")
						.role(Role.ADMIN)
						.active(true)
						.createdAt(LocalDateTime.now())
						.build();
				admin = userRepository.save(admin);

				System.out.println("=================================================");
				System.out.println(" Default ADMIN created successfully!");
				System.out.println(" Email: admin@shopflow.com");
				System.out.println(" Password: admin123");
				System.out.println("=================================================");
			}
            // --- AUTO-PROMOTION LOGIC ---
            String adminEmail = System.getenv("ADMIN_EMAIL");
            if (adminEmail != null && !adminEmail.isBlank()) {
                userRepository.findByEmail(adminEmail).ifPresent(user -> {
                    if (user.getRole() != Role.ADMIN) {
                        user.setRole(Role.ADMIN);
                        userRepository.save(user);
                        System.out.println(">>> [AUTO-PROMOTION] User " + adminEmail + " has been promoted to ADMIN.");
                    }
                });
            }
            // ----------------------------
		};
	}
}
