package io.agrest.runtime;

import io.agrest.AgRequestBuilder;
import io.agrest.UpdateStage;
import io.agrest.access.PathChecker;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.update.UpdateContext;
import io.agrest.runtime.processor.update.UpdateProcessorFactory;
import org.apache.cayenne.di.Injector;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultUpdateBuilderTest {

    private <T> DefaultUpdateBuilder<T> createBuilder(Class<T> type) {
        UpdateContext<T> context = new UpdateContext<>(type, mock(AgRequestBuilder.class), PathChecker.ofDefault(), mock(Injector.class));
        UpdateProcessorFactory processorFactory = mock(UpdateProcessorFactory.class);
        when(processorFactory.createProcessor(any())).thenReturn(mock(Processor.class));

        return new DefaultUpdateBuilder<T>(context, processorFactory);
    }

    @Test
    public void testStage_FunctionTypes() {

        // note that we do not make any assertions here.. just making sure methods with certain generic signatures
        // would compile without casting...

        createBuilder(X.class)
                .stage(UpdateStage.PARSE_REQUEST, this::doSomething0)
                .stage(UpdateStage.PARSE_REQUEST, this::doSomething1)
                .stage(UpdateStage.PARSE_REQUEST, this::doSomething2)
                .stage(UpdateStage.PARSE_REQUEST, this::doSomething3)
                .stage(UpdateStage.PARSE_REQUEST, this::doSomething4)
                .stage(UpdateStage.PARSE_REQUEST, (UpdateContext<X> s) -> {
                })
                .stage(UpdateStage.PARSE_REQUEST, s -> {
                });
    }

    @Test
    public void testTerminalStage_FunctionTypes() {

        // note that we do not make any assertions here.. just making sure methods with certain generic signatures
        // would compile without casting...

        createBuilder(X.class)
                .terminalStage(UpdateStage.PARSE_REQUEST, this::doSomething0)
                .terminalStage(UpdateStage.PARSE_REQUEST, this::doSomething1)
                .terminalStage(UpdateStage.PARSE_REQUEST, this::doSomething2)
                .terminalStage(UpdateStage.PARSE_REQUEST, this::doSomething3)
                .terminalStage(UpdateStage.PARSE_REQUEST, this::doSomething4)
                .terminalStage(UpdateStage.PARSE_REQUEST, (UpdateContext<X> s) -> {
                })
                .terminalStage(UpdateStage.PARSE_REQUEST, s -> {
                });
    }

    @Test
    public void testRoutingStage_FunctionTypes() {

        // note that we do not make any assertions here.. just making sure methods with certain generic signatures
        // would compile without casting...

        createBuilder(X.class)
                .routingStage(UpdateStage.PARSE_REQUEST, this::doSomethingAndReturn0)
                .routingStage(UpdateStage.PARSE_REQUEST, this::doSomethingAndReturn1)
                .routingStage(UpdateStage.PARSE_REQUEST, this::doSomethingAndReturn2)
                .routingStage(UpdateStage.PARSE_REQUEST, this::doSomethingAndReturn3)
                .routingStage(UpdateStage.PARSE_REQUEST, this::doSomethingAndReturn4)
                .routingStage(UpdateStage.PARSE_REQUEST, (UpdateContext<X> s) -> ProcessorOutcome.CONTINUE)
                .routingStage(UpdateStage.PARSE_REQUEST, s -> ProcessorOutcome.CONTINUE);
    }

    private void doSomething0(UpdateContext<?> c) {
    }

    private <T> void doSomething1(UpdateContext<T> c) {
    }

    private void doSomething2(UpdateContext<I1> c) {
        c.getEntity().setData(new ArrayList<>());
        List<I1> objects = c.getEntity().getData();
        objects.add(new T1());
    }

    private void doSomething3(UpdateContext<Object> c) {
        c.getEntity().setData(new ArrayList<>());
    }

    private void doSomething4(UpdateContext<X> c) {
        c.getEntity().setData(new ArrayList<>());
        c.getEntity().setData(new ArrayList<X>());
    }

    private ProcessorOutcome doSomethingAndReturn0(UpdateContext<?> c) {
        return ProcessorOutcome.CONTINUE;
    }

    private <T> ProcessorOutcome doSomethingAndReturn1(UpdateContext<T> c) {
        return ProcessorOutcome.CONTINUE;
    }

    private ProcessorOutcome doSomethingAndReturn2(UpdateContext<I1> c) {
        c.getEntity().setData(new ArrayList<>());
        return ProcessorOutcome.CONTINUE;
    }

    private ProcessorOutcome doSomethingAndReturn3(UpdateContext<Object> c) {
        c.getEntity().setData(new ArrayList<>());
        return ProcessorOutcome.CONTINUE;
    }

    private ProcessorOutcome doSomethingAndReturn4(UpdateContext<X> c) {
        c.getEntity().setData(new ArrayList<>());
        c.getEntity().setData(new ArrayList<X>());
        return ProcessorOutcome.CONTINUE;
    }

    interface I1 {
    }

    static class T1 implements I1 {
    }

    public class X {
    }
}
