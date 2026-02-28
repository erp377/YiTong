package com.yitong.guides.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(
    name = "follows",
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_follow_user_followed", columnNames = {"user_id", "follow_user_id"})
    })
public class Follow {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "follow_user_id", nullable = false)
  private Long followUserId;

  @Column(nullable = false)
  private final Instant createdAt = Instant.now();

  public Long getId() {
    return id;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public Long getFollowUserId() {
    return followUserId;
  }

  public void setFollowUserId(Long followUserId) {
    this.followUserId = followUserId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
