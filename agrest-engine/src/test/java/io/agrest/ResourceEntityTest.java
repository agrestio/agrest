package io.agrest;

import io.agrest.meta.AgEntity;
import io.agrest.junit.pojo.P1;
import io.agrest.protocol.Exp;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResourceEntityTest {

    private <T> AgEntity<T> mockEntity(Class<T> type) {
        AgEntity<T> entity = mock(AgEntity.class);
        when(entity.getName()).thenReturn("mock");
        when(entity.getType()).thenReturn(type);
        return entity;
    }

    @Test
    public void testQualifier() {
        ResourceEntity<P1> e = new RootResourceEntity<>(mockEntity(P1.class));
        assertNull(e.getExp());

        e.andExp(Exp.from("a = 1"));
        assertEquals(Exp.from("a = 1"), e.getExp());

        e.andExp(Exp.from("b = 2"));
        assertEquals(Exp.from("a = 1").and(Exp.from("b = 2")), e.getExp());
    }


    @Test
    public void testGetDataWindow_NoOffsetLimit() {
        List<P1> data = asList(new P1(), new P1(), new P1());

        ResourceEntity<P1> e = new RootResourceEntity<>(mockEntity(P1.class));
        assertSame(data, e.getDataWindow(data));
    }

    @Test
    public void testGetDataWindow_Offset() {
        List<P1> data = asList(new P1(), new P1(), new P1());

        ResourceEntity<P1> e = new RootResourceEntity<>(mockEntity(P1.class));
        e.setStart(1);
        assertEquals(asList(data.get(1), data.get(2)), e.getDataWindow(data));
    }

    @Test
    public void testGetDataWindow_OffsetPastEnd() {
        List<P1> data = asList(new P1(), new P1(), new P1());

        ResourceEntity<P1> e = new RootResourceEntity<>(mockEntity(P1.class));
        e.setStart(4);
        assertEquals(Collections.emptyList(), e.getDataWindow(data));
    }

    @Test
    public void testGetDataWindow_Limit() {
        List<P1> data = asList(new P1(), new P1(), new P1());

        ResourceEntity<P1> e = new RootResourceEntity<>(mockEntity(P1.class));
        e.setLimit(2);
        assertEquals(asList(data.get(0), data.get(1)), e.getDataWindow(data));
    }

    @Test
    public void testGetDataWindow_LimitPastEnd() {
        List<P1> data = asList(new P1(), new P1(), new P1());

        ResourceEntity<P1> e = new RootResourceEntity<>(mockEntity(P1.class));
        e.setLimit(4);
        assertEquals(asList(data.get(0), data.get(1), data.get(2)), e.getDataWindow(data));
    }

    @Test
    public void testGetDataWindow_OffsetLimit() {
        List<P1> data = asList(new P1(), new P1(), new P1());

        ResourceEntity<P1> e = new RootResourceEntity<>(mockEntity(P1.class));
        e.setStart(1);
        e.setLimit(1);
        assertEquals(asList(data.get(1)), e.getDataWindow(data));
    }

    @Test
    public void testGetDataWindow_OffsetNegativeLimit() {
        List<P1> data = asList(new P1(), new P1(), new P1());

        ResourceEntity<P1> e = new RootResourceEntity<>(mockEntity(P1.class));
        e.setStart(1);
        e.setLimit(-5);
        assertEquals(asList(data.get(1), data.get(2)), e.getDataWindow(data));
    }

    @Test
    public void testGetDataWindow_LimitNegativeOffset() {
        List<P1> data = asList(new P1(), new P1(), new P1());

        ResourceEntity<P1> e = new RootResourceEntity<>(mockEntity(P1.class));
        e.setLimit(2);
        e.setStart(-2);
        assertEquals(asList(data.get(0), data.get(1)), e.getDataWindow(data));
    }

}
