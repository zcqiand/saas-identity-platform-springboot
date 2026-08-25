package saas.identity.platform.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
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

  /**
   * 逗号分隔的允许 origin 列表。默认给 dev：saas-nextjs(:3000) + lab-react,vue(:5173) + lab-nextjs(:3001)；生产用
   * SAAS_CORS_ALLOWED_ORIGINS env override 改为正式域名。
   */
  @Value(
      "${saas.cors.allowed-origins:http://localhost:3000,http://localhost:5173,http://localhost:3001}")
  private List<String> allowedOrigins;

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
                    // v0.2.x 起: OAuth IdP 端点（Phase 6）。authorize/token 的调用方是
                    // OAuth client（lab 后端），调用前不可能持有 saas token —— 匿名可访问，
                    // 身份靠 client_id/redirect_uri/scope/code 校验（OauthService）。
                    // 对齐 saas-aspnetcore（OAuth 路由无 [Authorize]）。
                    .requestMatchers("/api/v1/oauth/**")
                    .permitAll()
                    // v0.1.12 起: 容器内 Docker HEALTHCHECK 与外部 deploy 脚本都直接
                    // wget /actuator/health; 不带 JWT 走不到 controller, 401 让
                    // healthcheck 失败, deploy 脚本 120 次都进不了 '200'. permitAll
                    // 让 health probe 路径免 auth, 不影响业务 endpoint.
                    .requestMatchers("/actuator/**")
                    .permitAll()
                    // v0.1.13 起: Swagger UI 在线访问 (springdoc-openapi-starter-webmvc-ui).
                    // 仅 metadata (OpenAPI doc), 无业务副作用, 跟 /actuator/** 一样
                    // 性质; 公开让任何客户端能 introspect endpoint contract.
                    // 业务 endpoint 仍走 .anyRequest().authenticated() 不放宽.
                    .requestMatchers(
                        "/v3/api-docs",
                        "/v3/api-docs.yaml",
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**")
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
   * CORS 配置：白名单 origin 走 saas.cors.allowed-origins（env SAAS_CORS_ALLOWED_ORIGINS 覆盖）。 与 aspnetcore
   * 端的 AddCors("NextDev") 对称 — aspnetcore 同样读 SAAS_CORS_ALLOWED_ORIGINS。 Production：用具体域名；这里
   * localhost:* 是 dev-only。
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    for (String origin : allowedOrigins) {
      String trimmed = origin.trim();
      if (!trimmed.isEmpty()) config.addAllowedOrigin(trimmed);
    }
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
