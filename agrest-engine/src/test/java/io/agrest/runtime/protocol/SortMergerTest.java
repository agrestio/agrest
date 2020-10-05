package io.agrest.runtime.protocol;

import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.base.protocol.Sort;
import io.agrest.meta.compiler.AgEntityCompiler;
import io.agrest.meta.compiler.PojoEntityCompiler;
import io.agrest.runtime.entity.SortMerger;
import io.agrest.runtime.meta.MetadataService;
import io.agrest.runtime.path.PathDescriptorManager;
import org.apache.cayenne.query.Ordering;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Iterator;

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
        merger = new SortMerger(new PathDescriptorManager());
    }

    @BeforeEach
    public void beforeEach() {
        this.entity = new RootResourceEntity<>(metadataService.getAgEntity(Tr.class), null);
    }

    @Test
    public void testMerge_Array() {

        merger.merge(entity, asList(new Sort("a"), new Sort("b")));

        assertEquals(2, entity.getOrderings().size());

        Iterator<Ordering> it = entity.getOrderings().iterator();
        Ordering o1 = it.next();
        Ordering o2 = it.next();

        assertEquals("a", o1.getSortSpecString());
        assertEquals("b", o2.getSortSpecString());
    }

    @Test
    public void testMerge_Simple() {

        merger.merge(entity, Collections.singletonList(new Sort("a")));

        assertEquals(1, entity.getOrderings().size());

        Iterator<Ordering> it = entity.getOrderings().iterator();
        Ordering o1 = it.next();

        assertEquals("a", o1.getSortSpecString());
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
