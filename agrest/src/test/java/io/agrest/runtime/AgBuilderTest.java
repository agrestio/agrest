package io.agrest.runtime;

import io.agrest.runtime.adapter.AgAdapter;
import io.agrest.runtime.protocol.ICayenneExpParser;
import org.apache.cayenne.di.Binder;
import org.apache.cayenne.validation.ValidationException;
import org.junit.Test;
import org.mockito.stubbing.Answer;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class AgBuilderTest {

    @Deprecated
    @Test
    public void testBuild_Adapter() {

        final Feature adapterFeature = mock(Feature.class);

        AgAdapter adapter = mock(AgAdapter.class);
        doAnswer((Answer<Object>) invocation -> {
            @SuppressWarnings("unchecked")
            Collection<Feature> c = (Collection<Feature>) invocation.getArguments()[0];
            c.add(adapterFeature);
            return null;
        }).when(adapter).contributeToJaxRs(anyCollectionOf(Feature.class));

        final ICayenneExpParser mockParser = mock(ICayenneExpParser.class);
        doAnswer((Answer<Object>) invocation -> {
            Binder b = (Binder) invocation.getArguments()[0];
            b.bind(ICayenneExpParser.class).toInstance(mockParser);
            return null;
        }).when(adapter).contributeToRuntime(any(Binder.class));

        AgRuntime runtime = new AgBuilder().adapter(adapter).build();

        assertSame(mockParser, runtime.service(ICayenneExpParser.class));

        FeatureContext context = mock(FeatureContext.class);
        runtime.configure(context);
        verify(adapterFeature).configure(context);
    }

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
    public void testExecutorService_DefaultShutdown()
            throws InterruptedException, ExecutionException, TimeoutException {

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
    public void testExecutorService_Custom() throws InterruptedException, ExecutionException, TimeoutException {

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

    private void assertRuntime(AgBuilder builder, Consumer<AgRuntime> test) {
        AgRuntime r = builder.build();
        try {
            test.accept(r);
        } finally {
            r.shutdown();
        }
    }

    static class TestValidationExceptionMapper implements ExceptionMapper<ValidationException> {

        @Override
        public Response toResponse(ValidationException exception) {
            return null;
        }
    }

}
