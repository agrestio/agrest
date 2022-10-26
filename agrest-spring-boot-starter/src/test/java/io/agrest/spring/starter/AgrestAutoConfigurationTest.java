package io.agrest.spring.starter;

import io.agrest.runtime.AgRuntime;
import io.agrest.runtime.AgRuntimeBuilder;
import io.agrest.runtime.jackson.IJacksonService;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;


class AgrestAutoConfigurationTest {

  private final ConditionEvaluationReportLoggingListener initializer = new ConditionEvaluationReportLoggingListener(
      LogLevel.INFO);

  private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
      .withInitializer(initializer)
      .withPropertyValues("agrest.enabled", "true")
      .withConfiguration(AutoConfigurations.of(AgrestAutoConfiguration.class));

  @Test
  void contributesDefaultBeans() {
    this.contextRunner.run((context) -> {
      assertThat(context).hasSingleBean(AgRuntime.class);
      assertThat(context).hasSingleBean(IJacksonService.class);
      assertThat(context).hasSingleBean(DataResponseWriter.class);
      assertThat(context).hasSingleBean(EntityUpdateReaderProcessor.class);
      assertThat(context).hasSingleBean(EntityUpdateReader.class);
      assertThat(context).hasSingleBean(SimpleResponseWriter.class);
    });
  }

  @Test
  void backsOffInNonWebApplicationContexts() {
    new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(AgrestAutoConfiguration.class))
        .run((context) -> assertThat(context).doesNotHaveBean(AgRuntime.class));
  }

  @Test
  void backsOffInNotEnabled() {
    new WebApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AgrestAutoConfiguration.class))
        .run((context) -> assertThat(context).doesNotHaveBean(AgRuntime.class));
  }
}