package io.agrest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AgExceptionTest {

    @Test
    public void of() {
        AgException e = AgException.of(101, "a %s b", "X");
        assertEquals("a X b", e.getMessage());
        assertNull(e.getCause());
        assertEquals(101, e.getStatus());
    }

    @Test
    public void of_Exception() {
        Throwable cause = new Throwable();
        AgException e = AgException.of(101, cause, "a %s b", "X");
        assertEquals("a X b", e.getMessage());
        assertSame(cause, e.getCause());
        assertEquals(101, e.getStatus());
    }

    @Test
    public void badRequest() {
        AgException e = AgException.badRequest();
        assertNull(e.getMessage());
        assertNull(e.getCause());
        assertEquals(400, e.getStatus());
    }

    @Test
    public void badRequest_Message() {
        AgException e = AgException.badRequest("a %s b", "X");
        assertEquals("a X b", e.getMessage());
        assertNull(e.getCause());
        assertEquals(400, e.getStatus());
    }

    @Test
    public void badRequest_Exception() {
        Throwable cause = new Throwable();
        AgException e = AgException.badRequest(cause, "a %s b", "X");
        assertEquals("a X b", e.getMessage());
        assertSame(cause, e.getCause());
        assertEquals(400, e.getStatus());
    }

    @Test
    public void forbidden() {
        AgException e = AgException.forbidden("a %s b", "X");
        assertEquals("a X b", e.getMessage());
        assertNull(e.getCause());
        assertEquals(403, e.getStatus());
    }

    @Test
    public void forbidden_Exception() {
        Throwable cause = new Throwable();
        AgException e = AgException.forbidden(cause, "a %s b", "X");
        assertEquals("a X b", e.getMessage());
        assertSame(cause, e.getCause());
        assertEquals(403, e.getStatus());
    }
}
