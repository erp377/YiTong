package com.yitong.guides.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
    name = "study_checkins",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_checkin_user_guide_day",
          columnNames = {"user_id", "guide_id", "check_day"})
    })
public class StudyCheckIn {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "guide_id", nullable = false)
  private Long guideId;

  @Column(name = "check_day", nullable = false)
  private LocalDate day;

  @Column(nullable = false)
  private Integer progress = 0; // 0-100

  @Column(length = 400)
  private String note;

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

  public LocalDate getDay() {
    return day;
  }

  public void setDay(LocalDate day) {
    this.day = day;
  }

  public Integer getProgress() {
    return progress;
  }

  public void setProgress(Integer progress) {
    this.progress = progress;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}

