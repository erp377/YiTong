package com.yitong.guides.web.api;

import com.yitong.guides.domain.User;
import com.yitong.guides.domain.UserRole;
import com.yitong.guides.security.AuthUser;
import com.yitong.guides.service.UserService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
  private final UserService userService;

  public AdminUserController(UserService userService) {
    this.userService = userService;
  }

  /** status: 0-正常 1-封禁 2-已注销 */
  public record UserAdminResp(
      Long id,
      String username,
      String displayName,
      String role,
      boolean enabled,
      int status,
      String createdAt,
      String passwordHash) {}

  @GetMapping
  public List<UserAdminResp> list() {
    DateTimeFormatter fmt = DateTimeFormatter.ISO_INSTANT;
    return userService.findAll().stream()
        .map(
            u ->
                new UserAdminResp(
                    u.getId(),
                    u.getUsername(),
                    u.getDisplayName(),
                    u.getRole() != null ? u.getRole().name() : "",
                    u.isEnabled(),
                    u.getStatus(),
                    fmt.format(u.getCreatedAt()),
                    u.getPasswordHash()))
        .toList();
  }

  public record UpdateUserReq(@NotNull Boolean enabled, @NotNull UserRole role) {}

  @PutMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void update(@PathVariable Long id, @RequestBody UpdateUserReq req) {
    userService.updateUser(id, req.enabled(), req.role());
  }

  /** 修改用户状态：0-正常 1-封禁 2-已注销。不执行物理删除。 */
  public record StatusReq(@NotNull @Min(0) @Max(2) Integer status) {}

  @PatchMapping("/{id}/status")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void setStatus(@PathVariable Long id, @RequestBody StatusReq req) {
    userService.setStatus(id, req.status());
  }

  public record ResetPasswordReq(@NotNull @Size(min = 6, max = 72) String newPassword) {}

  @PostMapping("/{id}/reset-password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void resetPassword(@PathVariable Long id, @RequestBody ResetPasswordReq req) {
    userService.resetPassword(id, req.newPassword());
  }

  /** 管理员逻辑删除用户：将 status 设为 2（已注销），不物理删除。 */
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteUser(@PathVariable Long id, Authentication auth) {
    if (auth == null || !(auth.getPrincipal() instanceof AuthUser au)) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "请先登录");
    }
    User current = userService.findByIdOrUnauthorized(au.id(), "用户不存在");
    if (current.getRole() == null || current.getRole() != UserRole.ADMIN) {
      throw new ApiException(HttpStatus.FORBIDDEN, "仅管理员可删除用户");
    }
    userService.deactivateUser(id);
  }
}
