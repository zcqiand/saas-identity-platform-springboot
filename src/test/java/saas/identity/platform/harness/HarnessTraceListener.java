package saas.identity.platform.harness;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

public class HarnessTraceListener implements TestExecutionListener {

  private final List<Map<String, Object>> entries = new ArrayList<>();

  @Override
  public void executionSkipped(TestIdentifier id, String reason) {
    record(id, null, true);
  }

  @Override
  public void executionFinished(TestIdentifier id, TestExecutionResult result) {
    record(id, result, false);
  }

  private void record(TestIdentifier id, TestExecutionResult result, boolean skipped) {
    if (!id.isTest()) return;
    List<String> fns = new ArrayList<>();
    TestSource src = id.getSource().orElse(null);
    if (src instanceof MethodSource methodSrc) {
      try {
        var method = methodSrc.getJavaMethod();
        Fn ann = method.getAnnotation(Fn.class);
        if (ann != null) {
          for (String f : ann.value()) if (!fns.contains(f)) fns.add(f);
        }
      } catch (Exception ignored) {
      }
    }
    boolean inert =
        skipped || (result != null && result.getStatus() == TestExecutionResult.Status.ABORTED);
    if (inert) fns.clear();
    Map<String, Object> entry = new LinkedHashMap<>();
    entry.put("test", id.getDisplayName());
    entry.put("fns", fns);
    entry.put("inert", inert);
    entries.add(entry);
  }

  @Override
  public void testPlanExecutionFinished(TestPlan plan) {
    if (!"1".equals(System.getenv("TRACE_MAP")) && !System.getProperty("TRACE_MAP", "").equals("1"))
      return;
    try {
      File dir = Paths.get(".state").toFile();
      dir.mkdirs();
      Map<String, Object> out = new LinkedHashMap<>();
      out.put("schema", 1);
      out.put("tests", entries);
      new ObjectMapper()
          .writerWithDefaultPrettyPrinter()
          .writeValue(new File(dir, "trace.json"), out);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
