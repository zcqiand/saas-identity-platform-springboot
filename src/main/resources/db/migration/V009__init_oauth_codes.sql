-- V009__init_oauth_codes.sql
-- Phase 6 真 OAuth 的 code + refresh_token 存储, 镜像 shared/sql/migrations/V014。
-- saas-aspnetcore (AppDbContext) + saas-springboot (JPA) 都映射这张表。

CREATE TABLE oauth_codes (
    id              UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    code            VARCHAR(255) NOT NULL,                                  -- auth code 或 refresh token (统一存)
    grant_type      VARCHAR(32)  NOT NULL DEFAULT 'authorization_code'
                    CHECK (grant_type IN ('authorization_code', 'refresh_token')),
    app_id          UUID         NOT NULL,
    user_id         UUID,                                                   -- authorization_code 交换时填
    tenant_id       UUID         NOT NULL,
    redirect_uri    VARCHAR(2048),
    scope           VARCHAR(512),
    expires_at      TIMESTAMPTZ  NOT NULL,
    consumed_at     TIMESTAMPTZ,                                            -- 一次性消费标记
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT oauth_codes_code_unique UNIQUE (code),
    CONSTRAINT oauth_codes_app_fk FOREIGN KEY (app_id)
        REFERENCES apps (id) ON DELETE CASCADE
);

CREATE INDEX idx_oauth_codes_app_id     ON oauth_codes (app_id);
CREATE INDEX idx_oauth_codes_expires_at ON oauth_codes (expires_at);
CREATE INDEX idx_oauth_codes_user_id    ON oauth_codes (user_id);

COMMENT ON TABLE  oauth_codes IS               'OAuth 2.0 authorization_code + refresh_token 存储；Phase 6 替代 saas-nextjs 进程内 oauth-store';
COMMENT ON COLUMN oauth_codes.grant_type IS    'authorization_code (TTL 10min) 或 refresh_token (TTL 7d)';
COMMENT ON COLUMN oauth_codes.code IS          'auth code 或 refresh token；前者一次性消费（consumed_at 非 NULL），后者可旋转换发';