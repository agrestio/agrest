package io.agrest.jpa.compiler;

import io.agrest.jpa.pocessor.JpaUtil;
import io.agrest.property.PropertyReader;
import jakarta.persistence.metamodel.SingularAttribute;

/**
 * @since 5.0
 */
public class JpaPropertyReader {

    public static PropertyReader reader(SingularAttribute<?, ?> attribute) {
        return o -> JpaUtil.readProperty(o, attribute);
    }

}
