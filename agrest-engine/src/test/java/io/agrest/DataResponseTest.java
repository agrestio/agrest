package io.agrest;

import io.agrest.encoder.Encoder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class DataResponseTest {

    @Test
    public void testOf() {

        List<Tr> objects = asList(new Tr(), new Tr());
        Encoder encoder = mock(Encoder.class);

        DataResponse<Tr> response = DataResponse.of(201, objects, encoder);
        assertNotNull(response);
        assertEquals(201, response.getStatus());
        assertSame(objects, response.getObjects());
        assertSame(encoder, response.getEncoder());
    }

    public static class Tr {
    }
}
