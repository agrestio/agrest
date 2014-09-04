package com.nhl.link.rest.runtime.cayenne;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.exp.parser.ASTDbPath;
import org.apache.cayenne.exp.parser.ASTPath;
import org.apache.cayenne.map.ObjEntity;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ObjectMapper;
import com.nhl.link.rest.ObjectMapperFactory;
import com.nhl.link.rest.UpdateResponse;

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

		ObjEntity entity = response.getEntity().getCayenneEntity();

		// sanity checking...
		if (entity == null) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Unknown entity class: " + response.getType());
		}

		// TODO: multi-column ids
		ASTPath keyPath = new ASTDbPath(entity.getPrimaryKeyNames().iterator().next());

		return new ByIdObjectMapper<>(keyPath);
	}
}
