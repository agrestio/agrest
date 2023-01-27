package io.agrest.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.AgException;

import java.io.IOException;
import java.util.Map;

/**
 * @since 5.0
 */
public class InheritanceAwareEntityEncoder extends AbstractEncoder {

    private final Map<Class<?>, Encoder> hierarchyEncoders;

    public InheritanceAwareEntityEncoder(Map<Class<?>, Encoder> hierarchyEncoders) {
        this.hierarchyEncoders = hierarchyEncoders;
    }

    @Override
    protected void encodeNonNullObject(Object object, boolean skipNullProperties, JsonGenerator out) throws IOException {
        Class<?> type = object.getClass();
        findEncoder(type, type).encode(null, object, skipNullProperties, out);
    }

    protected Encoder findEncoder(Class<?> startType, Class<?> type) {
        Encoder encoder = hierarchyEncoders.get(type);
        if (encoder != null) {
            return encoder;
        }

        // Recursive lookup of a superclass encoder is a  failover strategy that should not normally be required. If it
        // turns out we hit this a lot, consider caching superclass encoder per subclass, so that this lookup only
        // happens once.

        Class<?> superType = type.getSuperclass();
        if (Object.class.equals(superType)) {
            throw AgException.internalServerError("No encoder for class %s or its superclasses", startType.getName());
        }

        return findEncoder(startType, superType);
    }
}
