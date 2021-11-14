package io.agrest;

import io.agrest.meta.AgEntity;
import io.agrest.meta.AgIdPart;

import java.util.Collection;
import java.util.Map;

/**
 * @since 1.24
 */
public abstract class BaseObjectId implements AgObjectId {

    @Override
    public Map<String, Object> asMap(AgEntity<?> entity) {

        if (entity == null) {
            throw AgException.internalServerError("Can't build ID: entity is null");
        }

        Collection<AgIdPart> idAttributes = entity.getIdParts();
        if (idAttributes.size() != size()) {
            throw AgException.badRequest("Wrong ID size: expected %s, got: %s", idAttributes.size(), size());
        }

        return asMap(idAttributes);
    }

    protected abstract Map<String, Object> asMap(Collection<AgIdPart> idAttributes);
}
