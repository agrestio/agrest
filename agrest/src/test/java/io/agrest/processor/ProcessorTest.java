package io.agrest.processor;

import io.agrest.runtime.processor.select.SelectContext;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ProcessorTest {


    private Processor<SelectContext<?, ?>> shouldContinue1;
    private Processor<SelectContext<?, ?>> shouldContinue2;
    private Processor<SelectContext<?, ?>> shouldStop;
    private SelectContext<?, ?> mockContext;

    @Before
    public void createProcessors() {
        this.shouldContinue1 = mock(Processor.class);
        when(shouldContinue1.execute(any())).thenReturn(ProcessorOutcome.CONTINUE);

        this.shouldContinue2 = mock(Processor.class);
        when(shouldContinue2.execute(any())).thenReturn(ProcessorOutcome.CONTINUE);

        this.shouldStop = mock(Processor.class);
        when(shouldStop.execute(any())).thenReturn(ProcessorOutcome.STOP);

        mockContext = mock(SelectContext.class);
    }

    @Test
    public void testAndThen() {

        Processor<SelectContext<?, ?>> start = c -> ProcessorOutcome.CONTINUE;
        start.andThen(shouldContinue1).andThen(shouldContinue2).execute(mockContext);

        verify(shouldContinue1).execute(mockContext);
        verify(shouldContinue2).execute(mockContext);
    }

    @Test
    public void testAndThen_Stop() {

        Processor<SelectContext<?, ?>> start = c -> ProcessorOutcome.CONTINUE;
        start.andThen(shouldContinue1)
                .andThen(shouldStop)
                .andThen(shouldContinue2)
                .execute(mockContext);

        verify(shouldContinue1).execute(mockContext);
        verify(shouldStop).execute(mockContext);
        verifyZeroInteractions(shouldContinue2);
    }
}
