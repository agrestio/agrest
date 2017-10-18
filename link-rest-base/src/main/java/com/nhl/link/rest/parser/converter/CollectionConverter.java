package com.nhl.link.rest.parser.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.Collection;
import java.util.function.Supplier;

public class CollectionConverter implements JsonValueConverter {

    private final Supplier<Collection<Object>> containerSupplier;
    private final JsonValueConverter elementConverter;

    public CollectionConverter(Supplier<Collection<Object>> containerSupplier,
                               JsonValueConverter elementConverter) {
        this.containerSupplier = containerSupplier;
        this.elementConverter = elementConverter;
    }

    @Override
    public Object value(JsonNode node) {
        if (!node.isArray()) {
            throw new IllegalArgumentException("Node is not an array: " + node.getNodeType().name());
        }

        Collection<Object> container = containerSupplier.get();
        ArrayNode array = (ArrayNode) node;
        array.forEach(child -> container.add(elementConverter.value(child)));
        return container;
    }
}
