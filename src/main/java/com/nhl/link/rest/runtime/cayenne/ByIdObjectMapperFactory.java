package com.nhl.link.rest.runtime.cayenne;

import java.util.Collection;

import org.apache.cayenne.exp.parser.ASTPath;

import com.nhl.link.rest.ObjectMapper;
import com.nhl.link.rest.ObjectMapperFactory;
import com.nhl.link.rest.UpdateResponse;
import com.nhl.link.rest.meta.LrAttribute;

/**
 * A default singleton implementation of the {@link ObjectMapperFactory} that
 * looks up objects based on their IDs.
 * 
 * @since 1.4
 */
public class ByIdObjectMapperFactory implements ObjectMapperFactory {

	private static final ObjectMapperFactory instance = new ByIdObjectMapperFactory();

	public static ObjectMapperFactory mapper() {
		return instance;
	}

	@Override
	public <T> ObjectMapper<T> forResponse(UpdateResponse<T> response) {

		Collection<LrAttribute> ids = response.getEntity().getLrEntity().getIds();
		ASTPath[] paths = new ASTPath[ids.size()];

		int i = 0;
		for (LrAttribute id : ids) {
			paths[i++] = id.getPathExp();
		}

		return new ByIdObjectMapper<>(paths);
	}
}
