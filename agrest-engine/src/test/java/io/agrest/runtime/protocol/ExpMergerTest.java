package io.agrest.runtime.protocol;

import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.base.protocol.Exp;
import io.agrest.compiler.AgEntityCompiler;
import io.agrest.compiler.AnnotationsAgEntityCompiler;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.LazyAgDataMap;
import io.agrest.runtime.entity.ExpMerger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExpMergerTest {

    private static AgDataMap dataMap;
    private static ExpMerger merger;
    private ResourceEntity<Tr> entity;

    @BeforeAll
    public static void beforeAll() {

        AgEntityCompiler compiler = new AnnotationsAgEntityCompiler(Collections.emptyMap());
        dataMap = new LazyAgDataMap(Collections.singletonList(compiler));
        merger = new ExpMerger();
    }

    @BeforeEach
    public void beforeEach() {
        entity = new RootResourceEntity<>(dataMap.getEntity(Tr.class));
    }

    @Test
    public void testMerge_Empty() {
        merger.merge(entity, Exp.simple("a = 12345 and b = 'John Smith'"));
        assertEquals(Exp.simple("a = 12345 and b = 'John Smith'"), entity.getQualifier());
    }

    @Test
    public void testMerge_OverExisting() {
        entity.andQualifier(Exp.simple("c = true"));
        merger.merge(entity, Exp.simple("a = 12345 and b = 'John Smith'"));
        assertEquals(Exp.simple("c = true").and(Exp.simple("a = 12345 and b = 'John Smith'")), entity.getQualifier());
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
