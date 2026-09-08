package saas.identity.platform.controller;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import saas.identity.platform.entity.Generated.OauthClient;
import saas.identity.platform.entity.Generated.OauthCode;
import saas.identity.platform.repository.OauthClientRepository;
import saas.identity.platform.repository.OauthCodeRepository;
import saas.identity.platform.security.JwtIssuer;
import saas.identity.shared.api.OauthApi;
import saas.identity.shared.dto.AuthorizeCodeRequest;
import saas.identity.shared.dto.OAuthAuthorize200Response;
import saas.identity.shared.dto.TokenRequest;
import saas.identity.shared.dto.TokenResponse;

/** M04.F03 OAuth authorize + token + refresh（ADR-0020 路线 A）。skeleton。 */
@RestController
public class OauthController implements OauthApi {

  private final OauthClientRepository clients;
  private final OauthCodeRepository codes;
  private final JwtIssuer jwt;

  public OauthController(OauthClientRepository clients, OauthCodeRepository codes, JwtIssuer jwt) {
    this.clients = clients;
    this.codes = codes;
    this.jwt = jwt;
  }

  @Override
  public ResponseEntity<OAuthAuthorize200Response> oAuthAuthorize(AuthorizeCodeRequest body) {
    OauthClient client = clients.findByClientId(body.getClientId())
        .orElseThrow(() -> new IllegalArgumentException("unknown client_id"));
    UUID userId = UUID.randomUUID();
    UUID tenantId = UUID.randomUUID();
    String code = "ac_" + UUID.randomUUID();

    OauthCode row = new OauthCode();
    row.setCode(code);
    row.setClientId(client.getClientId());
    row.setUserId(userId);
    row.setTenantId(tenantId);
    row.setExpiresAt(OffsetDateTime.now().plusMinutes(5));
    codes.save(row);

    OAuthAuthorize200Response resp = new OAuthAuthorize200Response();
    resp.setCode(code);
    resp.setState(body.getState());
    return ResponseEntity.ok(resp);
  }

  @Override
  public ResponseEntity<TokenResponse> oAuthToken(TokenRequest body) {
    OauthClient client = clients.findByClientId(body.getClientId())
        .orElseThrow(() -> new IllegalArgumentException("unknown client_id"));
    UUID userId = UUID.randomUUID();
    UUID tenantId = UUID.randomUUID();

    String accessToken = jwt.issueAccessToken(userId, tenantId);
    String refreshToken = "rt_" + UUID.randomUUID();

    TokenResponse resp = new TokenResponse();
    resp.setAccessToken(accessToken);
    resp.setRefreshToken(refreshToken);
    resp.setTokenType("Bearer");
    resp.setExpiresIn(3600);
    resp.setUserId(userId.toString());
    resp.setClientId(client.getClientId());
    resp.setTenantId(tenantId);
    return ResponseEntity.ok(resp);
  }
}
