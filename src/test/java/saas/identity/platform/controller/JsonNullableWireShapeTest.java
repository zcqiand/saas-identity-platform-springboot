package saas.identity.platform.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import saas.identity.platform.config.JacksonConfig;
import saas.identity.shared.dto.EffectiveMenuNode;
import saas.identity.shared.dto.SysMenuType;

/**
 * 5.123 —— EffectiveMenuNode.parentId（JsonNullable&lt;UUID&gt;）wire 形状守卫。
 *
 * <p>契约（shared TSP EffectiveMenuNode）：parentId = string/uuid/nullable 且 required。 5.112① regen 把根
 * sentinel 换成 JsonNullable.undefined()（MeController 注释声明意图 「undefined 写 wire null」），但
 * JsonNullableModule 未注册进 Spring context —— Jackson 把 JsonNullable 当普通 bean 序列化，wire 出
 * {"present":false}（根）与 {"present":true} （子节点，value 丢失），违反自家契约形状，也是 contract-test I05 go 2-way
 * 分叉的根因 （rails 发 null 是契约合法形状；本修复让 oracle 回归 5.112① 声明的意图）。
 *
 * <p>用 Spring 自动装配的 ObjectMapper（TestApp 只排 DB 面，不开组件扫描）——断言的是 真实请求路径用的同一个 mapper，不是测试自建的。
 */
@SpringBootTest(classes = JsonNullableWireShapeTest.TestApp.class)
class JsonNullableWireShapeTest {

  /** 最小切片：全量 auto-config 但排 DB 面（RootRedirectIntegrationTest 同款，不开组件扫描）。 */
  @Configuration
  @EnableAutoConfiguration(
      exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class
      })
  @Import(JacksonConfig.class)
  static class TestApp {}

  @Autowired ObjectMapper objectMapper;

  @Test
  void rootUndefinedParentIdSerializesToWireNullNotPresentFalse() throws Exception {
    EffectiveMenuNode root =
        new EffectiveMenuNode(
            UUID.fromString("00000000-0000-0000-0000-910000000001"),
            "lab-management",
            UUID.fromString("00000000-0000-0000-0000-000000000000"),
            "仪表盘",
            SysMenuType.MENU,
            1,
            List.of());
    root.setParentId(JsonNullable.undefined());

    JsonNode node = objectMapper.readTree(objectMapper.writeValueAsString(root));

    // 模块实测行为：undefined → 整键省略（非字面 null）。5.112① 注释写「wire null」，
    // 但 CT 归一 M96.F01.I03 明文 null≡缺失，rails(显式 null)/aspnetcore(null) 归一全等，
    // 家族收敛达成。守卫点是不得出现 {"present":false} 对象形状。
    assertTrue(
        !node.has("parentId") || node.get("parentId").isNull(),
        "根 sentinel undefined 必须省略或 wire null，实际: "
            + (node.has("parentId") ? node.get("parentId") : "(缺键)"));
  }

  @Test
  void childPresentParentIdSerializesToBareUuidStringNotPresentTrue() throws Exception {
    UUID parentUuid = UUID.fromString("00000000-0000-0000-0000-910000000002");
    EffectiveMenuNode child =
        new EffectiveMenuNode(
            UUID.fromString("00000000-0000-0000-0000-910000000007"),
            "lab-management",
            parentUuid,
            "合同管理",
            SysMenuType.MENU,
            1,
            List.of());

    JsonNode node = objectMapper.readTree(objectMapper.writeValueAsString(child));

    assertTrue(
        node.get("parentId").isTextual() && node.get("parentId").asText().isEmpty() == false,
        "present 的 parentId 必须序列化为裸 uuid 字符串，实际: " + node.get("parentId"));
    assertEquals(
        parentUuid.toString(),
        node.get("parentId").asText(),
        "present 的 parentId 值不得丢失（此前 wire 只有 {\"present\":true} 连 value 都没上）");
  }
}
