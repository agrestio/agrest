package io.agrest.runtime.processor.update;

import io.agrest.AgException;
import io.agrest.EntityUpdate;
import io.agrest.ObjectMapper;
import io.agrest.base.protocol.Exp;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgIdPart;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.parser.ASTEqual;
import org.apache.cayenne.exp.parser.ASTPath;

import java.util.*;

import static org.apache.cayenne.exp.ExpressionFactory.joinExp;

class ByIdObjectMapper<T> implements ObjectMapper<T> {

    private final AgEntity<T> entity;

    ByIdObjectMapper(AgEntity<T> entity) {
        this.entity = Objects.requireNonNull(entity);
    }

    @Override
    public Exp expressionForKey(Object key) {

        // can't match by NULL id
        if (key == null) {
            return null;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> idMap = (Map<String, Object>) key;

        // can't match by NULL id
        if (idMap.isEmpty()) {
            return null;
        }

        Exp exp = null;
        for (AgIdPart id : entity.getIdParts()) {
            match(id, idMap).and(exp);
        }

        return exp;
    }

    private Exp match(AgIdPart id, Map<String, Object> idMap) {

        Object value = idMap.get(id.getName());
        if (value == null) {
            throw AgException.badRequest("No ID value for path: %s", id.getName());
        }

        return Exp.withPositionalParams(id.getName() + " = $k", key);
    }

    @Override
    public Object keyForObject(T object) {

        // TODO: for performance do not wrap the key in a Map for a single ID case. All other key-related methods
        //   would need to be aligned with this assumption
        Map<String, Object> idMap = new HashMap<>();

        for (AgIdPart id : entity.getIdParts()) {
            idMap.put(id.getName(), id.getReader().value(object));
        }

        return idMap;
    }

    @Override
    public Object keyForUpdate(EntityUpdate<T> update) {
        return update.getId();
    }
}
