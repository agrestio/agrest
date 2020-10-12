package io.agrest.runtime.protocol;

import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.base.protocol.CayenneExp;
import io.agrest.meta.compiler.AgEntityCompiler;
import io.agrest.meta.compiler.PojoEntityCompiler;
import io.agrest.runtime.entity.CayenneExpMerger;
import io.agrest.runtime.meta.MetadataService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CayenneExpMergerTest {

    private static MetadataService metadataService;
    private static CayenneExpMerger merger;
    private ResourceEntity<Tr> entity;

    @BeforeAll
    public static void beforeAll() {

        AgEntityCompiler compiler = new PojoEntityCompiler(Collections.emptyMap());
        metadataService = new MetadataService(Collections.singletonList(compiler));
        merger = new CayenneExpMerger();
    }

    @BeforeEach
    public void beforeEach() {
        entity = new RootResourceEntity<>(metadataService.getAgEntity(Tr.class), null);
    }

    @Test
    public void testMerge_Empty() {
        merger.merge(entity, new CayenneExp("a = 12345 and b = 'John Smith'"));
        assertEquals(1, entity.getQualifiers().size());
        assertEquals(new CayenneExp("a = 12345 and b = 'John Smith'"), entity.getQualifiers().get(0));
    }

    @Test
    public void testMerge_OverExisting() {
        entity.getQualifiers().add(new CayenneExp("c = true"));
        merger.merge(entity, new CayenneExp("a = 12345 and b = 'John Smith'"));
        assertEquals(2, entity.getQualifiers().size());
        assertEquals(new CayenneExp("c = true"), entity.getQualifiers().get(0));
        assertEquals(new CayenneExp("a = 12345 and b = 'John Smith'"), entity.getQualifiers().get(1));
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

        @AgAttribute
        public boolean getC() {
            throw new UnsupportedOperationException();
        }

        @AgAttribute
        public double getD() {
            throw new UnsupportedOperationException();
        }

        @AgAttribute
        public Date getE() {
            throw new UnsupportedOperationException();
        }
    }
}
