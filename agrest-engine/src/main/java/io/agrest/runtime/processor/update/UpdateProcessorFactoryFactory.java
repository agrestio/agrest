package io.agrest.runtime.processor.update;

import io.agrest.runtime.UpdateOperation;

import java.util.EnumMap;
import java.util.Objects;

/**
 * @since 2.7
 */
public class UpdateProcessorFactoryFactory {

    private final EnumMap<UpdateOperation, UpdateProcessorFactory> factories;

    public UpdateProcessorFactoryFactory(EnumMap<UpdateOperation, UpdateProcessorFactory> factories) {
        this.factories = factories;
    }

    public UpdateProcessorFactory getFactory(UpdateOperation operation) {
        return Objects.requireNonNull(factories.get(operation),
                "No UpdateProcessorFactory defined for operation: " + operation);
    }
}
