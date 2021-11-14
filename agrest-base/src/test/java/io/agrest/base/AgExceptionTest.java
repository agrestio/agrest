package io.agrest.base;

import io.agrest.AgException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AgExceptionTest {

    @Test
    public void testOf() {
        AgException e = AgException.of(101, "a %s b", "X");
        assertEquals("a X b", e.getMessage());
        assertNull(e.getCause());
        assertEquals(101, e.getStatus());
    }

    @Test
    public void testOf_Exception() {
        Throwable cause = new Throwable();
        AgException e = AgException.of(101, cause, "a %s b", "X");
        assertEquals("a X b", e.getMessage());
        assertSame(cause, e.getCause());
        assertEquals(101, e.getStatus());
    }

    @Test
    public void testBadRequest() {
        AgException e = AgException.badRequest();
        assertNull(e.getMessage());
        assertNull(e.getCause());
        assertEquals(400, e.getStatus());
    }

    @Test
    public void testBadRequest_Message() {
        AgException e = AgException.badRequest("a %s b", "X");
        assertEquals("a X b", e.getMessage());
        assertNull(e.getCause());
        assertEquals(400, e.getStatus());
    }

    @Test
    public void testBadRequest_Exception() {
        Throwable cause = new Throwable();
        AgException e = AgException.badRequest(cause, "a %s b", "X");
        assertEquals("a X b", e.getMessage());
        assertSame(cause, e.getCause());
        assertEquals(400, e.getStatus());
    }

    @Test
    public void testForbidden() {
        AgException e = AgException.forbidden("a %s b", "X");
        assertEquals("a X b", e.getMessage());
        assertNull(e.getCause());
        assertEquals(403, e.getStatus());
    }

    @Test
    public void testForbidden_Exception() {
        Throwable cause = new Throwable();
        AgException e = AgException.forbidden(cause, "a %s b", "X");
        assertEquals("a X b", e.getMessage());
        assertSame(cause, e.getCause());
        assertEquals(403, e.getStatus());
    }
}
