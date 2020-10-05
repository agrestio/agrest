package io.agrest.runtime;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class AgBuilderTest {

    @Test
    public void testExecutorService_Default() throws InterruptedException, ExecutionException, TimeoutException {
        AgBuilder builder = new AgBuilder();
        AgRuntime r = builder.build();

        ExecutorService exec;
        try {
            exec = r.service(ExecutorService.class);

            assertEquals("a", exec.submit(() -> "a").get(10, TimeUnit.SECONDS));
        } finally {
            r.shutdown();
        }
    }

    @Test
    public void testExecutorService_DefaultShutdown() {

        AgBuilder builder = new AgBuilder();
        AgRuntime r = builder.build();

        ExecutorService exec;
        try {
            exec = r.service(ExecutorService.class);
            assertFalse(exec.isShutdown());

        } finally {
            r.shutdown();
        }

        assertTrue(exec.isShutdown());
    }

    @Test
    public void testExecutorService_Custom() {

        ExecutorService mockExec = mock(ExecutorService.class);
        AgBuilder builder = new AgBuilder().executor(mockExec);

        AgRuntime r = builder.build();
        try {
            ExecutorService exec = r.service(ExecutorService.class);

            assertSame(mockExec, exec);
        } finally {
            r.shutdown();
        }
    }
}
