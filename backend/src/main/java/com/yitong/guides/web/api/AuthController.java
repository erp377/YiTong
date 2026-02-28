package com.yitong.guides.web.api;

import com.yitong.guides.domain.User;
import com.yitong.guides.security.JwtService;
import com.yitong.guides.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final UserService userService;
  private final JwtService jwtService;

  public AuthController(UserService userService, JwtService jwtService) {
    this.userService = userService;
    this.jwtService = jwtService;
  }

  public record RegisterReq(
      @NotBlank @Size(min = 3, max = 32) String username,
      @NotBlank @Size(min = 6, max = 72) String password,
      @NotBlank @Size(min = 1, max = 32) String displayName) {}

  public record LoginReq(@NotBlank String username, @NotBlank String password) {}

  public record AuthResp(String token, UserResp user) {}

  public record UserResp(Long id, String username, String displayName, String role) {}

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public UserResp register(@Valid @RequestBody RegisterReq req) {
    User saved = userService.register(req.username(), req.password(), req.displayName());
    String roleName = saved.getRole() != null ? saved.getRole().name() : "USER";
    return new UserResp(saved.getId(), saved.getUsername(), saved.getDisplayName(), roleName);
  }

  @PostMapping("/login")
  public AuthResp login(@Valid @RequestBody LoginReq req) {
    User u = userService.validateLogin(req.username(), req.password());
    String roleName = u.getRole() != null ? u.getRole().name() : "USER";
    String token = jwtService.issueToken(u.getId(), u.getUsername(), roleName);
    return new AuthResp(
        token,
        new UserResp(u.getId(), u.getUsername(), u.getDisplayName(), roleName));
  }
}
