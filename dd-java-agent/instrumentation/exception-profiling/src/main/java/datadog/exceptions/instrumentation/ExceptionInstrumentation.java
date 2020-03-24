package datadog.exceptions.instrumentation;

import static net.bytebuddy.matcher.ElementMatchers.isConstructor;

import com.google.auto.service.AutoService;
import datadog.trace.agent.tooling.Instrumenter;
import java.util.Collections;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

@AutoService(Instrumenter.class)
/**
 * Provides instrumentation of {@linkplain Exception} constructor. <br>
 * {@linkplain Exception}, as opposed to {@linkplain Throwable} was deliberately chosen such that we
 * don't instrument {@linkplain Error} class/subclasses since they are tracked by a native JFR event
 * already.
 */
public class ExceptionInstrumentation extends Instrumenter.Default {
  private final boolean hasJfr;

  public ExceptionInstrumentation() {
    super("exceptions");
    boolean jfr = false;
    try {
      /* Check only for the open-sources JFR implementation.
       * If it is ever needed to support also the closed sourced JDK 8 version the check should be
       * enhanced
       */
      Class.forName("jdk.jfr.Event");
      jfr = true;
    } catch (final ClassNotFoundException ignored) {
    }
    hasJfr = jfr;
  }

  @Override
  public String[] helperClassNames() {
    /*
     * Since the only instrumentation target is java.lang.Exception which is loaded by bootstrap classloader
     * it is ok to use helper classes instead of hacking around a Java 8 specific bootstrap.
     */
    return hasJfr
        ? new String[] {
          "com.datadog.profiling.exceptions.StreamingSampler",
          "com.datadog.profiling.exceptions.ExceptionCountEvent",
          "com.datadog.profiling.exceptions.ExceptionHistogram",
          "com.datadog.profiling.exceptions.ExceptionHistogram$1",
          "com.datadog.profiling.exceptions.ExceptionProfiling",
          "com.datadog.profiling.exceptions.ExceptionSampleEvent",
          "com.datadog.profiling.exceptions.ExceptionSampler"
        }
        : new String[0];
  }

  @Override
  public ElementMatcher<? super TypeDescription> typeMatcher() {
    if (hasJfr) {
      // match only java.lang.Exception since java.lang.Error is tracked by another JFR event
      return ElementMatchers.is(Exception.class);
    }
    return ElementMatchers.none();
  }

  @Override
  public Map<? extends ElementMatcher<? super MethodDescription>, String> transformers() {
    if (hasJfr) {
      return Collections.singletonMap(
          isConstructor(), "datadog.exceptions.instrumentation.ExceptionAdvice");
    }
    return Collections.emptyMap();
  }
}
