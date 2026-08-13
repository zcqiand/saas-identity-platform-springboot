package saas.identity.platform.mapper;

import java.util.List;
import java.util.UUID;
import saas.identity.platform.entity.AppEntity;
import saas.identity.shared.dto.CreateOAuthAppRequest;
import saas.identity.shared.dto.OAuthApp;
import saas.identity.shared.dto.UpdateOAuthAppRequest;

public final class AppMapper {

  private AppMapper() {}

  public static OAuthApp toDto(AppEntity e) {
    OAuthApp a = new OAuthApp();
    a.setId(e.getId());
    a.setClientId(e.getClientId());
    a.setName(e.getName());
    a.setRedirectUris(e.getRedirectUris());
    a.setScopes(e.getScopes());
    a.setGrantTypes(e.getGrantTypes());
    a.setIsFirstParty(e.getIsFirstParty());
    a.setCreatedAt(e.getCreatedAt());
    return a;
  }

  public static AppEntity fromCreateRequest(CreateOAuthAppRequest req) {
    AppEntity e = new AppEntity();
    e.setId(UUID.randomUUID());
    // code / clientId / clientSecret / description / icon / sortOrder / status /
    // isFirstParty 不在 CreateOAuthAppRequest DTO（NSwag 简化生成）。
    // Phase 5 重新跑 openapi-generator 补全字段后这里再加。
    e.setCode("");
    e.setName(req.getName());
    e.setStatus(saas.identity.platform.enums.AppStatus.ACTIVE);
    e.setClientId(UUID.randomUUID().toString());
    e.setClientSecretHash(null);
    e.setRedirectUris(req.getRedirectUris() != null ? req.getRedirectUris() : List.of());
    e.setScopes(req.getScopes() != null ? req.getScopes() : List.of());
    e.setGrantTypes(toLowerCaseList(req.getGrantTypes()));
    e.setIsFirstParty(false);
    return e;
  }

  public static void applyUpdate(AppEntity e, UpdateOAuthAppRequest req) {
    if (req.getName() != null) e.setName(req.getName());
    if (req.getRedirectUris() != null) e.setRedirectUris(req.getRedirectUris());
    if (req.getScopes() != null) e.setScopes(req.getScopes());
    if (req.getGrantTypes() != null) e.setGrantTypes(toLowerCaseList(req.getGrantTypes()));
  }

  private static List<String> toLowerCaseList(List<String> in) {
    if (in == null) return List.of();
    return in.stream().map(s -> s == null ? "" : s.toLowerCase(java.util.Locale.ROOT)).toList();
  }
}
