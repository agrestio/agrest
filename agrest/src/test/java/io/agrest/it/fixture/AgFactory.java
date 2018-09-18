package io.agrest.it.fixture;

import io.agrest.runtime.AgBuilder;
import io.agrest.runtime.AgRuntime;
import io.agrest.runtime.IAgService;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.junit.rules.ExternalResource;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AgFactory extends ExternalResource {

    private AgRuntime runtime;
    private ServerRuntime cayenneRuntime;

    public AgFactory(CayenneDerbyStack cayenneStack) {
        this.cayenneRuntime = cayenneStack.getCayenneStack();
    }

    @Override
    protected void before() {
        runtime = doBefore(AgBuilder.builder(cayenneRuntime)).build();
    }

    @Override
    protected void after() {
        runtime.shutdown();
        runtime = null;
    }

    /**
     * Customized AgBuilder builder. A method for subclasses to override.
     */
    protected AgBuilder doBefore(AgBuilder builder) {
        return builder;
    }

    public AgRuntime getRuntime() {
        return Objects.requireNonNull(runtime);
    }

    public IAgService getService() {
        return getRuntime().service(IAgService.class);
    }

    public ExecutorService getExecutor() {
        return getRuntime().service(ExecutorService.class);
    }

    public UriInfo mockUri(MultivaluedMap<String, String> params) {
        UriInfo mockUri = mock(UriInfo.class);
        when(mockUri.getQueryParameters()).thenReturn(params);
        return mockUri;
    }
}
