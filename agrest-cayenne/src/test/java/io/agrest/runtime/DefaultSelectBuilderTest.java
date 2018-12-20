package io.agrest.runtime;

import io.agrest.SelectStage;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.select.SelectContext;
import io.agrest.runtime.processor.select.SelectProcessorFactory;
import org.apache.cayenne.CayenneDataObject;
import org.apache.cayenne.DataObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultSelectBuilderTest {

    private <T, E> DefaultSelectBuilder<T, E> createBuilder(Class<T> type) {
        SelectContext<T, E> context = new SelectContext<>(type);
        SelectProcessorFactory processorFactory = mock(SelectProcessorFactory.class);
        when(processorFactory.createProcessor(any())).thenReturn(mock(Processor.class));

        return new DefaultSelectBuilder<T, E>(context, processorFactory);
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
                .stage(SelectStage.PARSE_REQUEST, (SelectContext<E2, Object> s) -> {
                })
                .stage(SelectStage.PARSE_REQUEST, s -> {
                });
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
                .terminalStage(SelectStage.PARSE_REQUEST, (SelectContext<E2, Object> s) -> {
                })
                .terminalStage(SelectStage.PARSE_REQUEST, s -> {
                });
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
                .routingStage(SelectStage.PARSE_REQUEST, (SelectContext<E2, Object> s) -> ProcessorOutcome.CONTINUE)
                .routingStage(SelectStage.PARSE_REQUEST, s -> ProcessorOutcome.CONTINUE);
    }

    private void doSomething0(SelectContext<?, ?> c) {
    }

    private <T, E> void doSomething1(SelectContext<T, E> c) {
    }

    private void doSomething2(SelectContext<DataObject, Object> c) {
        c.setObjects(new ArrayList<>());
        c.setObjects(new ArrayList<E2>());

        List<DataObject> objects = c.getObjects();
        objects.add(new CayenneDataObject());
    }

    private void doSomething3(SelectContext<Object, Object> c) {
        c.setObjects(new ArrayList<>());
        c.setObjects(new ArrayList<E2>());
    }

    private void doSomething4(SelectContext<E2, Object> c) {
        c.setObjects(new ArrayList<>());
        c.setObjects(new ArrayList<E2>());
    }

    private ProcessorOutcome doSomethingAndReturn0(SelectContext<?, ?> c) {
        return ProcessorOutcome.CONTINUE;
    }

    private <T, E> ProcessorOutcome doSomethingAndReturn1(SelectContext<T, E> c) {
        return ProcessorOutcome.CONTINUE;
    }

    private ProcessorOutcome doSomethingAndReturn2(SelectContext<DataObject, Object> c) {
        c.setObjects(new ArrayList<>());
        c.setObjects(new ArrayList<E2>());
        return ProcessorOutcome.CONTINUE;
    }

    private ProcessorOutcome doSomethingAndReturn3(SelectContext<Object, Object> c) {
        c.setObjects(new ArrayList<>());
        c.setObjects(new ArrayList<E2>());
        return ProcessorOutcome.CONTINUE;
    }

    private ProcessorOutcome doSomethingAndReturn4(SelectContext<E2, Object> c) {
        c.setObjects(new ArrayList<>());
        c.setObjects(new ArrayList<E2>());
        return ProcessorOutcome.CONTINUE;
    }
}
