package saas.identity.platform.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import saas.identity.platform.repository.TenantMemberRepository;
import saas.identity.shared.api.MeApi;
import saas.identity.shared.dto.CurrentUser;
import saas.identity.shared.dto.EffectiveMenuNode;
import saas.identity.shared.dto.SwitchTenantResponse;
import saas.identity.shared.dto.TenantMember;

/** M01.F01/F03 当前用户视图 + 跨租户。skeleton：返回占位数据。 */
@RestController
public class MeController implements MeApi {

  private final TenantMemberRepository members;

  public MeController(TenantMemberRepository members) {
    this.members = members;
  }

  @Override
  public ResponseEntity<CurrentUser> meWhoami() {
    CurrentUser u = new CurrentUser();
    u.setCurrentTenantId(UUID.randomUUID());
    return ResponseEntity.ok(u);
  }

  @Override
  public ResponseEntity<List<TenantMember>> meListMyTenants(String clientId) {
    return ResponseEntity.ok(List.of());
  }

  @Override
  public ResponseEntity<SwitchTenantResponse> meSwitchTenant(String tenantId, String clientId) {
    SwitchTenantResponse r = new SwitchTenantResponse();
    r.setTenantId(UUID.fromString(tenantId));
    return ResponseEntity.ok(r);
  }

  @Override
  public ResponseEntity<Map<String, List<EffectiveMenuNode>>> meGetMyMenus(String clientId) {
    return ResponseEntity.ok(Map.of());
  }
}
