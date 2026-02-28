package com.yitong.guides.service;

import com.yitong.guides.domain.Comment;
import com.yitong.guides.domain.Guide;
import com.yitong.guides.domain.GuideCategory;
import com.yitong.guides.domain.GuideFavorite;
import com.yitong.guides.domain.GuideLike;
import com.yitong.guides.domain.StudyCheckIn;
import com.yitong.guides.domain.User;
import com.yitong.guides.repo.CommentRepository;
import com.yitong.guides.repo.GuideFavoriteRepository;
import com.yitong.guides.repo.GuideLikeRepository;
import com.yitong.guides.repo.GuideRepository;
import com.yitong.guides.repo.StudyCheckInRepository;
import com.yitong.guides.repo.UserRepository;
import com.yitong.guides.web.api.ApiException;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GuideService {

  private final GuideRepository guideRepository;
  private final UserRepository userRepository;
  private final GuideLikeRepository likeRepository;
  private final GuideFavoriteRepository favoriteRepository;
  private final CommentRepository commentRepository;
  private final StudyCheckInRepository checkInRepository;

  public GuideService(
      GuideRepository guideRepository,
      UserRepository userRepository,
      GuideLikeRepository likeRepository,
      GuideFavoriteRepository favoriteRepository,
      CommentRepository commentRepository,
      StudyCheckInRepository checkInRepository) {
    this.guideRepository = guideRepository;
    this.userRepository = userRepository;
    this.likeRepository = likeRepository;
    this.favoriteRepository = favoriteRepository;
    this.commentRepository = commentRepository;
    this.checkInRepository = checkInRepository;
  }

  public Optional<Guide> findByIdWithAuthor(Long id) {
    return guideRepository.findByIdWithAuthor(id);
  }

  public Guide getByIdWithAuthor(Long id) {
    return guideRepository
        .findByIdWithAuthor(id)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "攻略不存在"));
  }

  public Guide getById(Long id) {
    return guideRepository
        .findById(id)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "攻略不存在"));
  }

  public boolean existsById(Long id) {
    return guideRepository.existsById(id);
  }

  public boolean existsByIdAndDeletedFalse(Long id) {
    return guideRepository.existsByIdAndDeletedFalse(id);
  }

  public Page<Guide> search(GuideCategory category, String q, Pageable pageable) {
    return guideRepository.search(
        category,
        (q == null || q.isBlank()) ? null : q,
        pageable);
  }

  /** 创建攻略 */
  @Transactional
  public Guide create(Long authorId, String title, GuideCategory category, String templateKey, String contentMarkdown) {
    User author = userRepository
        .findById(authorId)
        .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "用户不存在"));
    Guide g = new Guide();
    g.setAuthor(author);
    g.setTitle(title);
    g.setCategory(category);
    g.setTemplateKey(templateKey);
    g.setContentMarkdown(contentMarkdown);
    return guideRepository.save(g);
  }

  /** 更新攻略（校验作者） */
  @Transactional
  public Guide update(Long guideId, Long authorId, String title, GuideCategory category, String templateKey, String contentMarkdown) {
    Guide g = getByIdWithAuthor(guideId);
    if (!g.getAuthor().getId().equals(authorId)) {
      throw new ApiException(HttpStatus.FORBIDDEN, "只能修改自己的攻略");
    }
    g.setTitle(title);
    g.setCategory(category);
    g.setTemplateKey(templateKey);
    g.setContentMarkdown(contentMarkdown);
    g.touchUpdatedAt();
    return guideRepository.save(g);
  }

  /** 逻辑删除攻略（校验作者） */
  @Transactional
  public void delete(Long guideId, Long authorId) {
    Guide g = getById(guideId);
    if (!g.getAuthor().getId().equals(authorId)) {
      throw new ApiException(HttpStatus.FORBIDDEN, "只能删除自己的攻略");
    }
    g.setDeleted(true);
    guideRepository.save(g);
  }

  /** 点赞 */
  @Transactional
  public void like(Long guideId, Long userId) {
    if (!guideRepository.existsById(guideId)) {
      throw new ApiException(HttpStatus.NOT_FOUND, "攻略不存在");
    }
    if (likeRepository.existsByUserIdAndGuideId(userId, guideId)) {
      return;
    }
    GuideLike l = new GuideLike();
    l.setUserId(userId);
    l.setGuideId(guideId);
    likeRepository.save(l);
  }

  /** 取消点赞 */
  @Transactional
  public void unlike(Long guideId, Long userId) {
    likeRepository.deleteByUserIdAndGuideId(userId, guideId);
  }

  /** 收藏 */
  @Transactional
  public void favorite(Long guideId, Long userId) {
    if (!guideRepository.existsById(guideId)) {
      throw new ApiException(HttpStatus.NOT_FOUND, "攻略不存在");
    }
    if (favoriteRepository.existsByUserIdAndGuideId(userId, guideId)) {
      return;
    }
    GuideFavorite f = new GuideFavorite();
    f.setUserId(userId);
    f.setGuideId(guideId);
    favoriteRepository.save(f);
  }

  /** 取消收藏 */
  @Transactional
  public void unfavorite(Long guideId, Long userId) {
    favoriteRepository.deleteByUserIdAndGuideId(userId, guideId);
  }

  /** 发表评论 */
  @Transactional
  public Comment createComment(Long guideId, Long userId, String content) {
    Guide g = getByIdWithAuthor(guideId);
    User user = userRepository
        .findById(userId)
        .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "用户不存在"));
    Comment c = new Comment();
    c.setGuide(g);
    c.setUser(user);
    c.setContent(content);
    return commentRepository.save(c);
  }

  /** 打卡（学习/游戏类攻略） */
  @Transactional
  public StudyCheckIn upsertCheckIn(Long guideId, Long userId, LocalDate day, int progress, String note) {
    Guide g = getByIdWithAuthor(guideId);
    if (g.getCategory() != GuideCategory.STUDY && g.getCategory() != GuideCategory.GAME) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "仅学习、游戏类攻略支持打卡");
    }
    StudyCheckIn ci = checkInRepository
        .findByUserIdAndGuideIdAndDay(userId, guideId, day)
        .orElseGet(StudyCheckIn::new);
    ci.setUserId(userId);
    ci.setGuideId(guideId);
    ci.setDay(day);
    ci.setProgress(progress);
    ci.setNote(note);
    return checkInRepository.save(ci);
  }

  // 以下供 Controller 组装 DTO 时使用
  public long countLikesByGuideId(Long guideId) {
    return likeRepository.countByGuideId(guideId);
  }

  public long countFavoritesByGuideId(Long guideId) {
    return favoriteRepository.countByGuideId(guideId);
  }

  public boolean existsLike(Long userId, Long guideId) {
    return likeRepository.existsByUserIdAndGuideId(userId, guideId);
  }

  public boolean existsFavorite(Long userId, Long guideId) {
    return favoriteRepository.existsByUserIdAndGuideId(userId, guideId);
  }

  public long countDistinctUserIdByGuideId(Long guideId) {
    return checkInRepository.countDistinctUserIdByGuideId(guideId);
  }
}
