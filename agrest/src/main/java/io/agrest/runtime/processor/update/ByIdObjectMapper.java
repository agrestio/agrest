package io.agrest.runtime.processor.update;

import io.agrest.AgException;
import io.agrest.EntityUpdate;
import io.agrest.ObjectMapper;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.parser.ASTEqual;
import org.apache.cayenne.exp.parser.ASTPath;

import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.apache.cayenne.exp.ExpressionFactory.joinExp;

class ByIdObjectMapper<T> implements ObjectMapper<T> {

    private AgEntity<T> entity;

    ByIdObjectMapper(AgEntity<T> entity) {
        this.entity = Objects.requireNonNull(entity);
    }

    @Override
    public Expression expressionForKey(Object key) {

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

        Collection<AgAttribute> ids = entity.getIds();
        int len = ids.size();
        if (len == 1) {
            return match(ids.iterator().next().getPathExp(), idMap);
        }

        List<Expression> exps = new ArrayList<>(len);
        for (AgAttribute id : ids) {
            exps.add(match(id.getPathExp(), idMap));
        }
        return joinExp(Expression.AND, exps);
    }

    private Expression match(ASTPath path, Map<String, Object> idMap) {

        Object value = idMap.get(path.getPath());
        if (value == null) {
            throw new AgException(Status.BAD_REQUEST, "No ID value for path: " + path);
        }

        return new ASTEqual(path, value);
    }

    @Override
    public Object keyForObject(T object) {
        Map<String, Object> idMap = new HashMap<>();
        for (AgAttribute id : entity.getIds()) {
            idMap.put(id.getPathExp().getPath(), id.getPropertyReader().value(object, id.getName()));
        }
        return idMap;
    }

    @Override
    public Object keyForUpdate(EntityUpdate<T> update) {
        return update.getId();
    }

}
