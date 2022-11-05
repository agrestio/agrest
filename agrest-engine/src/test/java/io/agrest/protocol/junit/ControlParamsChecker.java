package io.agrest.protocol.junit;

import io.agrest.meta.AgAttribute;
import io.agrest.protocol.Exp;
import io.agrest.protocol.Sort;
import io.agrest.runtime.processor.select.SelectContext;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class ControlParamsChecker {

    private final SelectContext<?> context;

    public ControlParamsChecker(SelectContext<?> context) {
        this.context = context;
    }

    public ControlParamsChecker assertExp(Exp expectedExp) {
        assertEquals(expectedExp, context.getEntity().getExp());
        return this;
    }

    public ControlParamsChecker assertSort(Sort... expectedOrderings) {
        List<Sort> actualOrderings = context.getEntity().getOrderings();
        assertEquals(expectedOrderings.length, actualOrderings.size());
        assertArrayEquals(expectedOrderings, actualOrderings.toArray(new Sort[0]));
        return this;
    }

    public ControlParamsChecker assertIdIncluded() {
        assertTrue(context.getEntity().isIdIncluded());
        return this;
    }

    public ControlParamsChecker assertIdExcluded() {
        assertFalse(context.getEntity().isIdIncluded());
        return this;
    }

    public ControlParamsChecker assertAttributes(String... expectedAttributes) {
        String expectedAsString = Arrays.stream(expectedAttributes).sorted().collect(Collectors.joining(","));
        String actualAsString = context.getEntity().getBaseProjection().getAttributes().stream()
                .map(AgAttribute::getName)
                .sorted()
                .collect(Collectors.joining(","));
        assertEquals(expectedAsString, actualAsString);
        return this;
    }

    public ControlParamsChecker assertRelationships(String... expectedRelationships) {
        String expectedAsString = Arrays.stream(expectedRelationships).sorted().collect(Collectors.joining(","));
        String actualAsString = context.getEntity().getChildren().stream().map(re -> re.getIncoming().getName()).sorted().collect(Collectors.joining(","));
        assertEquals(expectedAsString, actualAsString);
        return this;
    }


}
