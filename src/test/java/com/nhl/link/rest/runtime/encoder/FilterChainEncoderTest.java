package com.nhl.link.rest.runtime.encoder;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nhl.link.rest.ClientEntity;
import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.encoder.EncoderFilter;
import com.nhl.link.rest.encoder.FilterChainEncoder;

public class FilterChainEncoderTest {

	@Test
	public void testEncodeNoFilters() throws IOException {

		Encoder delegate = mock(Encoder.class);

		FilterChainEncoder chain = new FilterChainEncoder(delegate, Collections.<EncoderFilter> emptyList());
		chain.encode(null, new Object(), mock(JsonGenerator.class));
		verify(delegate).encode(any(String.class), any(), any(JsonGenerator.class));
	}

	@Test
	public void testEncode_PassThroughFilter() throws IOException {

		Encoder delegate = mock(Encoder.class);
		EncoderFilter filter = new EncoderFilter() {

			@Override
			public boolean matches(ClientEntity<?> clientEntity) {
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
		EncoderFilter filter = mock(EncoderFilter.class);

		FilterChainEncoder chain = new FilterChainEncoder(delegate, Collections.singletonList(filter));
		chain.encode(null, new Object(), mock(JsonGenerator.class));
		verify(delegate, times(0)).encode(isNull(String.class), any(), any(JsonGenerator.class));
		verify(filter, times(1)).encode(isNull(String.class), any(), any(JsonGenerator.class), any(Encoder.class));
	}

	@Test
	public void testEncode_MultiFilters() throws IOException {

		Encoder delegate = mock(Encoder.class);

		// pass-through
		EncoderFilter filter1 = new EncoderFilter() {
			
			@Override
			public boolean matches(ClientEntity<?> clientEntity) {
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
		EncoderFilter filter2 = mock(EncoderFilter.class);

		// blocking, but that's irrelevant
		EncoderFilter filter3 = mock(EncoderFilter.class);

		FilterChainEncoder chain = new FilterChainEncoder(delegate, Arrays.asList(filter1, filter2, filter3));
		chain.encode(null, new Object(), mock(JsonGenerator.class));
		verify(delegate, times(0)).encode(isNull(String.class), any(), any(JsonGenerator.class));
		verify(filter2, times(1)).encode(isNull(String.class), any(), any(JsonGenerator.class), any(Encoder.class));
		verify(filter3, times(0)).encode(isNull(String.class), any(), any(JsonGenerator.class), any(Encoder.class));
	}
}
