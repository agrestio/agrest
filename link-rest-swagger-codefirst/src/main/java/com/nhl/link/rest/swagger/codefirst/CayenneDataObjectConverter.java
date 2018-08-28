package com.nhl.link.rest.swagger.codefirst;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.jackson.AbstractModelConverter;
import io.swagger.v3.oas.models.media.Schema;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author vyarmolovich
 * 8/28/18
 */
public class CayenneDataObjectConverter extends AbstractModelConverter {

    static final Set<String> ignored = new HashSet();

    static {
        // ignores cayenne package to generate clean data model
        ignored.add("org.apache.cayenne");
    }

    public CayenneDataObjectConverter(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public Schema resolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
        if (isIgnored(type.getType().getTypeName())) {
            return null;
        }
        return super.resolve(type, context, chain);
    }

    protected boolean isIgnored(String className) {
        if (className != null) {
            for (String name : ignored) {
                if (className.contains(name)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }
}
