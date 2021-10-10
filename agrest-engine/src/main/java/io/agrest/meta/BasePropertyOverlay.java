package io.agrest.meta;

import java.util.Objects;

/**
 * @since 4.7
 */
public abstract class BasePropertyOverlay {

    protected final String name;
    protected final Class<?> sourceType;

    public BasePropertyOverlay(String name, Class<?> sourceType) {
        this.name = Objects.requireNonNull(name);
        this.sourceType = Objects.requireNonNull(sourceType);
    }

    protected <T> T propertyOrDefault(T value, T ifMissing) {
        return value != null ? value : ifMissing;
    }

    protected <T> T requiredProperty(String property, T value) {
        if (value == null) {
            String message = String.format(
                    "Overlay can't be resolved - '%s' is not defined and no overlaid property '%s.%s' exists",
                    property,
                    this.sourceType.getSimpleName(),
                    this.name);

            throw new IllegalStateException(message);
        }

        return value;
    }
}
