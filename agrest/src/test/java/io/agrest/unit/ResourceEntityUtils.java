package io.agrest.unit;

import io.agrest.ResourceEntity;
import io.agrest.meta.DefaultAgAttribute;
import io.agrest.property.BeanPropertyReader;
import org.apache.cayenne.exp.parser.ASTObjPath;

public class ResourceEntityUtils {

    public static void appendAttribute(ResourceEntity<?> entity, String name, Class<?> type) {
        entity.getAttributes().put(name, new DefaultAgAttribute(name, type, new ASTObjPath(name), BeanPropertyReader.reader()));
    }
}
