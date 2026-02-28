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
    name = "guide_likes",
    uniqueConstraints = {@UniqueConstraint(name = "uk_like_user_guide", columnNames = {"user_id", "guide_id"})})
public class GuideLike {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "guide_id", nullable = false)
  private Long guideId;

  @Column(nullable = false)
  private Instant createdAt = Instant.now();

  public Long getId() {
    return id;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public Long getGuideId() {
    return guideId;
  }

  public void setGuideId(Long guideId) {
    this.guideId = guideId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}

