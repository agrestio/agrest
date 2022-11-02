package io.agrest.spring.it;

import io.agrest.AgException;
import io.agrest.runtime.AgRuntime;
import io.agrest.runtime.AgRuntimeBuilder;
import io.agrest.runtime.processor.delete.DeleteProcessorFactory;
import io.agrest.runtime.processor.select.SelectProcessorFactory;
import io.agrest.runtime.processor.unrelate.UnrelateProcessorFactory;
import io.agrest.runtime.processor.update.CreateOrUpdateProcessorFactory;
import io.agrest.runtime.processor.update.CreateProcessorFactory;
import io.agrest.runtime.processor.update.IdempotentCreateOrUpdateProcessorFactory;
import io.agrest.runtime.processor.update.IdempotentFullSyncProcessorFactory;
import io.agrest.runtime.processor.update.UpdateProcessorFactory;
import io.agrest.spi.AgExceptionMapper;
import io.agrest.spring.it.controllers.ExceptionMappersController.TestAgExceptionMapper;
import io.agrest.spring.it.controllers.ExceptionMappersController.TestException;
import io.agrest.spring.it.controllers.ExceptionMappersController.TestExceptionMapper;
import io.agrest.spring.it.pojo.runtime.PojoFetchStage;
import io.agrest.spring.it.pojo.runtime.PojoSelectProcessorFactoryProvider;
import io.agrest.spring.it.pojo.runtime.PojoStore;

import org.apache.cayenne.di.Module;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.mock;

@SpringBootApplication
public class ItApp {

  @Bean
  public AgRuntime agRuntime() {
    return AgRuntime.builder()
        .module(this::configureAg)
        .module(exceptionsModule())
        .build();
  }

  private static Module exceptionsModule() {
    return cb -> cb
        .bindMap(AgExceptionMapper.class)
        .put(AgException.class.getName(), TestAgExceptionMapper.class)
        .put(TestException.class.getName(), TestExceptionMapper.class);
  }

  @Bean
  PojoStore pojoStore() {
    return new PojoStore();
  }

  private void configureAg(org.apache.cayenne.di.Binder agBinder) {
    agBinder.bind(SelectProcessorFactory.class).toProvider(PojoSelectProcessorFactoryProvider.class);
    agBinder.bind(DeleteProcessorFactory.class).toInstance(mock(DeleteProcessorFactory.class));
    agBinder.bind(CreateProcessorFactory.class).toInstance(mock(CreateProcessorFactory.class));
    agBinder.bind(UpdateProcessorFactory.class).toInstance(mock(UpdateProcessorFactory.class));
    agBinder.bind(CreateOrUpdateProcessorFactory.class).toInstance(mock(CreateOrUpdateProcessorFactory.class));
    agBinder.bind(IdempotentCreateOrUpdateProcessorFactory.class).toInstance(mock(IdempotentCreateOrUpdateProcessorFactory.class));
    agBinder.bind(IdempotentFullSyncProcessorFactory.class).toInstance(mock(IdempotentFullSyncProcessorFactory.class));
    agBinder.bind(UnrelateProcessorFactory.class).toInstance(mock(UnrelateProcessorFactory.class));
    agBinder.bind(PojoFetchStage.class).to(PojoFetchStage.class);
    agBinder.bind(PojoStore.class).toInstance(pojoStore());
  }

}
