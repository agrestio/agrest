package io.agrest.parser.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.Collection;
import java.util.function.Supplier;

public class CollectionConverter<T extends Collection<E>, E> extends AbstractConverter<T> {

    private final Supplier<T> containerSupplier;
    private final JsonValueConverter<E> elementConverter;

    public CollectionConverter(Supplier<T> containerSupplier,
                               JsonValueConverter<E> elementConverter) {
        this.containerSupplier = containerSupplier;
        this.elementConverter = elementConverter;
    }

    @Override
    public T valueNonNull(JsonNode node) {
        if (!node.isArray()) {
            throw new IllegalArgumentException("Node is not an array: " + node.getNodeType().name());
        }

        T container = containerSupplier.get();
        ArrayNode array = (ArrayNode) node;
        array.forEach(child -> container.add(elementConverter.value(child)));
        return container;
    }
}
