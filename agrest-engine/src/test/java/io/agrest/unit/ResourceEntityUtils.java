package io.agrest.unit;

import io.agrest.ResourceEntity;
import io.agrest.meta.DefaultAgAttribute;
import io.agrest.property.BeanPropertyReader;
import org.apache.cayenne.exp.parser.ASTObjPath;

public class ResourceEntityUtils {

    public static void appendAttribute(ResourceEntity<?> entity, String name) {
        appendAttribute(entity, name, String.class);
    }

    public static void appendAttribute(ResourceEntity<?> entity, String name, Class<?> type) {
        entity.addAttribute(new DefaultAgAttribute(name, type, new ASTObjPath(name), BeanPropertyReader.reader()), false);
    }
}
