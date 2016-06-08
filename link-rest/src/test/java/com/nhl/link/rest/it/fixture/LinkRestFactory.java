package com.nhl.link.rest.it.fixture;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.configuration.server.ServerRuntime;
import org.junit.rules.ExternalResource;

import com.nhl.link.rest.runtime.ILinkRestService;
import com.nhl.link.rest.runtime.LinkRestBuilder;
import com.nhl.link.rest.runtime.LinkRestRuntime;

public class LinkRestFactory extends ExternalResource {

	private LinkRestRuntime runtime;
	private ServerRuntime cayenneRuntime;

	public LinkRestFactory(CayenneDerbyStack cayenneStack) {
		this.cayenneRuntime = cayenneStack.getCayenneStack();
	}

	@Override
	protected void before() throws Throwable {
		runtime = doBefore(LinkRestBuilder.builder(cayenneRuntime)).build();
	}

	@Override
	protected void after() {
		runtime.shutdown();
		runtime = null;
	}

	/**
	 * Customized LR builder. A method for subclasses to override.
	 */
	protected LinkRestBuilder doBefore(LinkRestBuilder builder) {
		return builder;
	}

	public LinkRestRuntime getRuntime() {
		return Objects.requireNonNull(runtime);
	}
	
	public ILinkRestService getLinkRestService() {
		return getRuntime().service(ILinkRestService.class);
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
