package io.agrest.runtime.protocol;

import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.protocol.Direction;
import io.agrest.protocol.Sort;
import io.agrest.compiler.AgEntityCompiler;
import io.agrest.compiler.AnnotationsAgEntityCompiler;
import io.agrest.meta.AgSchema;
import io.agrest.meta.LazySchema;
import io.agrest.runtime.entity.SortMerger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SortMergerTest {

    private static SortMerger merger;
    private static AgSchema schema;

    private ResourceEntity<?> entity;

    @BeforeAll
    public static void beforeAll() {
        AgEntityCompiler compiler = new AnnotationsAgEntityCompiler(Collections.emptyMap());
        schema = new LazySchema(Collections.singletonList(compiler));
        merger = new SortMerger();
    }

    @BeforeEach
    public void beforeEach() {
        this.entity = new RootResourceEntity<>(schema.getEntity(Tr.class));
    }

    @Test
    public void testMerge_Array() {

        merger.merge(entity, asList(new Sort("a"), new Sort("b")));

        assertEquals(2, entity.getOrderings().size());
        assertEquals(new Sort("a", Direction.asc), entity.getOrderings().get(0));
        assertEquals(new Sort("b", Direction.asc), entity.getOrderings().get(1));
    }

    @Test
    public void testMerge_Simple() {

        merger.merge(entity, Collections.singletonList(new Sort("a")));

        assertEquals(1, entity.getOrderings().size());
        assertEquals(new Sort("a", Direction.asc), entity.getOrderings().get(0));
    }

    public static class Tr {

        @AgId
        public int getId() {
            throw new UnsupportedOperationException();
        }

        @AgAttribute
        public int getA() {
            throw new UnsupportedOperationException();
        }

        @AgAttribute
        public String getB() {
            throw new UnsupportedOperationException();
        }
    }

}
