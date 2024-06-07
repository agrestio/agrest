package io.agrest.runtime.processor.update;

import io.agrest.ObjectMapper;
import io.agrest.ObjectMapperFactory;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;

/**
 * An {@link ObjectMapperFactory} that locates objects by the combination of FK
 * to parent and some other column. I.e. those objects in 1..N relationships
 * that have a unique property within parent.
 *
 * @since 1.4
 * @deprecated in favor of {@link ByPropertyObjectMapperFactory}
 */
@Deprecated(since = "5.0", forRemoval = true)
public class ByKeyObjectMapperFactory implements ObjectMapperFactory {

    private final String property;

    /**
     * @deprecated in favor of {@link ObjectMapperFactory#matchByProperties(String...)}
     */
    public static ByKeyObjectMapperFactory byKey(String key) {
        return new ByKeyObjectMapperFactory(key);
    }

    private ByKeyObjectMapperFactory(String property) {
        this.property = property;
    }

    @Override
    public <T> ObjectMapper<T> createMapper(UpdateContext<T> context) {
        AgEntity<T> entity = context.getEntity().getAgEntity();

        // TODO: should we account for "id" attributes here?
        AgAttribute attribute = entity.getAttribute(property);
        return new ByPropertyObjectMapper<>(attribute);
    }
}
