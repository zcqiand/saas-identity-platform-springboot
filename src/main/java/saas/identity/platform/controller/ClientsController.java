package saas.identity.platform.controller;

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
    OauthClient c = clients.findByClientId(clientId)
        .orElseThrow(() -> new NoSuchElementException("client " + clientId + " not found"));
    OAuthClientPublicInfo info = new OAuthClientPublicInfo();
    info.setClientId(c.getClientId());
    info.setClientName(c.getClientName());
    return ResponseEntity.ok(info);
  }
}
