package datadog.trace.agent.tooling;

import datadog.trace.api.Config;
import datadog.trace.api.GlobalTracer;
import datadog.trace.bootstrap.instrumentation.api.AgentTracer;
import datadog.trace.core.CoreTracer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TracerInstaller {
  /** Register a global tracer if no global tracer is already registered. */
  public static synchronized void installGlobalTracer() {
    if (Config.get().isTraceEnabled()) {
      if (!(GlobalTracer.get() instanceof CoreTracer)) {
        installGlobalTracer(CoreTracer.builder().build());
      } else {
        log.debug("GlobalTracer already registered.");
      }
    } else {
      log.debug("Tracing is disabled, not installing GlobalTracer.");
    }
  }

  public static void installGlobalTracer(final CoreTracer tracer) {
    try {
      GlobalTracer.registerIfAbsent(tracer);
      AgentTracer.registerIfAbsent(tracer);

      log.debug("Global tracer installed");
    } catch (final RuntimeException re) {
      log.warn("Failed to register tracer: {}", tracer, re);
    }
  }

  public static void forceInstallGlobalTracer(CoreTracer tracer) {
    try {
      log.warn("Overriding installed global tracer.  This is not intended for production use");

      GlobalTracer.forceRegister(tracer);
      AgentTracer.forceRegister(tracer);

      log.debug("Global tracer installed");
    } catch (final RuntimeException re) {
      log.warn("Failed to register tracer: {}", tracer, re);
    }
  }
}
