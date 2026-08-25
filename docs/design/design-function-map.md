# 设计与功能对齐 — SaaS 身份平台SpringBoot后端

> 人填、人评审。机器只检查功能 ID 存在性。
> 回答一个问题：**这个功能子项，落到哪段代码、哪张表、哪个权限码上？**
> 答不上来的行，说明设计没做完，别开工。

## 映射表

| 功能子项 ID | 页面/组件 | 接口 | 数据表 | 权限码 | 设计稿 | 状态 |
|---|---|---|---|---|---|---|
| M04.F02.I06 | OauthController#oAuthAuthorize / OauthService#authorize | POST /api/v1/oauth/authorize | oauth_codes（V009，存 saas-code-{ts}-{rand}，TTL 10min）+ apps（clientId/redirect_uris/scopes 校验） | M04.F02.I06 | - | 已上线 |
| M04.F02.I07 | OauthController#oAuthToken / OauthService#token（grant_type=authorization_code） | POST /api/v1/oauth/token | oauth_codes（验未消费/未过期/redirectUri 一致 → 标 consumed）+ apps | M04.F02.I07 | - | 已上线 |
| M04.F02.I08 | OauthController#oAuthToken / OauthService#token（grant_type=refresh_token） | POST /api/v1/oauth/token | oauth_codes（refresh_token 行 TTL 7d，旋转换发：旧 consumed 新写入） | M04.F02.I08 | - | 已上线 |

> 签发统一走 JwtIssuer（HS256，JWT_SIGNING_KEY env，≥32B）。
> 本仓其余已上线条目的设计映射待补（v0.2.x 前的 M00/M01 走通用 auth 链路），
> 本批次只登记 v0.2.0 Phase 6 真 OAuth 三接口 —— 对应 L5 软告警清零。
