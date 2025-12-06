package com.example.viegymapp;

import com.example.viegymapp.entity.Enum.PredefinedRole;
import com.example.viegymapp.entity.Enum.UserStatus;
import com.example.viegymapp.entity.Role;
import com.example.viegymapp.entity.User;
import com.example.viegymapp.entity.UserRole;
import com.example.viegymapp.repository.RoleRepository;
import com.example.viegymapp.repository.UserRepository;
import com.example.viegymapp.repository.UserRoleRepository;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class VieGymAppApplication {
	public static void main(String[] args) {
		// Load .env file TRƯỚC KHI Spring Boot khởi động
		loadDotEnv();
		
		SpringApplication.run(VieGymAppApplication.class, args);
	}
	
	/**
	 * Load file .env và set vào System Properties
	 * Spring Boot sẽ tự động đọc từ System Properties
	 */
	private static void loadDotEnv() {
		try {
			// dotenv-java tự động tìm file .env trong thư mục hiện tại
			Dotenv dotenv = Dotenv.configure()
					.directory(".") // Tìm trong thư mục hiện tại
					.ignoreIfMalformed()
					.ignoreIfMissing()
					.load();
			
			// Set tất cả biến vào System Properties
			// Spring Boot sẽ tự động đọc từ System Properties khi resolve placeholders
			dotenv.entries().forEach(entry -> {
				System.setProperty(entry.getKey(), entry.getValue());
			});
			
			System.out.println("✅ Loaded " + dotenv.entries().size() + " environment variables from .env file");
			
		} catch (Exception e) {
			System.err.println("⚠️ WARNING: Could not load .env file: " + e.getMessage());
			System.err.println("⚠️ Make sure .env file exists in the project root directory");
		}
	}

	@Bean
	CommandLineRunner run(RoleRepository roleRepository, UserRepository userRepository, 
						  UserRoleRepository userRoleRepository, PasswordEncoder passwordEncoder,
						  org.springframework.core.env.Environment env) {
		return args -> {
			// Initialize roles
			if (roleRepository.findByName(PredefinedRole.ROLE_USER).isEmpty()) {
				roleRepository.save(new Role(PredefinedRole.ROLE_USER));
			}
			if (roleRepository.findByName(PredefinedRole.ROLE_ADMIN).isEmpty()) {
				roleRepository.save(new Role(PredefinedRole.ROLE_ADMIN));
			}
			if (roleRepository.findByName(PredefinedRole.ROLE_COACH).isEmpty()) {
				roleRepository.save(new Role(PredefinedRole.ROLE_COACH));
			}
			if (roleRepository.findByName(PredefinedRole.ROLE_SUPER_ADMIN).isEmpty()) {
				roleRepository.save(new Role(PredefinedRole.ROLE_SUPER_ADMIN));
			}

			// Create Super Admin account from environment variables
			String superAdminEmail = env.getProperty("app.super-admin.email");
			String superAdminPassword = env.getProperty("app.super-admin.password");
			
			if (superAdminEmail != null && superAdminPassword != null 
					&& userRepository.findByEmail(superAdminEmail).isEmpty()) {
				User superAdmin = User.builder()
						.email(superAdminEmail)
						.fullName("Super Administrator")
						.password(passwordEncoder.encode(superAdminPassword))
						.status(UserStatus.ACTIVE)
						.streakDays(0)
						.totalWorkouts(0)
						.totalVolume(0.0)
						.build();
				
				userRepository.save(superAdmin);

				Role superAdminRole = roleRepository.findByName(PredefinedRole.ROLE_SUPER_ADMIN)
						.orElseThrow();

				UserRole userRole = UserRole.builder()
						.user(superAdmin)
						.role(superAdminRole)
						.assignedBy(superAdmin)
						.build();

				userRoleRepository.save(userRole);
			}
		};
	}
}



