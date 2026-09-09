# 设计与功能对齐 — SaaS 身份平台SpringBoot后端

> 人填、人评审。机器只检查功能 ID 存在性。
> 回答一个问题：**这个功能子项，落到哪段代码、哪张表、哪个权限码上？**
> 答不上来的行，说明设计没做完，别开工。

## 映射表

| 功能子项 ID | 页面/组件 | 接口 | 数据表 | 权限码 | 设计稿 | 状态 |
|---|---|---|---|---|---|---|
| M04.F03.I01 | OauthController#oAuthAuthorize / OauthService#authorize | POST /api/v1/oauth/authorize | oauth_codes（V009，存 saas-code-{ts}-{rand}，TTL 10min）+ apps（clientId/redirect_uris/scopes 校验） | M04.F03.I01 | - | 已上线 |
| M04.F03.I02 | OauthController#oAuthToken / OauthService#token（grant_type=authorization_code） | POST /api/v1/oauth/token | oauth_codes（验未消费/未过期/redirectUri 一致 → 标 consumed）+ apps | M04.F03.I02 | - | 已上线 |
| M04.F03.I03 | OauthController#oAuthToken / OauthService#token（grant_type=refresh_token） | POST /api/v1/oauth/token | oauth_codes（refresh_token 行 TTL 7d，旋转换发：旧 consumed 新写入） | M04.F03.I03 | - | 已上线 |
| M01.F01.I02 | TenantUsersController#createUser / TenantUsersService#createUser | POST /api/v1/tenants/{tenantId}/users | users（status 默认 'active'，TenantUserMapper:48）+ tenant_memberships | M01.F01.I02 | - | 已上线 |
| M05.F01.I05 | TenantApiKeysController#deleteApiKey / TenantApiKeyService#delete（物理删） | DELETE /api/v1/tenants/{tenantId}/api-keys/{keyId} | api_keys（hard delete，无 audit；404 by GlobalExceptionHandler NoSuchElementException→NOT_FOUND） | M05.F01.I05 | - | 已上线 |
| M04.F04.I08 | MeController#membershipMenuIds / MenuService#resolveMembershipMenuIds | GET /api/v1/me/menus（装配第一步） | role_menu_grants + tenant_memberships（LEFT JOIN 取真 roleIds） | M04.F04.I08 | - | 已上线 |
| M04.F04.I08 | MenuService#assembleMenuTree | GET /api/v1/me/menus（装配第二步：菜单树父链补全） | menus（parentId 链 → EffectiveMenuNode[]） | M04.F04.I08 | - | 已上线 |
| M04.F04.I08 | MenuService#groupMenusByApp | GET /api/v1/me/menus（装配第三步：app.code 分组输出） | apps（按 appCode 输出 Map<appCode, List<EffectiveMenuNode>>） | M04.F04.I08 | - | 已上线 |
| M08.F01.I01 | AdminAppMenusController#listMenus / AdminAppMenusService#list | GET /api/v1/admin/apps/{appId}/menus | menus（按 appId 过滤，扁平列表） | M08.F01.I01 | - | 已上线 |
| M08.F01.I02 | AdminAppMenusController#createMenu / AdminAppMenusService#create | POST /api/v1/admin/apps/{appId}/menus | menus（INSERT） | M08.F01.I02 | - | 已上线 |
| M08.F01.I03 | AdminAppMenusController#getMenu / AdminAppMenusService#get | GET /api/v1/admin/apps/{appId}/menus/{menuId} | menus（PK 查询） | M08.F01.I03 | - | 已上线 |
| M08.F01.I04 | AdminAppMenusController#updateMenu / AdminAppMenusService#update | PATCH /api/v1/admin/apps/{appId}/menus/{menuId} | menus（UPDATE） | M08.F01.I04 | - | 已上线 |
| M08.F01.I05 | AdminAppMenusController#deleteMenu / AdminAppMenusService#delete | DELETE /api/v1/admin/apps/{appId}/menus/{menuId} | menus（DELETE，NoSuchElementException→NOT_FOUND by GlobalExceptionHandler） | M08.F01.I05 | - | 已上线 |
| M08.F02.I06 | AdminAppMenusController#reorderMenus / AdminAppMenusService#reorder | PUT /api/v1/admin/apps/{appId}/menus/{menuId}/reorder | menus（按 ReorderMenuRequest 数组顺序写 sortOrder） | M08.F02.I06 | - | 已上线 |
| M08.F02.I07 | AdminAppMenusController#moveMenu / AdminAppMenusService#move | PATCH /api/v1/admin/apps/{appId}/menus/{menuId}/parent | menus（UPDATE parentId） | M08.F02.I07 | - | 已上线 |
| M09.F01.I01 | TenantRoleMenusController#listRoleMenus / TenantRoleMenusService#list | GET /api/v1/tenants/{tenantId}/roles/{roleId}/menus | role_menu_grants（roleId 查 menuIds）+ 权限码 | M09.F01.I01 | - | 已上线 |
| M00.F04.I03 | TenantRoleMenusController#setRoleMenus / TenantRoleMenusService#set | PUT /api/v1/tenants/{tenantId}/roles/{roleId}/menus | role_menu_grants（UPSERT 整批替换） | M00.F04.I03 | - | 已上线 |
| M09.F02.I03 | TenantRoleMenusController#clearRoleMenus / TenantRoleMenusService#clear | DELETE /api/v1/tenants/{tenantId}/roles/{roleId}/menus | role_menu_grants（DELETE，整批移除） | M09.F02.I03 | - | 已上线 |

> 签发统一走 JwtIssuer（HS256，JWT_SIGNING_KEY env，≥32B）。
> 本仓其余已上线条目的设计映射待补（v0.2.x 前的 M00/M01 走通用 auth 链路），
> 本批次只登记 v0.2.0 Phase 6 真 OAuth 三接口 —— 对应 L5 软告警清零。
