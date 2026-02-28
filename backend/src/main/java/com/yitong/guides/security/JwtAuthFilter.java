package com.yitong.guides.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtService jwtService;

  public JwtAuthFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (auth != null && auth.startsWith("Bearer ")) {
      String token = auth.substring("Bearer ".length());
      try {
        DecodedJWT jwt = jwtService.verify(token);
        Long userId = Long.valueOf(jwt.getSubject());
        String username = jwt.getClaim("username").asString();
        String role = jwt.getClaim("role").asString();
        AuthUser principal = new AuthUser(userId, username);
        var authorities =
            (role != null && !role.isBlank())
                ? List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role))
                : List.<org.springframework.security.core.GrantedAuthority>of();
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(principal, null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (JWTVerificationException | NumberFormatException ignored) {
        // invalid token -> treat as anonymous
      }
    }
    filterChain.doFilter(request, response);
  }
}

