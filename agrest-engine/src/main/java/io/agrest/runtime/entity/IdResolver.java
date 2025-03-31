package io.agrest.runtime.entity;

import io.agrest.id.AgObjectId;
import io.agrest.meta.AgEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @since 5.0
 */
public class IdResolver implements IIdResolver {

    @Override
    public List<AgObjectId> resolve(AgEntity<?> entity, Collection<?> idValues) {
        int len = idValues.size();
        if (len == 0) {
            return Collections.emptyList();
        }

        Function<Object, AgObjectId> idMaker = null;

        List<AgObjectId> resolved = new ArrayList<>(len);
        for (Object id : idValues) {

            if (idMaker == null) {
                idMaker = idMaker(entity.getIdParts().size(), id.getClass());
            }

            resolved.add(idMaker.apply(id));
        }

        return resolved;
    }

    static Function<Object, AgObjectId> idMaker(int size, Class<?> type) {
        if (size == 1) {
            return AgObjectId.class.isAssignableFrom(type)
                    ? o -> (AgObjectId) o
                    : o -> AgObjectId.of(o);
        } else if (Map.class.isAssignableFrom(type)) {
            return o -> AgObjectId.ofMap((Map<String, Object>) o);
        } else {
            throw new IllegalArgumentException("Argument for a multi-value ID must be of a Map type: " + type.getName());
        }
    }
}
