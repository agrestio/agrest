package com.nhl.link.rest.runtime;

import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Collection;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.apache.cayenne.di.Binder;
import org.apache.cayenne.validation.ValidationException;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.nhl.link.rest.provider.ValidationExceptionMapper;
import com.nhl.link.rest.runtime.adapter.LinkRestAdapter;
import com.nhl.link.rest.runtime.parser.IRequestParser;

public class LinkRestBuilderTest {

	@Test
	public void testMapException_Standard() {
		LinkRestBuilder builder = new LinkRestBuilder();
		Feature f = builder.build().getFeature();

		FeatureContext context = mock(FeatureContext.class);

		f.configure(context);

		verify(context).register(ValidationExceptionMapper.class);
	}

	@Test
	public void testMapException_Custom() {
		LinkRestBuilder builder = new LinkRestBuilder().mapException(TestValidationExceptionMapper.class);
		Feature f = builder.build().getFeature();

		FeatureContext context = mock(FeatureContext.class);

		f.configure(context);

		verify(context).register(TestValidationExceptionMapper.class);
		verify(context, never()).register(ValidationExceptionMapper.class);
	}

	@Test
	public void testBuild_Adapter() {

		final Feature adapterFeature = mock(Feature.class);

		LinkRestAdapter adapter = mock(LinkRestAdapter.class);
		doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				@SuppressWarnings("unchecked")
				Collection<Feature> c = (Collection<Feature>) invocation.getArguments()[0];
				c.add(adapterFeature);
				return null;
			}
		}).when(adapter).contributeToJaxRs(anyCollectionOf(Feature.class));

		final IRequestParser mockParser = mock(IRequestParser.class);
		doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				Binder b = (Binder) invocation.getArguments()[0];
				b.bind(IRequestParser.class).toInstance(mockParser);
				return null;
			}
		}).when(adapter).contributeToRuntime(any(Binder.class));

		LinkRestRuntime runtime = new LinkRestBuilder().adapter(adapter).build();

		assertSame(mockParser, runtime.service(IRequestParser.class));

		FeatureContext context = mock(FeatureContext.class);
		runtime.getFeature().configure(context);
		verify(adapterFeature).configure(context);
	}

	static class TestValidationExceptionMapper implements ExceptionMapper<ValidationException> {

		@Override
		public Response toResponse(ValidationException exception) {
			return null;
		}
	}
}
