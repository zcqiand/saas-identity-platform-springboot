# CHANGELOG — saas-identity-platform-springboot

格式参照 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)。

## [0.2.7] — 2026-08-28

- fix(deps): springdoc-openapi 2.6.0 → 2.8.9。spring-web 6.2（Boot 3.4 自带）删除
  `ControllerAdviceBean.<init>(Object)`，springdoc < 2.7.0 调它 → 线上 `/v3/api-docs`
  运行时 `NoSuchMethodError`（springdoc #2687）。编译期与单测无感知。
- test: 新增 `SpringdocCompatibilityTest` 类路径级二进制兼容守卫（升级前实测红）。

## [0.2.4] — 2026-08-27

- 初始化台账：Java 21 + Spring Boot 3.4 后端。历史变更见 git log 与 `.state/session.json`。
