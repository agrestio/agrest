package io.agrest.spring.starter;

import io.agrest.runtime.AgRuntimeBuilder;

public interface AgRuntimeCustomizer {

  void customize(AgRuntimeBuilder builder);
}
