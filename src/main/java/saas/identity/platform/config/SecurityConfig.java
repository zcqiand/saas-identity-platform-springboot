package saas.identity.platform.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            authz ->
                authz
                    .requestMatchers("/api/v1/auth/**")
                    .permitAll()
                    // Dev 简化：任何 authenticated() 用户都能访问所有 endpoint。
                    // Production 要恢复：
                    //   .requestMatchers("/api/v1/admin/**").hasAuthority("SCOPE_platform_admin")
                    //   .requestMatchers("/api/v1/tenants/*/users").access(...)  // tenant-scope 由
                    // TenantGuard 校验
                    //   .anyRequest().authenticated()
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(o -> o.jwt(jwt -> {}));
    return http.build();
  }

  /**
   * CORS 配置：让 next.js dev server (http://localhost:3000) 能调本后端。 与 aspnetcore 端的 AddCors("NextDev")
   * 对称。 Production：用 allowedOriginPatterns 指定具体域名；这里 localhost:* 是 dev-only。
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.addAllowedOrigin("http://localhost:3000");
    config.addAllowedMethod("*");
    config.addAllowedHeader("*");
    config.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

  /**
   * Dev JwtDecoder — 信任 MSW/dev-helper 发的 alg=none dev token（Authorization header: {@code Bearer
   * eyJhbGciOiJub25lIn0.eyJ...dev-placeholder}）。
   *
   * <p>手动 parse payload（base64url → JSON），只校验 exp 不过期；trust signature / issuer / audience。 Spring
   * 默认的 NimbusJwtDecoder 会拒 alg=none，因为 PG/asymmetric key 路径走不通。
   *
   * <p>Production：删除这个 bean + 在 application.yml 配 {@code
   * spring.security.oauth2.resourceserver.jwt.issuer-uri} 指向真实 OAuth2 server， Spring Boot 自动配置
   * NimbusJwtDecoder 用 JWKS 验签。
   *
   * <p>Authorization header 解码示例（next.js dev token）：
   *
   * <pre>
   *   header  = {"alg":"none"}
   *   payload = {"sub":"...","tenant_id":"00000000-...","exp":1786631755}
   *   sig     = dev-placeholder
   * </pre>
   */
  @Bean
  public JwtDecoder jwtDecoder() {
    return new DevJwtDecoder();
  }

  static class DevJwtDecoder implements JwtDecoder {
    // 命名 static 常量，避免每次 decode 都新建匿名 TypeReference 子类（也消除 SpotBugs
    // SIC_INNER_SHOULD_BE_STATIC_ANON）。
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Jwt decode(String token) throws JwtException {
      try {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
          throw new JwtException("Malformed JWT: expected 3 segments, got " + parts.length);
        }

        // base64url decode header + payload
        Map<String, Object> headers =
            mapper.readValue(
                new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8),
                MAP_TYPE);
        Map<String, Object> claims =
            mapper.readValue(
                new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8),
                MAP_TYPE);

        Instant now = Instant.now();
        // dev token 可能已过期（前端写死的 exp ≈ 2026-08-13 14:36 UTC），
        // 如果过期就把 Jwt.exp 延长到 +1h，避免 JwtTimestampValidator 拒绝。
        // Production JwtDecoder 不会这么做。
        Instant tokenExp =
            claims.get("exp") instanceof Number n
                ? Instant.ofEpochSecond(n.longValue())
                : now.plusSeconds(3600);
        Instant effectiveExp = tokenExp.isBefore(now) ? now.plusSeconds(3600) : tokenExp;

        return new Jwt(token, now, effectiveExp, headers, claims);
      } catch (JwtException e) {
        throw e;
      } catch (Exception e) {
        throw new JwtException("Failed to decode dev JWT: " + e.getMessage(), e);
      }
    }
  }
}
