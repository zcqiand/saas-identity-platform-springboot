package saas.identity.platform.harness;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/** Smoke test to verify @Fn annotation is wired correctly. */
class FnAnnotationSmokeTest {
  @Test
  @Fn({"M00.F01.I01"})
  void annotationIsReadable() throws Exception {
    assertNotNull(
        FnAnnotationSmokeTest.class
            .getDeclaredMethod("annotationIsReadable")
            .getAnnotation(Fn.class));
  }
}
