package saas.identity.platform.repository;

import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @DataJpaTest 切片需要显式 import 一些 auto-configuration 以保证 Flyway 跑 V*.sql。
 */
@Configuration
@Import({FlywayAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class RepositoryTestFactoryConfig {}
