package com.yitong.guides.service;

import com.yitong.guides.domain.User;
import com.yitong.guides.domain.UserRole;
import com.yitong.guides.repo.UserRepository;
import com.yitong.guides.web.api.ApiException;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Value("${app.passwordChangeCooldownDays:7}")
  private int passwordChangeCooldownDays;

  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  /** 注册新用户 */
  @Transactional
  public User register(String username, String password, String displayName) {
    if (userRepository.existsByUsername(username)) {
      throw new ApiException(HttpStatus.CONFLICT, "用户名已存在");
    }
    User u = new User();
    u.setUsername(username);
    u.setDisplayName(displayName);
    u.setPasswordHash(passwordEncoder.encode(password));
    u.setRole(UserRole.USER);
    return userRepository.save(u);
  }

  /** 校验登录：返回用户，失败抛异常 */
  public User validateLogin(String username, String password) {
    User u = userRepository
        .findByUsername(username)
        .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "用户名或密码错误"));
    if (!u.isEnabled() || !passwordEncoder.matches(password, u.getPasswordHash())) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
    }
    if (u.getStatus() != 0) {
      throw new ApiException(HttpStatus.FORBIDDEN, "账号已封禁或已注销");
    }
    return u;
  }

  public User findById(Long id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "用户不存在"));
  }

  public User findByIdOrUnauthorized(Long id, String message) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, message));
  }

  public List<User> findAll() {
    return userRepository.findAll();
  }

  /** 管理员：更新用户启用状态与角色 */
  @Transactional
  public void updateUser(Long id, Boolean enabled, UserRole role) {
    User u = findById(id);
    u.setEnabled(enabled);
    u.setRole(role);
    userRepository.save(u);
  }

  /** 管理员：修改用户状态 0-正常 1-封禁 2-已注销 */
  @Transactional
  public void setStatus(Long id, int status) {
    User u = findById(id);
    u.setStatus(status);
    userRepository.save(u);
  }

  /** 管理员：重置密码 */
  @Transactional
  public void resetPassword(Long id, String newPassword) {
    User u = findById(id);
    u.setPasswordHash(passwordEncoder.encode(newPassword));
    userRepository.save(u);
  }

  /** 管理员：逻辑注销用户（status=2），不物理删除 */
  @Transactional
  public void deactivateUser(Long id) {
    User u = findById(id);
    u.setStatus(2);
    userRepository.save(u);
  }

  /** 当前用户修改用户名 */
  @Transactional
  public void updateUsername(Long userId, String newUsername) {
    User user = findById(userId);
    if (user.getStatus() != 0) {
      throw new ApiException(HttpStatus.FORBIDDEN, "账号异常");
    }
    String trimmed = newUsername.trim();
    if (userRepository.existsByUsername(trimmed) && !user.getUsername().equalsIgnoreCase(trimmed)) {
      throw new ApiException(HttpStatus.CONFLICT, "用户名已被占用");
    }
    user.setUsername(trimmed);
    userRepository.save(user);
  }

  /** 当前用户修改密码（带 7 天冷却） */
  @Transactional
  public void updatePassword(Long userId, String oldPassword, String newPassword) {
    User user = findById(userId);
    if (user.getStatus() != 0) {
      throw new ApiException(HttpStatus.FORBIDDEN, "账号异常");
    }
    Instant now = Instant.now();
    if (user.getPasswordChangedAt() != null) {
      Instant allowedAfter = user.getPasswordChangedAt().plusSeconds(passwordChangeCooldownDays * 86400L);
      if (now.isBefore(allowedAfter)) {
        throw new ApiException(HttpStatus.BAD_REQUEST,
            "修改密码过于频繁，请 " + passwordChangeCooldownDays + " 天后再试");
      }
    }
    if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "原密码错误");
    }
    user.setPasswordHash(passwordEncoder.encode(newPassword));
    user.setPasswordChangedAt(now);
    userRepository.save(user);
  }
}
