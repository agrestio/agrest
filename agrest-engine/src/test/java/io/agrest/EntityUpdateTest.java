package io.agrest;

import io.agrest.meta.AgEntity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class EntityUpdateTest {

    @Test
    public void getToMany() {
        EntityUpdate<Object> u = new EntityUpdate<>(mock(AgEntity.class));
        assertNull(u.getToMany("a"));

        u.emptyToMany("a");
        assertEquals(List.of(), u.getToMany("a"));

        EntityUpdate<Object> au = new EntityUpdate<>(mock(AgEntity.class));
        u.addToMany("a", au);

        assertEquals(List.of(au), u.getToMany("a"));
    }
}
