package io.agrest.runtime.encoder;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import io.agrest.ResourceEntity;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.EntityEncoderFilter;
import io.agrest.encoder.FilterChainEncoder;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonGenerator;

public class FilterChainEncoderTest {

	@Test
	public void testEncodeNoFilters() throws IOException {

		Encoder delegate = mock(Encoder.class);

		FilterChainEncoder chain = new FilterChainEncoder(delegate, Collections.<EntityEncoderFilter> emptyList());
		chain.encode(null, new Object(), mock(JsonGenerator.class));
		verify(delegate).encode(any(String.class), any(), any(JsonGenerator.class));
	}

	@Test
	public void testEncode_PassThroughFilter() throws IOException {

		Encoder delegate = mock(Encoder.class);
		EntityEncoderFilter filter = new EntityEncoderFilter() {

			@Override
			public boolean matches(ResourceEntity<?> entity) {
				return true;
			}

			@Override
			public boolean encode(String propertyName, Object object, JsonGenerator out, Encoder delegate)
					throws IOException {
				return delegate.encode(propertyName, object, out);
			}
			
			@Override
			public boolean willEncode(String propertyName, Object object, Encoder delegate) {
				return delegate.willEncode(propertyName, object);
			}
		};

		FilterChainEncoder chain = new FilterChainEncoder(delegate, Collections.singletonList(filter));
		chain.encode(null, new Object(), mock(JsonGenerator.class));
		verify(delegate).encode(isNull(String.class), any(), any(JsonGenerator.class));
	}

	@Test
	public void testEncode_BlockingFilter() throws IOException {

		Encoder delegate = mock(Encoder.class);
		EntityEncoderFilter filter = mock(EntityEncoderFilter.class);

		FilterChainEncoder chain = new FilterChainEncoder(delegate, Collections.singletonList(filter));
		chain.encode(null, new Object(), mock(JsonGenerator.class));
		verify(delegate, times(0)).encode(isNull(String.class), any(), any(JsonGenerator.class));
		verify(filter, times(1)).encode(isNull(String.class), any(), any(JsonGenerator.class), any(Encoder.class));
	}

	@Test
	public void testEncode_MultiFilters() throws IOException {

		Encoder delegate = mock(Encoder.class);

		// pass-through
		EntityEncoderFilter filter1 = new EntityEncoderFilter() {
			
			@Override
			public boolean matches(ResourceEntity<?> entity) {
				return true;
			}

			@Override
			public boolean encode(String propertyName, Object object, JsonGenerator out, Encoder delegate)
					throws IOException {
				return delegate.encode(propertyName, object, out);
			}
			
			@Override
			public boolean willEncode(String propertyName, Object object, Encoder delegate) {
				return delegate.willEncode(propertyName, object);
			}
		};

		// blocking
		EntityEncoderFilter filter2 = mock(EntityEncoderFilter.class);

		// blocking, but that's irrelevant
		EntityEncoderFilter filter3 = mock(EntityEncoderFilter.class);

		FilterChainEncoder chain = new FilterChainEncoder(delegate, Arrays.asList(filter1, filter2, filter3));
		chain.encode(null, new Object(), mock(JsonGenerator.class));
		verify(delegate, times(0)).encode(isNull(String.class), any(), any(JsonGenerator.class));
		verify(filter2, times(1)).encode(isNull(String.class), any(), any(JsonGenerator.class), any(Encoder.class));
		verify(filter3, times(0)).encode(isNull(String.class), any(), any(JsonGenerator.class), any(Encoder.class));
	}
}
