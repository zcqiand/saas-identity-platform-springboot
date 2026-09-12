package saas.identity.platform.config;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 把 {@link AuditTimestampInterceptor} 挂进 Hibernate SessionFactory（显式注册，不靠隐式探测）。 */
@Configuration
public class HibernateAuditConfig {

  @Bean
  public AuditTimestampInterceptor auditTimestampInterceptor() {
    return new AuditTimestampInterceptor();
  }

  @Bean
  public HibernatePropertiesCustomizer auditTimestampCustomizer(
      AuditTimestampInterceptor interceptor) {
    return properties -> properties.put(AvailableSettings.INTERCEPTOR, interceptor);
  }
}
