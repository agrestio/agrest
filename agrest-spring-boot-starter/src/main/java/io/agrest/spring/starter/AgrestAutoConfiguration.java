package io.agrest.spring.starter;

import io.agrest.meta.AgSchema;
import io.agrest.runtime.AgRuntime;
import io.agrest.runtime.jackson.IJacksonService;
import io.agrest.runtime.protocol.IEntityUpdateParser;
import io.agrest.spring.starter.exceptions.RestResponseEntityExceptionHandler;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.ControllerAdvice;

@Configuration
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "agrest", name = "enabled")
public class AgrestAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public AgRuntime agRuntime(ObjectProvider<AgRuntimeCustomizer> customizerProvider) {
    var builder = AgRuntime.builder();
    customizerProvider.orderedStream().forEach((customizer) -> customizer.customize(builder));
    return builder.build();
  }

  @Bean
  @ConditionalOnMissingBean
  public IJacksonService jacksonService(AgRuntime runtime) {
    return runtime.service(IJacksonService.class);
  }

  @Bean
  @ConditionalOnMissingBean
  public DataResponseWriter dataResponseWriter(IJacksonService jacksonService) {
    return new DataResponseWriter(jacksonService);
  }

  @Bean
  @ConditionalOnMissingBean
  public EntityUpdateReaderProcessor entityUpdateReaderProcessor(AgRuntime runtime) {
    return new EntityUpdateReaderProcessor(runtime.service(IEntityUpdateParser.class), runtime.service(AgSchema.class));
  }

  @Bean
  @ConditionalOnMissingBean
  public EntityUpdateReader entityUpdateReader(EntityUpdateReaderProcessor processor) {
    return new EntityUpdateReader(processor);
  }

  @Bean
  @ConditionalOnMissingBean
  public EntityUpdateCollectionReader entityUpdateCollectionReader(EntityUpdateReaderProcessor processor) {
    return new EntityUpdateCollectionReader(processor);
  }

  @Bean
  @ConditionalOnMissingBean
  public SimpleResponseWriter simpleResponseWriter(IJacksonService jacksonService) {
    return new SimpleResponseWriter(jacksonService);
  }

  @Bean
  public RestResponseEntityExceptionHandler exceptionHandler() {
      return new RestResponseEntityExceptionHandler();
  }
}
