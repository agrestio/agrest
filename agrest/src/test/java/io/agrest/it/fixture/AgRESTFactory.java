package io.agrest.it.fixture;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import io.agrest.runtime.AgRESTBuilder;
import io.agrest.runtime.IAgRESTService;
import io.agrest.runtime.AgRESTRuntime;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.junit.rules.ExternalResource;

public class AgRESTFactory extends ExternalResource {

	private AgRESTRuntime runtime;
	private ServerRuntime cayenneRuntime;

	public AgRESTFactory(CayenneDerbyStack cayenneStack) {
		this.cayenneRuntime = cayenneStack.getCayenneStack();
	}

	@Override
	protected void before() throws Throwable {
		runtime = doBefore(AgRESTBuilder.builder(cayenneRuntime)).build();
	}

	@Override
	protected void after() {
		runtime.shutdown();
		runtime = null;
	}

	/**
	 * Customized AgREST builder. A method for subclasses to override.
	 */
	protected AgRESTBuilder doBefore(AgRESTBuilder builder) {
		return builder;
	}

	public AgRESTRuntime getRuntime() {
		return Objects.requireNonNull(runtime);
	}
	
	public IAgRESTService getAgRESTService() {
		return getRuntime().service(IAgRESTService.class);
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
