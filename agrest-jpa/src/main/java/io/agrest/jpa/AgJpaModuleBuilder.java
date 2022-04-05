package io.agrest.jpa;

import io.agrest.jpa.persister.AgJpaPersister;
import io.agrest.jpa.persister.IAgJpaPersister;
import jakarta.persistence.EntityManagerFactory;

/**
 * @since 5.0
 */
public class AgJpaModuleBuilder {

    private IAgJpaPersister persister;

    private AgJpaModuleBuilder() {
    }

    public static AgJpaModuleBuilder builder() {
        return new AgJpaModuleBuilder();
    }

    public static AgJpaModule build(EntityManagerFactory entityManagerFactory) {
        return new AgJpaModuleBuilder().entityManagerFactory(entityManagerFactory).build();
    }

    public AgJpaModuleBuilder entityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.persister = new AgJpaPersister(entityManagerFactory);
        return this;
    }

    public AgJpaModuleBuilder persister(IAgJpaPersister persister) {
        this.persister = persister;
        return this;
    }

    public AgJpaModule build() {
        return new AgJpaModule(persister);
    }
}
