package com.yitong.guides.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "guides")
public class Guide {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "author_id", nullable = false)
  private User author;

  @Column(nullable = false, length = 120)
  private String title;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private GuideCategory category;

  @Column(length = 32)
  private String templateKey;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String contentMarkdown;

  @Column(nullable = false)
  private Instant createdAt = Instant.now();

  @Column(nullable = false)
  private Instant updatedAt = Instant.now();

  /** 逻辑删除：true 表示已删除，不物理删除 */
  @Column(nullable = false)
  private boolean deleted = false;

  public Long getId() {
    return id;
  }

  public User getAuthor() {
    return author;
  }

  public void setAuthor(User author) {
    this.author = author;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public GuideCategory getCategory() {
    return category;
  }

  public void setCategory(GuideCategory category) {
    this.category = category;
  }

  public String getTemplateKey() {
    return templateKey;
  }

  public void setTemplateKey(String templateKey) {
    this.templateKey = templateKey;
  }

  public String getContentMarkdown() {
    return contentMarkdown;
  }

  public void setContentMarkdown(String contentMarkdown) {
    this.contentMarkdown = contentMarkdown;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void touchUpdatedAt() {
    this.updatedAt = Instant.now();
  }

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }
}

