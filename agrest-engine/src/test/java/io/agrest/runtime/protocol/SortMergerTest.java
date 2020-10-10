package io.agrest.runtime.protocol;

import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.base.protocol.Dir;
import io.agrest.base.protocol.Sort;
import io.agrest.meta.compiler.AgEntityCompiler;
import io.agrest.meta.compiler.PojoEntityCompiler;
import io.agrest.runtime.entity.SortMerger;
import io.agrest.runtime.meta.MetadataService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SortMergerTest {

    private static SortMerger merger;
    private static MetadataService metadataService;

    private ResourceEntity<?> entity;

    @BeforeAll
    public static void beforeAll() {
        AgEntityCompiler compiler = new PojoEntityCompiler(Collections.emptyMap());
        metadataService = new MetadataService(Collections.singletonList(compiler));
        merger = new SortMerger();
    }

    @BeforeEach
    public void beforeEach() {
        this.entity = new RootResourceEntity<>(metadataService.getAgEntity(Tr.class), null);
    }

    @Test
    public void testMerge_Array() {

        merger.merge(entity, asList(new Sort("a"), new Sort("b")));

        assertEquals(2, entity.getOrderings().size());
        assertEquals(new Sort("a", Dir.ASC), entity.getOrderings().get(0));
        assertEquals(new Sort("b", Dir.ASC), entity.getOrderings().get(1));
    }

    @Test
    public void testMerge_Simple() {

        merger.merge(entity, Collections.singletonList(new Sort("a")));

        assertEquals(1, entity.getOrderings().size());
        assertEquals(new Sort("a", Dir.ASC), entity.getOrderings().get(0));
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
