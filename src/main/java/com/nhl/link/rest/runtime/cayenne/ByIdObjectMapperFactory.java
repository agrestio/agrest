package com.nhl.link.rest.runtime.cayenne;

import java.util.Collection;
import java.util.Iterator;

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
		Collection<String> pks = entity.getPrimaryKeyNames();

		int len = pks.size();
		ASTPath[] keyPaths = new ASTDbPath[len];
		Iterator<String> it = pks.iterator();
		for (int i = 0; i < len; i++) {
			keyPaths[i] = new ASTDbPath(it.next());
		}

		return new ByIdObjectMapper<>(keyPaths);
	}
}
