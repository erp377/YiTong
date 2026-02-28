package com.yitong.guides.web.api;

import com.yitong.guides.domain.Guide;
import com.yitong.guides.domain.GuideFavorite;
import com.yitong.guides.domain.StudyCheckIn;
import com.yitong.guides.domain.User;
import com.yitong.guides.repo.GuideFavoriteRepository;
import com.yitong.guides.repo.GuideRepository;
import com.yitong.guides.repo.StudyCheckInRepository;
import com.yitong.guides.security.AuthUser;
import com.yitong.guides.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class MeController {
  private final UserService userService;
  private final GuideRepository guideRepo;
  private final GuideFavoriteRepository favoriteRepo;
  private final StudyCheckInRepository checkInRepo;

  public MeController(
      UserService userService,
      GuideRepository guideRepo,
      GuideFavoriteRepository favoriteRepo,
      StudyCheckInRepository checkInRepo) {
    this.userService = userService;
    this.guideRepo = guideRepo;
    this.favoriteRepo = favoriteRepo;
    this.checkInRepo = checkInRepo;
  }

  private static AuthUser mustAuth(Authentication auth) {
    if (auth == null || !(auth.getPrincipal() instanceof AuthUser u)) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "请先登录");
    }
    return u;
  }

  public record MeResp(Long id, String username, String displayName, String role) {}

  @GetMapping
  public MeResp me(Authentication auth) {
    AuthUser u = mustAuth(auth);
    User user = userService.findByIdOrUnauthorized(u.id(), "用户不存在");
    if (user.getStatus() != 0) {
      throw new ApiException(HttpStatus.FORBIDDEN, "账号已封禁或已注销");
    }
    return new MeResp(user.getId(), user.getUsername(), user.getDisplayName(), user.getRole().name());
  }

  public record UpdateUsernameReq(@NotBlank @Size(min = 3, max = 32) String username) {}

  @PatchMapping("/username")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updateUsername(Authentication auth, @Valid @RequestBody UpdateUsernameReq req) {
    AuthUser u = mustAuth(auth);
    userService.updateUsername(u.id(), req.username());
  }

  public record UpdatePasswordReq(
      @NotBlank String oldPassword,
      @NotBlank @Size(min = 6, max = 72) String newPassword) {}

  @PatchMapping("/password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updatePassword(Authentication auth, @Valid @RequestBody UpdatePasswordReq req) {
    AuthUser u = mustAuth(auth);
    userService.updatePassword(u.id(), req.oldPassword(), req.newPassword());
  }

  public record GuideSimpleResp(Long id, String title, String createdAt) {}

  @GetMapping("/guides")
  public List<GuideSimpleResp> myGuides(Authentication auth) {
    AuthUser u = mustAuth(auth);
    return guideRepo.findByAuthorIdAndDeletedFalseOrderByCreatedAtDesc(u.id(), PageRequest.of(0, 50)).stream()
        .map(g -> new GuideSimpleResp(g.getId(), g.getTitle(), g.getCreatedAt().toString()))
        .toList();
  }

  @GetMapping("/favorites")
  public List<GuideSimpleResp> myFavorites(Authentication auth) {
    AuthUser u = mustAuth(auth);
    List<Long> ids = favoriteRepo.findByUserIdOrderByCreatedAtDesc(u.id()).stream().map(GuideFavorite::getGuideId).toList();
    return ids.stream()
        .map(guideRepo::findById)
        .filter(java.util.Optional::isPresent)
        .map(java.util.Optional::get)
        .filter(g -> !g.isDeleted())
        .map(g -> new GuideSimpleResp(g.getId(), g.getTitle(), g.getCreatedAt().toString()))
        .toList();
  }

  /** 我的打卡记录：攻略标题、打卡日期，可跳转帖子 */
  public record CheckInRecordResp(Long id, Long guideId, String guideTitle, String checkinDate, String createdAt) {}

  @GetMapping("/checkins")
  public List<CheckInRecordResp> myCheckins(Authentication auth) {
    AuthUser u = mustAuth(auth);
    List<StudyCheckIn> list = checkInRepo.findByUserIdOrderByCreatedAtDesc(u.id(), PageRequest.of(0, 200)).getContent();
    List<Long> guideIds = list.stream().map(StudyCheckIn::getGuideId).distinct().toList();
    Map<Long, Guide> guides = guideIds.stream()
        .map(guideRepo::findById)
        .filter(java.util.Optional::isPresent)
        .map(java.util.Optional::get)
        .filter(g -> !g.isDeleted())
        .collect(Collectors.toMap(Guide::getId, g -> g));
    return list.stream()
        .filter(c -> guides.containsKey(c.getGuideId()))
        .map(c -> new CheckInRecordResp(
            c.getId(),
            c.getGuideId(),
            guides.get(c.getGuideId()).getTitle(),
            c.getDay().toString(),
            c.getCreatedAt().toString()))
        .toList();
  }
}
