package saas.identity.platform.service;

import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import saas.identity.platform.entity.TenantEntity;
import saas.identity.platform.mapper.TenantMapper;
import saas.identity.platform.repository.TenantRepository;
import saas.identity.shared.dto.CreateTenantRequest;
import saas.identity.shared.dto.Tenant;
import saas.identity.shared.dto.UpdateTenantRequest;

/** M00.F01 — 平台 admin 租户 CRUD。 v0.4.0：从 InMemoryStore 迁到 TenantRepository 真实 DB。 */
@Service
public class AdminTenantService {

  private final TenantRepository tenantRepository;

  public AdminTenantService(TenantRepository tenantRepository) {
    this.tenantRepository = tenantRepository;
  }

  @Transactional(readOnly = true)
  public Page<Tenant> list(int page, int pageSize) {
    Pageable p = PageRequest.of(page, pageSize);
    return tenantRepository.findAll(p).map(TenantMapper::toDto);
  }

  @Transactional
  public Tenant create(CreateTenantRequest body) {
    TenantEntity e = TenantMapper.fromCreateRequest(body);
    return TenantMapper.toDto(tenantRepository.save(e));
  }

  @Transactional(readOnly = true)
  public Tenant get(UUID id) {
    return TenantMapper.toDto(
        tenantRepository
            .findById(id)
            .orElseThrow(() -> new NoSuchElementException("tenant " + id + " not found")));
  }

  @Transactional
  public Tenant update(UUID id, UpdateTenantRequest body) {
    TenantEntity e =
        tenantRepository
            .findById(id)
            .orElseThrow(() -> new NoSuchElementException("tenant " + id + " not found"));
    TenantMapper.applyUpdate(e, body);
    return TenantMapper.toDto(tenantRepository.save(e));
  }

  @Transactional
  public void delete(UUID id) {
    tenantRepository.findById(id).ifPresent(tenantRepository::delete);
  }
}
