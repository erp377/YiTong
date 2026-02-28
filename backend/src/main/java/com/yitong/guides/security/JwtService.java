package com.yitong.guides.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.time.Instant;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final Algorithm algorithm;
  private final JWTVerifier verifier;
  private final String issuer;
  private final long expiresMinutes;

  public JwtService(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.issuer}") String issuer,
      @Value("${app.jwt.expiresMinutes}") long expiresMinutes) {
    this.algorithm = Algorithm.HMAC256(secret);
    this.issuer = issuer;
    this.expiresMinutes = expiresMinutes;
    this.verifier = JWT.require(algorithm).withIssuer(issuer).build();
  }

  public String issueToken(Long userId, String username, String role) {
    Instant now = Instant.now();
    Instant exp = now.plusSeconds(expiresMinutes * 60);
    return JWT.create()
        .withIssuer(issuer)
        .withIssuedAt(Date.from(now))
        .withExpiresAt(Date.from(exp))
        .withSubject(String.valueOf(userId))
        .withClaim("username", username)
        .withClaim("role", role)
        .sign(algorithm);
  }

  public DecodedJWT verify(String token) throws JWTVerificationException {
    return verifier.verify(token);
  }
}

