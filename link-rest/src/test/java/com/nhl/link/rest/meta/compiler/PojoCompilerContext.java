package com.nhl.link.rest.meta.compiler;

import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrEntityBuilder;
import com.nhl.link.rest.meta.compiler.CompilerContext;

import java.util.HashMap;
import java.util.Map;

public class PojoCompilerContext implements CompilerContext {

    private Map<Class<?>, LrEntity<?>> entityMap;

    public PojoCompilerContext() {
        entityMap = new HashMap<>();
    }

    @Override
    public <T> LrEntity<T> addEntityIfAbsent(Class<T> type, LrEntity<T> entity) {
        entityMap.put(type, entity);
        return entity;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> LrEntity<T> getOrCreateEntity(Class<T> type) {

        LrEntity<T> entity = (LrEntity<T>) entityMap.get(type);
        if (entity == null) {
            entity = addEntityIfAbsent(type, LrEntityBuilder.build(type, this));
        }
        return entity;
    }
}
