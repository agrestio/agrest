package com.nhl.link.rest.runtime.cayenne;

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
public class ByIdObjectMapperFactory extends CayenneObjectMapperFactory {

	private static final ObjectMapperFactory instance = new ByIdObjectMapperFactory();

	public static ObjectMapperFactory mapper() {
		return instance;
	}

	@Override
	protected <T> ObjectMapper<T> mapper(UpdateResponse<T> response) {

		LrAttribute id = response.getEntity().getLrEntity().getId();

		// TODO: multi-column ids
		return new ByIdObjectMapper<>(new ASTPath[] { id.getPathExp() });
	}
}
