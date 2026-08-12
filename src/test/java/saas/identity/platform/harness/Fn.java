package saas.identity.platform.harness;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Fn — claim a function ID for this test. Read by HarnessTraceListener to write .state/trace.json
 * with test → fn mappings.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Fn {
  String[] value();
}
