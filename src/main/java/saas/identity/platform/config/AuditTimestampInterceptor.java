package saas.identity.platform.config;

// @impl M00.F02.I08 — book anchor (xr-know-007)

import java.time.OffsetDateTime;
import org.hibernate.Interceptor;
import org.hibernate.type.Type;

/**
 * 2026-09-12 live 4-way 修复（R2）：审计列 INSERT 兜底。
 *
 * <p>POST /clients/{clientId}/menus 与 /oauth/authorize 的 INSERT 因 created_at 为 null 被 PG NOT NULL
 * 拒（23502 → 400 `constraint violation`）。JPA 对显式 null 属性不走 DB DEFAULT，而 Generated/ 实体由
 * scaffold-entities.sh 从真库反推再生成——手写 @PrePersist 叠进去会在下次重生成 被抹掉。故用 Hibernate 全局 Interceptor 在 onSave
 * 阶段统一兜底：createdAt / updatedAt 为 null 时填 now()，业务代码已显式赋值（AdminClientsController 等）不被覆盖。
 */
public class AuditTimestampInterceptor implements Interceptor {

  @Override
  public boolean onSave(
      Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) {
    OffsetDateTime now = OffsetDateTime.now();
    boolean changed = false;
    for (int i = 0; i < propertyNames.length; i++) {
      if (state[i] != null) {
        continue;
      }
      if ("createdAt".equals(propertyNames[i])) {
        state[i] = now;
        changed = true;
      } else if ("updatedAt".equals(propertyNames[i])) {
        state[i] = now;
        changed = true;
      }
    }
    return changed;
  }
}
