package io.agrest.runtime;

import io.agrest.SelectStage;
import io.agrest.pojo.model.P1;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.select.SelectContext;
import io.agrest.runtime.processor.select.SelectProcessorFactory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultSelectBuilderTest {

    private <T> DefaultSelectBuilder<T> createBuilder(Class<T> type) {
        SelectContext<T> context = new SelectContext<>(type);
        SelectProcessorFactory processorFactory = mock(SelectProcessorFactory.class);
        when(processorFactory.createProcessor(any())).thenReturn(mock(Processor.class));

        return new DefaultSelectBuilder<T>(context, processorFactory);
    }

    @Test
    public void testStage_FunctionTypes() {

        // note that we do not make any assertions here.. just making sure methods with certain generic signatures
        // would compile without casting...

        createBuilder(P1.class)
                .stage(SelectStage.PARSE_REQUEST, this::doSomething0)
                .stage(SelectStage.PARSE_REQUEST, this::doSomething1)
                .stage(SelectStage.PARSE_REQUEST, this::doSomething2)
                .stage(SelectStage.PARSE_REQUEST, this::doSomething3)
                .stage(SelectStage.PARSE_REQUEST, this::doSomething4)
                .stage(SelectStage.PARSE_REQUEST, (SelectContext<P1> s) -> {
                })
                .stage(SelectStage.PARSE_REQUEST, s -> {
                });
    }

    @Test
    public void testTerminalStage_FunctionTypes() {

        // note that we do not make any assertions here.. just making sure methods with certain generic signatures
        // would compile without casting...

        createBuilder(P1.class)
                .terminalStage(SelectStage.PARSE_REQUEST, this::doSomething0)
                .terminalStage(SelectStage.PARSE_REQUEST, this::doSomething1)
                .terminalStage(SelectStage.PARSE_REQUEST, this::doSomething2)
                .terminalStage(SelectStage.PARSE_REQUEST, this::doSomething3)
                .terminalStage(SelectStage.PARSE_REQUEST, this::doSomething4)
                .terminalStage(SelectStage.PARSE_REQUEST, (SelectContext<P1> s) -> {
                })
                .terminalStage(SelectStage.PARSE_REQUEST, s -> {
                });
    }

    @Test
    public void testRoutingStage_FunctionTypes() {

        // note that we do not make any assertions here.. just making sure methods with certain generic signatures
        // would compile without casting...

        createBuilder(P1.class)
                .routingStage(SelectStage.PARSE_REQUEST, this::doSomethingAndReturn0)
                .routingStage(SelectStage.PARSE_REQUEST, this::doSomethingAndReturn1)
                .routingStage(SelectStage.PARSE_REQUEST, this::doSomethingAndReturn2)
                .routingStage(SelectStage.PARSE_REQUEST, this::doSomethingAndReturn3)
                .routingStage(SelectStage.PARSE_REQUEST, this::doSomethingAndReturn4)
                .routingStage(SelectStage.PARSE_REQUEST, (SelectContext<P1> s) -> ProcessorOutcome.CONTINUE)
                .routingStage(SelectStage.PARSE_REQUEST, s -> ProcessorOutcome.CONTINUE);
    }

    private void doSomething0(SelectContext<?> c) {
    }

    private <T> void doSomething1(SelectContext<T> c) {
    }

    private void doSomething2(SelectContext<I1> c) {
        List<I1> data = new ArrayList<>();
        data.add(new T1());
        c.getEntity().setData(data);
    }

    private void doSomething3(SelectContext<Object> c) {
        c.getEntity().setData(new ArrayList<>());
    }

    private void doSomething4(SelectContext<P1> c) {
        c.getEntity().setData(new ArrayList<>());
    }

    private ProcessorOutcome doSomethingAndReturn0(SelectContext<?> c) {
        return ProcessorOutcome.CONTINUE;
    }

    private <T> ProcessorOutcome doSomethingAndReturn1(SelectContext<T> c) {
        return ProcessorOutcome.CONTINUE;
    }

    private ProcessorOutcome doSomethingAndReturn2(SelectContext<I1> c) {
        c.getEntity().setData(new ArrayList<>());
        return ProcessorOutcome.CONTINUE;
    }

    private ProcessorOutcome doSomethingAndReturn3(SelectContext<Object> c) {
        c.getEntity().setData(new ArrayList<>());
        return ProcessorOutcome.CONTINUE;
    }

    private ProcessorOutcome doSomethingAndReturn4(SelectContext<P1> c) {
        c.getEntity().setData(new ArrayList<>());
        c.getEntity().setData(new ArrayList<P1>());
        return ProcessorOutcome.CONTINUE;
    }

    interface I1 {
    }

    static class T1 implements I1 {
    }
}
