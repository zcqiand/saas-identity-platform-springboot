package saas.identity.platform.controller;

// @impl M04.F01.I06 — book anchor (xr-know-007)

import java.util.NoSuchElementException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import saas.identity.platform.entity.Generated.OauthClient;
import saas.identity.platform.repository.OauthClientRepository;
import saas.identity.shared.api.ClientsApi;
import saas.identity.shared.dto.OAuthClientPublicInfo;

/** M04.F01.I06 公共 client 元数据 — anonymous GET /api/v1/clients/{clientId}。 */
@RestController
public class ClientsController implements ClientsApi {

  private final OauthClientRepository clients;

  public ClientsController(OauthClientRepository clients) {
    this.clients = clients;
  }

  @Override
  public ResponseEntity<OAuthClientPublicInfo> clientsGetClient(String clientId) {
    OauthClient c =
        clients
            .findByClientId(clientId)
            .orElseThrow(() -> new NoSuchElementException("client " + clientId + " not found"));
    OAuthClientPublicInfo info = new OAuthClientPublicInfo();
    info.setClientId(c.getClientId());
    info.setClientName(c.getClientName());
    // 2026-09-12 live 4-way 修复（R6）：status 是契约 required 字段（msw oracle 返 int，
    // 1=active），此前漏 set → 序列化 null → normalize 后与 msw 分叉。
    info.setStatus(c.getStatus() == null ? null : c.getStatus().intValue());
    return ResponseEntity.ok(info);
  }
}
