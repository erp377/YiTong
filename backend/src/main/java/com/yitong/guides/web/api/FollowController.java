package com.yitong.guides.web.api;

import com.yitong.guides.domain.Follow;
import com.yitong.guides.domain.GuideCategory;
import com.yitong.guides.repo.FollowRepository;
import com.yitong.guides.repo.GuideRepository;
import com.yitong.guides.repo.UserRepository;
import com.yitong.guides.security.AuthUser;
import com.yitong.guides.service.FollowService;
import com.yitong.guides.service.GuideService;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class FollowController {
  private final FollowService followService;
  private final FollowRepository followRepo;
  private final UserRepository userRepo;
  private final GuideRepository guideRepo;
  private final GuideService guideService;

  public FollowController(
      FollowService followService,
      FollowRepository followRepo,
      UserRepository userRepo,
      GuideRepository guideRepo,
      GuideService guideService) {
    this.followService = followService;
    this.followRepo = followRepo;
    this.userRepo = userRepo;
    this.guideRepo = guideRepo;
    this.guideService = guideService;
  }

  private static AuthUser mustAuth(Authentication auth) {
    if (auth == null || !(auth.getPrincipal() instanceof AuthUser u)) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "请先登录");
    }
    return u;
  }

  @PostMapping("/users/{id}/follow")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void follow(@PathVariable Long id, Authentication auth) {
    AuthUser u = mustAuth(auth);
    followService.follow(u.id(), id);
  }

  @DeleteMapping("/users/{id}/follow")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void unfollow(@PathVariable Long id, Authentication auth) {
    AuthUser u = mustAuth(auth);
    followService.unfollow(u.id(), id);
  }

  public record UserBriefResp(Long id, String username, String displayName, boolean following) {}

  @GetMapping("/me/following")
  public List<UserBriefResp> myFollowing(Authentication auth) {
    AuthUser u = mustAuth(auth);
    List<Long> ids = followRepo.findByUserIdOrderByCreatedAtDesc(u.id()).stream()
        .map(Follow::getFollowUserId)
        .toList();
    return ids.stream()
        .map(userRepo::findById)
        .filter(java.util.Optional::isPresent)
        .map(java.util.Optional::get)
        .map(user -> new UserBriefResp(user.getId(), user.getUsername(), user.getDisplayName(), true))
        .toList();
  }

  public record GuideCardResp(
      Long id,
      Long authorId,
      String title,
      GuideCategory category,
      String authorName,
      String createdAt,
      long likeCount,
      long favoriteCount) {}

  @GetMapping("/me/feed")
  public List<GuideCardResp> feed(
      Authentication auth,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    AuthUser u = mustAuth(auth);
    List<Long> authorIds = followRepo.findByUserIdOrderByCreatedAtDesc(u.id()).stream()
        .map(Follow::getFollowUserId)
        .toList();
    if (authorIds.isEmpty()) return List.of();
    PageRequest pr = PageRequest.of(page, size);
    return guideRepo.findByAuthor_IdInAndDeletedFalseOrderByCreatedAtDesc(authorIds, pr).stream()
        .map(g -> new GuideCardResp(
            g.getId(),
            g.getAuthor().getId(),
            g.getTitle(),
            g.getCategory(),
            g.getAuthor().getDisplayNamePublic(),
            g.getCreatedAt().toString(),
            guideService.countLikesByGuideId(g.getId()),
            guideService.countFavoritesByGuideId(g.getId())))
        .toList();
  }

  @GetMapping("/users/{id}/following")
  public boolean isFollowing(@PathVariable Long id, Authentication auth) {
    if (auth == null || !(auth.getPrincipal() instanceof AuthUser u)) return false;
    return followService.existsByUserIdAndFollowUserId(u.id(), id);
  }
}
