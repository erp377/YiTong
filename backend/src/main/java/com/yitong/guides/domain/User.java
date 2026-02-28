package com.yitong.guides.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "users")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 32)
  private String username;

  @Column(nullable = false)
  private String passwordHash;

  @Column(nullable = false, length = 32)
  private String displayName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = true, length = 16)
  private UserRole role;

  @Column(nullable = true)
  private Boolean enabled;

  /** 0-正常 1-封禁 2-已注销；管理员只能改 status，不物理删除 */
  @Column(nullable = false)
  private int status = 0;

  @Column(nullable = false)
  private Instant createdAt = Instant.now();

  /** 上次修改密码时间，用于“改密码时限”校验 */
  @Column(name = "password_changed_at")
  private Instant passwordChangedAt;

  public Long getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public UserRole getRole() {
    return role == null ? UserRole.USER : role;
  }

  public void setRole(UserRole role) {
    this.role = role;
  }

  public boolean isEnabled() {
    return enabled == null || enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  /** 是否已注销 */
  public boolean isDeactivated() {
    return status == 2;
  }

  /** 对外展示名：已注销显示「已注销用户」 */
  public String getDisplayNamePublic() {
    return status == 2 ? "已注销用户" : displayName;
  }

  public Instant getPasswordChangedAt() {
    return passwordChangedAt;
  }

  public void setPasswordChangedAt(Instant passwordChangedAt) {
    this.passwordChangedAt = passwordChangedAt;
  }
}

