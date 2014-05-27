package com.nhl.link.rest.runtime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.apache.cayenne.validation.ValidationException;
import org.junit.Test;

import com.nhl.link.rest.provider.ValidationExceptionMapper;

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

	static class TestValidationExceptionMapper implements ExceptionMapper<ValidationException> {

		@Override
		public Response toResponse(ValidationException exception) {
			return null;
		}
	}
}
