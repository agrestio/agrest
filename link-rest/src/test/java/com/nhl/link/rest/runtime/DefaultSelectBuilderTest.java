package com.nhl.link.rest.runtime;

import com.nhl.link.rest.SelectStage;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.processor.ProcessorOutcome;
import com.nhl.link.rest.runtime.listener.IListenerService;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import com.nhl.link.rest.runtime.processor.select.SelectProcessorFactory;
import org.apache.cayenne.DataObject;
import org.junit.Test;

import java.util.ArrayList;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultSelectBuilderTest {

    private <T> DefaultSelectBuilder<T> createBuilder(Class<T> type) {
        SelectContext<T> context = new SelectContext<>(type);
        SelectProcessorFactory processorFactory = mock(SelectProcessorFactory.class);
        when(processorFactory.createProcessor(any())).thenReturn(mock(Processor.class));

        return new DefaultSelectBuilder<T>(context, processorFactory, mock(IListenerService.class));
    }

    @Test
    public void testStage_FunctionTypes() {

        // note that we do not make any assertions here.. just making sure methods with certain generic signatures
        // would compile without casting...

        createBuilder(E2.class)
                .stage(SelectStage.PARSE_REQUEST, this::doSomething0)
                .stage(SelectStage.PARSE_REQUEST, this::doSomething1)
                .stage(SelectStage.PARSE_REQUEST, this::doSomething2)
                .stage(SelectStage.PARSE_REQUEST, this::doSomething3)
                .stage(SelectStage.PARSE_REQUEST, this::doSomething4)
                .stage(SelectStage.PARSE_REQUEST, (SelectContext<E2> s) -> {
                })
                .stage(SelectStage.PARSE_REQUEST, s -> {
                })
                .get();
    }

    @Test
    public void testTerminalStage_FunctionTypes() {

        // note that we do not make any assertions here.. just making sure methods with certain generic signatures
        // would compile without casting...

        createBuilder(E2.class)
                .terminalStage(SelectStage.PARSE_REQUEST, this::doSomething0)
                .terminalStage(SelectStage.PARSE_REQUEST, this::doSomething1)
                .terminalStage(SelectStage.PARSE_REQUEST, this::doSomething2)
                .terminalStage(SelectStage.PARSE_REQUEST, this::doSomething3)
                .terminalStage(SelectStage.PARSE_REQUEST, this::doSomething4)
                .terminalStage(SelectStage.PARSE_REQUEST, (SelectContext<E2> s) -> {
                })
                .terminalStage(SelectStage.PARSE_REQUEST, s -> {
                })
                .get();
    }

    @Test
    public void testRoutingStage_FunctionTypes() {

        // note that we do not make any assertions here.. just making sure methods with certain generic signatures
        // would compile without casting...

        createBuilder(E2.class)
                .routingStage(SelectStage.PARSE_REQUEST, this::doSomethingAndReturn0)
                .routingStage(SelectStage.PARSE_REQUEST, this::doSomethingAndReturn1)
                .routingStage(SelectStage.PARSE_REQUEST, this::doSomethingAndReturn2)
                .routingStage(SelectStage.PARSE_REQUEST, this::doSomethingAndReturn3)
                .routingStage(SelectStage.PARSE_REQUEST, this::doSomethingAndReturn4)
                .routingStage(SelectStage.PARSE_REQUEST, (SelectContext<E2> s) -> ProcessorOutcome.CONTINUE)
                .routingStage(SelectStage.PARSE_REQUEST, s -> ProcessorOutcome.CONTINUE)
                .get();
    }

    private void doSomething0(SelectContext<?> c) {
        c.setObjects(new ArrayList<>());
    }

    private <T> void doSomething1(SelectContext<T> c) {
    }

    private void doSomething2(SelectContext<DataObject> c) {
        c.setObjects(new ArrayList<>());
        c.setObjects(new ArrayList<E2>());
    }

    private void doSomething3(SelectContext<Object> c) {
        c.setObjects(new ArrayList<>());
        c.setObjects(new ArrayList<E2>());
    }

    private void doSomething4(SelectContext<E2> c) {
        c.setObjects(new ArrayList<>());
        c.setObjects(new ArrayList<E2>());
    }

    private ProcessorOutcome doSomethingAndReturn0(SelectContext<?> c) {
        c.setObjects(new ArrayList<>());
        return ProcessorOutcome.CONTINUE;
    }

    private <T> ProcessorOutcome doSomethingAndReturn1(SelectContext<T> c) {
        return ProcessorOutcome.CONTINUE;
    }

    private ProcessorOutcome doSomethingAndReturn2(SelectContext<DataObject> c) {
        c.setObjects(new ArrayList<>());
        c.setObjects(new ArrayList<E2>());
        return ProcessorOutcome.CONTINUE;
    }

    private ProcessorOutcome doSomethingAndReturn3(SelectContext<Object> c) {
        c.setObjects(new ArrayList<>());
        c.setObjects(new ArrayList<E2>());
        return ProcessorOutcome.CONTINUE;
    }

    private ProcessorOutcome doSomethingAndReturn4(SelectContext<E2> c) {
        c.setObjects(new ArrayList<>());
        c.setObjects(new ArrayList<E2>());
        return ProcessorOutcome.CONTINUE;
    }
}
