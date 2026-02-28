package com.yitong.guides.bootstrap;

import com.yitong.guides.domain.User;
import com.yitong.guides.domain.UserRole;
import com.yitong.guides.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements ApplicationRunner {
  private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final String adminUsername;
  private final String adminPassword;

  public AdminInitializer(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      @Value("${app.bootstrap.admin.username:admin}") String adminUsername,
      @Value("${app.bootstrap.admin.password:admin123}") String adminPassword) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.adminUsername = adminUsername;
    this.adminPassword = adminPassword;
  }

  @Override
  public void run(ApplicationArguments args) {
    User admin =
        userRepository
            .findByUsername(adminUsername)
            .orElseGet(
                () -> {
                  User u = new User();
                  u.setUsername(adminUsername);
                  u.setDisplayName("管理员");
                  return u;
                });

    boolean changed = false;
    if (admin.getRole() != UserRole.ADMIN) {
      admin.setRole(UserRole.ADMIN);
      changed = true;
    }
    if (!admin.isEnabled()) {
      admin.setEnabled(true);
      changed = true;
    }
    if (admin.getPasswordHash() == null || !passwordEncoder.matches(adminPassword, admin.getPasswordHash())) {
      admin.setPasswordHash(passwordEncoder.encode(adminPassword));
      changed = true;
    }

    if (changed) {
      userRepository.save(admin);
      log.warn("已确保默认管理员可用: 用户名={}, 密码={}（仅开发用，上线前请修改）", adminUsername, adminPassword);
    }
  }
}

