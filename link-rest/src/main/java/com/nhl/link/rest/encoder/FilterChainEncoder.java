package com.nhl.link.rest.encoder;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;

/**
 * An encoder that passes encoding request through a chain of
 * {@link EncoderFilter} objects before it gets to the real Encoder.
 */
public class FilterChainEncoder implements Encoder {

	private Encoder delegate;
	private List<EncoderFilter> filters;

	public FilterChainEncoder(Encoder delegate, List<EncoderFilter> filters) {
		this.delegate = delegate;
		this.filters = filters;
	}

	@Override
	public boolean encode(String propertyName, Object object, JsonGenerator out) throws IOException {
		return new ChainEncoder().encode(propertyName, object, out);
	}

	@Override
	public boolean willEncode(String propertyName, Object object) {
		return new ChainEncoder().willEncode(propertyName, object);
	}

	private final class ChainEncoder implements Encoder {
		private int i;

		EncoderFilter nextFilter() {
			EncoderFilter filter = i >= filters.size() ? null : filters.get(i);

			i++;
			return filter;
		}

		@Override
		public boolean encode(String propertyName, Object object, JsonGenerator out) throws IOException {
			EncoderFilter filter = nextFilter();
			if (filter != null) {
				return filter.encode(propertyName, object, out, this);
			} else {
				return delegate.encode(propertyName, object, out);
			}
		}

		@Override
		public boolean willEncode(String propertyName, Object object) {
			EncoderFilter filter = nextFilter();
			if (filter != null) {
				return filter.willEncode(propertyName, object, this);
			} else {
				return delegate.willEncode(propertyName, object);
			}
		}
	}

}
