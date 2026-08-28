package saas.identity.platform.config;

import java.util.List;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
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

  /**
   * 公开 OAuth IdP 端点过滤器链 — 在主链之前生效（@Order 更小优先级更高）。 2026-08-28 lab-nextjs 登录页 502 修复：原主链把
   * /api/v1/oauth/** 配 permitAll，但 Spring Security 6.x 的 `BearerTokenAuthenticationFilter`（来自
   * `.oauth2ResourceServer(jwt)`）会在 AuthorizationFilter 之前判缺 Bearer → 401，AuthorizationFilter 的
   * permitAll 永远拿不到机会。 拆独立链： 这条链只匹配 /api/v1/oauth/**，完全不带 oauth2ResourceServer，无 Bearer 校验。
   */
  @Bean
  @Order(1)
  public SecurityFilterChain oauthFilterChain(HttpSecurity http) throws Exception {
    http.securityMatcher("/api/v1/oauth/**")
        .csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(authz -> authz.anyRequest().permitAll());
    return http.build();
  }

  @Bean
  @Order(2)
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            authz ->
                authz
                    .requestMatchers("/api/v1/auth/**")
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
   * JwtDecoder — HS256 真验签（RFC 7519）。 与 JwtIssuer 共享同一把 JWT_SIGNING_KEY， 因此 saas-msw /
   * saas-nextjs-self 签出来的 token 在本仓 dev profile 也能验签通过。
   *
   * <p>v0.2.x Phase 2 起删除旧 DevJwtDecoder（alg=none 占位）路径，因为： 1) MSW 现在 真签 HS256（ADR-0012 v0.3.0 +
   * Phase 1A），MSW 不再发 alg=none fixture； 2) HS256 真签发让 NimbusJwtDecoder 标准路径走通，不需要 dev 兜底； 3) 「dev
   * profile 单独验签 路径」是 alg=none 时代的妥协，Phase 2 后不再需要。
   *
   * <p>Production：配 {@code spring.security.oauth2.resourceserver.jwt.issuer-uri} env 自动切 JWKS 验签；本
   * bean 在 prod profile 可被覆盖（如多 IdP 场景）或直接删除。
   */
  @Bean
  public JwtDecoder jwtDecoder(@Value("${JWT_SIGNING_KEY:}") String signingKey) {
    if (signingKey == null || signingKey.isEmpty()) {
      throw new IllegalStateException("JWT_SIGNING_KEY env not configured (Phase 2: HS256 真签验签必备)");
    }
    if (signingKey.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < 32) {
      throw new IllegalStateException(
          "JWT_SIGNING_KEY must be >=32 bytes for HS256 (got "
              + signingKey.getBytes(java.nio.charset.StandardCharsets.UTF_8).length
              + ")");
    }
    return NimbusJwtDecoder.withSecretKey(
            new SecretKeySpec(
                signingKey.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256"))
        .macAlgorithm(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256)
        .build();
  }
}
