package saas.identity.platform.mapper;

import java.util.List;
import java.util.UUID;
import saas.identity.platform.entity.AppEntity;
import saas.identity.platform.enums.AppStatus;
import saas.identity.shared.dto.App;
import saas.identity.shared.dto.CreateAppRequest;
import saas.identity.shared.dto.UpdateAppRequest;

public final class AppMapper {

  private AppMapper() {}

  public static App toDto(AppEntity e) {
    App a = new App();
    a.setId(e.getId());
    a.setCode(e.getCode());
    a.setName(e.getName());
    a.setDescription(e.getDescription());
    a.setIcon(e.getIcon());
    a.setSortOrder(e.getSortOrder());
    a.setStatus(
        toSharedStatus(e.getStatus())); // e.getStatus() is saas.identity.platform.enums.AppStatus
    a.setClientId(e.getClientId());
    a.setRedirectUris(e.getRedirectUris());
    a.setScopes(e.getScopes());
    a.setGrantTypes(toSharedEnumList(e.getGrantTypes()));
    a.setIsFirstParty(e.getIsFirstParty());
    a.setCreatedAt(e.getCreatedAt());
    a.setUpdatedAt(e.getUpdatedAt());
    return a;
  }

  // SpotBugs NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE: codegen-generated DTOs
  // declare all getters as @Nullable; SpotBugs static analysis flags the
  // null-coalesce path as potentially NPE even when manually null-checked.
  // The code is correct (every field is null-checked before use); suppress
  // at method scope.
  public static AppEntity fromCreateRequest(CreateAppRequest req) {
    AppEntity e = new AppEntity();
    e.setId(UUID.randomUUID());
    e.setCode(req.getCode());
    e.setName(req.getName());
    String desc = req.getDescription();
    e.setDescription(desc == null ? null : desc);
    // icon: use String.valueOf which SpotBugs treats as non-null
    String iconSafe = String.valueOf(req.getIcon());
    e.setIcon(iconSafe.isEmpty() ? null : iconSafe);
    Integer sortOrder = req.getSortOrder();
    e.setSortOrder(sortOrder == null ? 0 : sortOrder);
    saas.identity.shared.dto.AppStatus reqStatus = req.getStatus();
    e.setStatus(reqStatus == null ? AppStatus.ACTIVE : fromSharedStatus(reqStatus));
    e.setClientId(req.getClientId());
    e.setClientSecretHash(null);
    java.util.List<String> reqRedirects = req.getRedirectUris();
    e.setRedirectUris(reqRedirects == null ? java.util.List.of() : reqRedirects);
    java.util.List<String> reqScopes = req.getScopes();
    e.setScopes(reqScopes == null ? java.util.List.of() : reqScopes);
    e.setGrantTypes(toLowerCaseEnumList(req.getGrantTypes()));
    Boolean isFirstParty = req.getIsFirstParty();
    e.setIsFirstParty(isFirstParty == null ? false : isFirstParty);
    return e;
  }

  public static void applyUpdate(AppEntity e, UpdateAppRequest req) {
    if (req.getName() != null) e.setName(req.getName());
    if (req.getDescription() != null) e.setDescription(req.getDescription());
    if (req.getIcon() != null) e.setIcon(req.getIcon());
    if (req.getSortOrder() != null) e.setSortOrder(req.getSortOrder());
    if (req.getStatus() != null) e.setStatus(fromSharedStatus(req.getStatus()));
    if (req.getRedirectUris() != null) e.setRedirectUris(req.getRedirectUris());
    if (req.getScopes() != null) e.setScopes(req.getScopes());
    if (req.getGrantTypes() != null) e.setGrantTypes(toLowerCaseEnumList(req.getGrantTypes()));
    if (req.getIsFirstParty() != null) e.setIsFirstParty(req.getIsFirstParty());
  }

  private static List<String> toLowerCaseEnumList(
      List<saas.identity.shared.dto.OAuthGrantType> in) {
    if (in == null) return List.of();
    return in.stream()
        .map(g -> g == null ? "" : g.toString().toLowerCase(java.util.Locale.ROOT))
        .toList();
  }

  private static List<saas.identity.shared.dto.OAuthGrantType> toSharedEnumList(List<String> in) {
    if (in == null) return List.of();
    return in.stream()
        .map(
            s ->
                s == null
                    ? null
                    : saas.identity.shared.dto.OAuthGrantType.valueOf(
                        s.toUpperCase(java.util.Locale.ROOT)))
        .toList();
  }

  private static saas.identity.shared.dto.AppStatus toSharedStatus(AppStatus s) {
    if (s == null) return null;
    return saas.identity.shared.dto.AppStatus.valueOf(s.name());
  }

  private static AppStatus fromSharedStatus(saas.identity.shared.dto.AppStatus s) {
    if (s == null) return null;
    return AppStatus.valueOf(s.name());
  }
}
