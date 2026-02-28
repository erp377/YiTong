package com.yitong.guides.web.api;

import com.yitong.guides.domain.Comment;
import com.yitong.guides.domain.Guide;
import com.yitong.guides.domain.GuideCategory;
import com.yitong.guides.domain.StudyCheckIn;
import com.yitong.guides.repo.CommentRepository;
import com.yitong.guides.repo.StudyCheckInRepository;
import com.yitong.guides.security.AuthUser;
import com.yitong.guides.service.GuideService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/guides")
public class GuidesController {
  private final GuideService guideService;
  private final CommentRepository commentRepo;
  private final StudyCheckInRepository checkInRepo;

  public GuidesController(
      GuideService guideService,
      CommentRepository commentRepo,
      StudyCheckInRepository checkInRepo) {
    this.guideService = guideService;
    this.commentRepo = commentRepo;
    this.checkInRepo = checkInRepo;
  }

  private static AuthUser mustAuth(Authentication auth) {
    if (auth == null || !(auth.getPrincipal() instanceof AuthUser u)) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "请先登录");
    }
    return u;
  }

  public record GuideCardResp(
      Long id,
      Long authorId,
      String title,
      GuideCategory category,
      String templateKey,
      String authorName,
      String createdAt,
      long likeCount,
      long favoriteCount) {}

  public record GuideDetailResp(
      Long id,
      Long authorId,
      String title,
      GuideCategory category,
      String templateKey,
      String contentMarkdown,
      String authorName,
      String createdAt,
      String updatedAt,
      long likeCount,
      long favoriteCount,
      boolean liked,
      boolean favorited,
      long checkinCount,
      boolean checkinToday) {}

  @GetMapping
  public Page<GuideCardResp> list(
      @RequestParam(required = false) GuideCategory category,
      @RequestParam(required = false) String q,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size,
      @RequestParam(defaultValue = "latest") String sort) {
    Sort s = Sort.by(Sort.Direction.DESC, "createdAt");
    if ("updated".equalsIgnoreCase(sort)) {
      s = Sort.by(Sort.Direction.DESC, "updatedAt");
    }
    PageRequest pr = PageRequest.of(page, size, s);
    Page<Guide> p = guideService.search(category, q, pr);
    return p.map(
        g ->
            new GuideCardResp(
                g.getId(),
                g.getAuthor().getId(),
                g.getTitle(),
                g.getCategory(),
                g.getTemplateKey(),
                g.getAuthor().getDisplayNamePublic(),
                g.getCreatedAt().toString(),
                guideService.countLikesByGuideId(g.getId()),
                guideService.countFavoritesByGuideId(g.getId())));
  }

  @GetMapping("/{id}")
  public GuideDetailResp detail(@PathVariable Long id, Authentication auth) {
    Guide g = guideService.getByIdWithAuthor(id);
    long likeCount = guideService.countLikesByGuideId(id);
    long favoriteCount = guideService.countFavoritesByGuideId(id);
    boolean liked = false;
    boolean favorited = false;
    boolean checkinToday = false;
    if (auth != null && auth.getPrincipal() instanceof AuthUser u) {
      liked = guideService.existsLike(u.id(), id);
      favorited = guideService.existsFavorite(u.id(), id);
      checkinToday = checkInRepo.findByUserIdAndGuideIdAndDay(u.id(), id, LocalDate.now()).isPresent();
    }
    long checkinCount = guideService.countDistinctUserIdByGuideId(id);
    return new GuideDetailResp(
        g.getId(),
        g.getAuthor().getId(),
        g.getTitle(),
        g.getCategory(),
        g.getTemplateKey(),
        g.getContentMarkdown(),
        g.getAuthor().getDisplayNamePublic(),
        g.getCreatedAt().toString(),
        g.getUpdatedAt().toString(),
        likeCount,
        favoriteCount,
        liked,
        favorited,
        checkinCount,
        checkinToday);
  }

  public record UpsertGuideReq(
      @NotBlank @Size(max = 120) String title,
      @NotNull GuideCategory category,
      String templateKey,
      @NotBlank String contentMarkdown) {}

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public GuideDetailResp create(@Valid @RequestBody UpsertGuideReq req, Authentication auth) {
    AuthUser u = mustAuth(auth);
    Guide saved = guideService.create(
        u.id(),
        req.title(),
        req.category(),
        req.templateKey(),
        req.contentMarkdown());
    return detail(saved.getId(), auth);
  }

  @PutMapping("/{id}")
  public GuideDetailResp update(
      @PathVariable Long id, @Valid @RequestBody UpsertGuideReq req, Authentication auth) {
    AuthUser u = mustAuth(auth);
    guideService.update(
        id,
        u.id(),
        req.title(),
        req.category(),
        req.templateKey(),
        req.contentMarkdown());
    return detail(id, auth);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id, Authentication auth) {
    AuthUser u = mustAuth(auth);
    guideService.delete(id, u.id());
  }

  @PostMapping("/{id}/like")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void like(@PathVariable Long id, Authentication auth) {
    AuthUser u = mustAuth(auth);
    guideService.like(id, u.id());
  }

  @DeleteMapping("/{id}/like")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void unlike(@PathVariable Long id, Authentication auth) {
    AuthUser u = mustAuth(auth);
    guideService.unlike(id, u.id());
  }

  @PostMapping("/{id}/favorite")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void favorite(@PathVariable Long id, Authentication auth) {
    AuthUser u = mustAuth(auth);
    guideService.favorite(id, u.id());
  }

  @DeleteMapping("/{id}/favorite")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void unfavorite(@PathVariable Long id, Authentication auth) {
    AuthUser u = mustAuth(auth);
    guideService.unfavorite(id, u.id());
  }

  public record CommentResp(Long id, String userName, String content, String createdAt) {}
  public record CreateCommentReq(@NotBlank @Size(max = 800) String content) {}

  @GetMapping("/{id}/comments")
  public List<CommentResp> listComments(@PathVariable Long id) {
    if (!guideService.existsById(id)) {
      throw new ApiException(HttpStatus.NOT_FOUND, "攻略不存在");
    }
    return commentRepo.findByGuideIdOrderByCreatedAtAsc(id).stream()
        .map(c -> new CommentResp(c.getId(), c.getUser().getDisplayNamePublic(), c.getContent(), c.getCreatedAt().toString()))
        .toList();
  }

  @PostMapping("/{id}/comments")
  @ResponseStatus(HttpStatus.CREATED)
  public CommentResp createComment(
      @PathVariable Long id, @Valid @RequestBody CreateCommentReq req, Authentication auth) {
    AuthUser u = mustAuth(auth);
    Comment saved = guideService.createComment(id, u.id(), req.content());
    return new CommentResp(
        saved.getId(),
        saved.getUser().getDisplayNamePublic(),
        saved.getContent(),
        saved.getCreatedAt().toString());
  }

  public record CheckInResp(Long id, String day, Integer progress, String note, String createdAt) {}
  public record UpsertCheckInReq(
      @NotNull LocalDate day, @NotNull @Min(0) @Max(100) Integer progress, String note) {}

  @GetMapping("/{id}/checkins")
  public List<CheckInResp> listCheckIns(@PathVariable Long id, Authentication auth) {
    AuthUser u = mustAuth(auth);
    Guide g = guideService.getByIdWithAuthor(id);
    if (g.getCategory() != GuideCategory.STUDY && g.getCategory() != GuideCategory.GAME) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "仅学习、游戏类攻略支持打卡");
    }
    return checkInRepo.findByUserIdAndGuideIdOrderByDayAsc(u.id(), id).stream()
        .map(ci -> new CheckInResp(ci.getId(), ci.getDay().toString(), ci.getProgress(), ci.getNote(), ci.getCreatedAt().toString()))
        .toList();
  }

  @PostMapping("/{id}/checkins")
  public CheckInResp upsertCheckIn(
      @PathVariable Long id, @Valid @RequestBody UpsertCheckInReq req, Authentication auth) {
    AuthUser u = mustAuth(auth);
    StudyCheckIn saved = guideService.upsertCheckIn(id, u.id(), req.day(), req.progress(), req.note());
    return new CheckInResp(saved.getId(), saved.getDay().toString(), saved.getProgress(), saved.getNote(), saved.getCreatedAt().toString());
  }
}
