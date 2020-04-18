package io.agrest.constraints;

import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgEntity;
import io.agrest.meta.LazyAgDataMap;
import io.agrest.meta.compiler.AgEntityCompiler;
import io.agrest.meta.compiler.PojoEntityCompiler;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Date;

import static org.junit.Assert.*;

public class ConstraintsBuilderTest {

    private AgEntity<T> entity;

    @Before
    public void before() {
        AgEntityCompiler compiler = new PojoEntityCompiler(Collections.emptyMap());
        AgDataMap dataMap = new LazyAgDataMap(Collections.singletonList(compiler));
        this.entity = dataMap.getEntity(T.class);
    }

    @Test
    public void testExcludeAll() {
        ConstraintsBuilder<T> tc = Constraint.excludeAll(T.class);
        ConstrainedAgEntity<T> result = tc.apply(entity);

        assertNotNull(result);
        assertTrue(result.getAttributes().isEmpty());
        assertTrue(result.getChildren().isEmpty());
        assertFalse(result.isIdIncluded());
    }

    @Test
    public void testIdOnly() {
        ConstraintsBuilder<T> tc = Constraint.idOnly(T.class);
        ConstrainedAgEntity<T> result = tc.apply(entity);

        assertNotNull(result);
        assertTrue(result.getAttributes().isEmpty());
        assertTrue(result.getChildren().isEmpty());
        assertTrue(result.isIdIncluded());
    }

    @Test
    public void testIdAndAttributes() {

        ConstraintsBuilder<T> tc = Constraint.idAndAttributes(T.class);
        ConstrainedAgEntity<T> result = tc.apply(entity);

        assertNotNull(result);
        assertEquals(3, result.getAttributes().size());
        assertTrue(result.getChildren().isEmpty());
        assertTrue(result.isIdIncluded());
    }

    @Test
    public void testExcludeProperties() {

        ConstraintsBuilder<T> tc = Constraint.idAndAttributes(T.class).excludeProperties("b", "d");
        ConstrainedAgEntity<T> result = tc.apply(entity);

        assertNotNull(result);
        assertEquals(1, result.getAttributes().size());
        assertTrue(result.getChildren().isEmpty());
        assertTrue(result.isIdIncluded());
    }

    public class T {

        private int id;
        private boolean b;
        private Date d;
        private int i;

        @AgId
        public int getId() {
            return id;
        }

        @AgAttribute
        public boolean isB() {
            return b;
        }

        @AgAttribute
        public Date getD() {
            return d;
        }

        @AgAttribute
        public int getI() {
            return i;
        }
    }
}
