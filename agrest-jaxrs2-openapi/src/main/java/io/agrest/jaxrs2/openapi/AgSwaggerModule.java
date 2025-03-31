package io.agrest.jaxrs2.openapi;

import io.agrest.jaxrs2.openapi.modelconverter.AgEntityModelConverter;
import org.apache.cayenne.di.Binder;
import org.apache.cayenne.di.ListBuilder;
import org.apache.cayenne.di.Module;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @deprecated in favor of Jakarta version (JAX-RS 3)
 */
@Deprecated(since = "5.0", forRemoval = true)
public class AgSwaggerModule implements Module {

    private final Set<String> entityPackages;

    /**
     * @since 5.0
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @since 5.0
     */
    public static AgSwaggerModule build() {
        return builder().build();
    }


    protected AgSwaggerModule(Set<String> entityPackages) {
        this.entityPackages = entityPackages;
    }

    @Override
    public void configure(Binder binder) {
        ListBuilder<String> diPackages = binder.bindList(String.class, AgEntityModelConverter.BINDING_ENTITY_PACKAGES);
        entityPackages.forEach(diPackages::add);
    }

    public static class Builder {
        private final Set<String> entityPackages;

        private Builder() {
            this.entityPackages = new LinkedHashSet<>();
        }

        public AgSwaggerModule build() {
            return new AgSwaggerModule(entityPackages);
        }

        public Builder entityPackage(Package aPackage) {
            return entityPackage(aPackage.getName());
        }

        public Builder entityPackage(String aPackage) {
            entityPackages.add(aPackage);
            return this;
        }

        public Builder entityPackages(String... packages) {
            Collections.addAll(entityPackages, packages);
            return this;
        }
    }
}
