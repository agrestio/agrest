package io.agrest.runtime;

import io.agrest.AgException;
import io.agrest.spi.AgExceptionMapper;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AgExceptionMappersTest {

    @Test
    public void toAgException_Default() {
        AgException e = new AgExceptionMappers(Collections.emptyMap()).toAgException(new Throwable());
        assertException(500, "Exception processing Agrest request", e);
    }

    @Test
    public void toAgException() {

        Map<String, AgExceptionMapper<?>> map = new HashMap<>();
        map.put(IllegalStateException.class.getName(), new IllegalStateMapper());
        map.put(IllegalArgumentException.class.getName(), new IllegalArgumentMapper());

        AgExceptionMappers mappers = new AgExceptionMappers(map);

        AgException illegalState = mappers.toAgException(new IllegalStateException());
        assertException(500, "_IllegalStateException_", illegalState);

        AgException illegalArgument = mappers.toAgException(new IllegalArgumentException());
        assertException(400, "_IllegalArgumentException_", illegalArgument);
    }

    @Test
    public void locateMapper_ExplicitlyMapped() {
        Map<String, AgExceptionMapper<?>> map = new HashMap<>();
        map.put(RuntimeException.class.getName(), new RuntimeMapper());
        map.put(IllegalArgumentException.class.getName(), new IllegalArgumentMapper());

        AgExceptionMappers mappers = new AgExceptionMappers(map);

        AgExceptionMapper<?> mapper = mappers.locateMapper(IllegalArgumentException.class);
        assertTrue(mapper instanceof IllegalArgumentMapper);
        assertSame(mapper, mappers.locateMapper(IllegalArgumentException.class));
    }

    @Test
    public void locateMapper_SuperException() {
        Map<String, AgExceptionMapper<?>> map = new HashMap<>();
        map.put(RuntimeException.class.getName(), new RuntimeMapper());
        map.put(IllegalArgumentException.class.getName(), new IllegalArgumentMapper());

        AgExceptionMappers mappers = new AgExceptionMappers(map);

        AgExceptionMapper<?> mapper = mappers.locateMapper(IllegalStateException.class);
        assertTrue(mapper instanceof RuntimeMapper);
        assertSame(mapper, mappers.locateMapper(IllegalStateException.class));
        assertSame(mapper, mappers.locateMapper(RuntimeException.class));
    }

    @Test
    public void locateMapper_DefaultMapper() {
        Map<String, AgExceptionMapper<?>> map = new HashMap<>();
        map.put(RuntimeException.class.getName(), new RuntimeMapper());
        map.put(IllegalArgumentException.class.getName(), new IllegalArgumentMapper());

        AgExceptionMappers mappers = new AgExceptionMappers(map);

        AgExceptionMapper<?> mapper = mappers.locateMapper(Error.class);
        assertSame(mappers.defaultMapper(), mapper);
        assertSame(mapper, mappers.locateMapper(Error.class));
    }

    private void assertException(int expectedStatus, String expectedMessage, AgException exception) {
        assertEquals(expectedStatus, exception.getStatus(), "Unexpected status");
        assertEquals(expectedMessage, exception.getMessage(), "Unexpected message");
    }

    static class RuntimeMapper implements AgExceptionMapper<RuntimeException> {

        @Override
        public AgException toAgException(RuntimeException e) {
            return AgException.internalServerError(e, "_RuntimeException_");
        }
    }

    static class IllegalStateMapper implements AgExceptionMapper<IllegalStateException> {

        @Override
        public AgException toAgException(IllegalStateException e) {
            return AgException.internalServerError(e, "_IllegalStateException_");
        }
    }

    static class IllegalArgumentMapper implements AgExceptionMapper<IllegalArgumentException> {

        @Override
        public AgException toAgException(IllegalArgumentException e) {
            return AgException.badRequest(e, "_IllegalArgumentException_");
        }
    }

}
