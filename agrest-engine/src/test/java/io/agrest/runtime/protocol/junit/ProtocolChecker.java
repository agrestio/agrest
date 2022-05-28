package io.agrest.runtime.protocol.junit;

import io.agrest.protocol.Sort;
import io.agrest.runtime.processor.select.SelectContext;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class ProtocolChecker {

    private final SelectContext<?> context;

    public ProtocolChecker(SelectContext<?> context) {
        this.context = context;
    }

    public ProtocolChecker assertExp(String expectedExp) {
        // TODO: may require proper "toString" in exps other than SimpleExp
        assertEquals(expectedExp, context.getEntity().getExp().toString());
        return this;
    }

    public ProtocolChecker assertSort(Sort... expectedOrderings) {
        List<Sort> actualOrderings = context.getEntity().getOrderings();
        assertEquals(expectedOrderings.length, actualOrderings.size());
        assertArrayEquals(expectedOrderings, actualOrderings.toArray(new Sort[0]));
        return this;
    }

    public ProtocolChecker assertIdIncluded() {
        assertTrue(context.getEntity().isIdIncluded());
        return this;
    }

    public ProtocolChecker assertIdExcluded() {
        assertFalse(context.getEntity().isIdIncluded());
        return this;
    }

    public ProtocolChecker assertAttributes(String... expectedAttributes) {
        String expectedAsString = asList(expectedAttributes).stream().sorted().collect(Collectors.joining(","));
        String actualAsString = context.getEntity().getAttributes().keySet().stream().sorted().collect(Collectors.joining(","));
        assertEquals(expectedAsString, actualAsString);
        return this;
    }

    public ProtocolChecker assertRelationships(String... expectedRelationships) {
        String expectedAsString = asList(expectedRelationships).stream().sorted().collect(Collectors.joining(","));
        String actualAsString = context.getEntity().getChildren().keySet().stream().sorted().collect(Collectors.joining(","));
        assertEquals(expectedAsString, actualAsString);
        return this;
    }


}
