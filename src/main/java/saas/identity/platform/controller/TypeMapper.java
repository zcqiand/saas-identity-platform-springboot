package saas.identity.platform.controller;

import saas.identity.shared.dto.SysMenuType;

/**
 * menu type 双形态映射工具（API 字符串 enum ↔ DB smallint）。
 *
 * <p>家族约定：sys_menu.type 列是 smallint（1=directory, 2=menu, 3=button），API 层是 SysMenuType 字符串
 * enum。ClientMenusController（CRUD）与 MeController（me/menus 装配）两侧都要用， 提成公共静态工具防两处漂移——此前
 * MeController.buildTree 把 DB 数字 {@code String.valueOf(1)} 直接塞给只认 "directory|menu|button" 的
 * fromValue → 400。
 */
public final class TypeMapper {

  private TypeMapper() {}

  /** DB smallint → API enum（1=directory, 2=menu, 3=button）。 */
  public static SysMenuType fromShort(Short s) {
    if (s == null) return null;
    int v = s.intValue();
    if (v == 1) return SysMenuType.DIRECTORY;
    if (v == 2) return SysMenuType.MENU;
    if (v == 3) return SysMenuType.BUTTON;
    return SysMenuType.MENU;
  }

  /** API enum → DB smallint。 */
  public static short toShort(SysMenuType t) {
    return switch (t) {
      case DIRECTORY -> (short) 1;
      case MENU -> (short) 2;
      case BUTTON -> (short) 3;
    };
  }
}
