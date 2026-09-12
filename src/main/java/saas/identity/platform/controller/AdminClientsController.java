package saas.identity.platform.controller;

import java.time.OffsetDateTime;
import java.util.NoSuchElementException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import saas.identity.platform.entity.Generated.OauthClient;
import saas.identity.platform.repository.OauthClientRepository;
import saas.identity.shared.api.AdminClientsApi;
import saas.identity.shared.dto.AdminClientsListClients200Response;
import saas.identity.shared.dto.AdminClientsSetClientStatusRequest;
import saas.identity.shared.dto.CreateOAuthClientRequest;
import saas.identity.shared.dto.OAuthClient;
import saas.identity.shared.dto.UpdateOAuthClientRequest;

/** M04.F01 OAuth 应用 CRUD + M04.F02 启停。skeleton。 */
@RestController
public class AdminClientsController implements AdminClientsApi {

  private final OauthClientRepository clients;

  public AdminClientsController(OauthClientRepository clients) {
    this.clients = clients;
  }

  @Override
  public ResponseEntity<AdminClientsListClients200Response> adminClientsListClients(
      Integer page, Integer pageSize) {
    int p = page == null ? 0 : page;
    int ps = pageSize == null ? 20 : pageSize;
    var pg = clients.findAll(PageRequest.of(p, ps));
    AdminClientsListClients200Response resp = new AdminClientsListClients200Response();
    resp.setItems(pg.getContent().stream().map(this::toDto).toList());
    resp.setTotal(pg.getTotalElements());
    resp.setPage(p);
    resp.setPageSize(ps);
    return ResponseEntity.ok(resp);
  }

  @Override
  public ResponseEntity<OAuthClient> adminClientsCreateClient(CreateOAuthClientRequest body) {
    OauthClient e = new OauthClient();
    e.setClientId(body.getClientId());
    e.setClientSecret(body.getClientSecret());
    e.setClientName(body.getClientName());
    e.setGrantTypes(body.getGrantTypes());
    e.setRedirectUris(body.getRedirectUris());
    e.setScopes(body.getScopes());
    // NOT NULL 列必须显式赋值（Hibernate scaffold 不推 NotNull 默认值，同 23502 教训）。
    // SSOT CreateOAuthClientRequest 里 validity 是可选 int32 —— 缺省/非正落家族应用层默认
    // 3600/86400（msw oracle 与 nextjs 同口径；不是 DB 列 DEFAULT 的 7200/2592000，
    // 2026-09-12 four-way I45 分叉修复）。
    e.setAccessTokenValidity(
        body.getAccessTokenValidity() != null && body.getAccessTokenValidity() > 0
            ? body.getAccessTokenValidity()
            : 3600);
    e.setRefreshTokenValidity(
        body.getRefreshTokenValidity() != null && body.getRefreshTokenValidity() > 0
            ? body.getRefreshTokenValidity()
            : 86400);
    e.setAutoApprove(body.getAutoApprove() != null ? body.getAutoApprove() : false);
    e.setStatus((short) 1);
    e.setCreatedAt(OffsetDateTime.now());
    e.setUpdatedAt(OffsetDateTime.now());
    OauthClient saved = clients.save(e);
    return ResponseEntity.ok(toDto(saved));
  }

  @Override
  public ResponseEntity<OAuthClient> adminClientsGetClient(String clientId) {
    OauthClient e =
        clients
            .findByClientId(clientId)
            .orElseThrow(() -> new NoSuchElementException("client " + clientId));
    return ResponseEntity.ok(toDto(e));
  }

  @Override
  public ResponseEntity<OAuthClient> adminClientsUpdateClient(
      String clientId, UpdateOAuthClientRequest body) {
    OauthClient e =
        clients
            .findByClientId(clientId)
            .orElseThrow(() -> new NoSuchElementException("client " + clientId));
    if (body.getClientName() != null) e.setClientName(body.getClientName());
    if (body.getRedirectUris() != null) e.setRedirectUris(body.getRedirectUris());
    if (body.getScopes() != null) e.setScopes(body.getScopes());
    return ResponseEntity.ok(toDto(clients.save(e)));
  }

  @Override
  public ResponseEntity<Void> adminClientsDeleteClient(String clientId) {
    OauthClient e =
        clients
            .findByClientId(clientId)
            .orElseThrow(() -> new NoSuchElementException("client " + clientId));
    clients.deleteById(e.getId());
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<OAuthClient> adminClientsSetClientStatus(
      String clientId, AdminClientsSetClientStatusRequest body) {
    OauthClient e =
        clients
            .findByClientId(clientId)
            .orElseThrow(() -> new NoSuchElementException("client " + clientId));
    if (body.getStatus() != null) e.setStatus(body.getStatus().shortValue());
    return ResponseEntity.ok(toDto(clients.save(e)));
  }

  private OAuthClient toDto(OauthClient e) {
    OAuthClient d = new OAuthClient();
    d.setId(e.getId());
    d.setClientId(e.getClientId());
    d.setClientName(e.getClientName());
    d.setGrantTypes(e.getGrantTypes());
    d.setRedirectUris(e.getRedirectUris());
    d.setScopes(e.getScopes());
    d.setStatus(e.getStatus() == null ? null : e.getStatus().intValue());
    d.setAutoApprove(e.getAutoApprove());
    // 2026-09-12 four-way I45 分叉修复：validity 与审计列是响应契约字段，此前漏映射
    // → 序列化 null → normalize 剔 null 后与 msw oracle（3600/86400 + 时间戳非空）分叉。
    d.setAccessTokenValidity(e.getAccessTokenValidity());
    d.setRefreshTokenValidity(e.getRefreshTokenValidity());
    d.setCreatedAt(e.getCreatedAt());
    d.setUpdatedAt(e.getUpdatedAt());
    return d;
  }
}
