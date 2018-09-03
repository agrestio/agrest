package io.agrest.runtime.cayenne;

import java.util.Collection;

import io.agrest.ObjectMapper;
import io.agrest.ObjectMapperFactory;
import io.agrest.meta.AgAttribute;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.exp.parser.ASTPath;

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
	public <T> ObjectMapper<T> createMapper(UpdateContext<T> context) {

		Collection<AgAttribute> ids = context.getEntity().getAgEntity().getIds();
		ASTPath[] paths = new ASTPath[ids.size()];

		int i = 0;
		for (AgAttribute id : ids) {
			paths[i++] = id.getPathExp();
		}

		return new ByIdObjectMapper<>(paths);
	}
}
