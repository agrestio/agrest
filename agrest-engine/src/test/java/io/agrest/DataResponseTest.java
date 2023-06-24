package io.agrest;

import io.agrest.encoder.Encoder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class DataResponseTest {

    @Test
    public void of() {

        List<Tr> objects = asList(new Tr(), new Tr());
        Encoder encoder = mock(Encoder.class);

        DataResponse<Tr> response = DataResponse.of(objects).status(201).total(1).encoder(encoder).build();
        assertNotNull(response);
        assertEquals(201, response.getStatus());
        assertSame(objects, response.getData());
        assertEquals(1, response.getTotal());
        assertSame(encoder, response.getEncoder());
    }

    public static class Tr {
    }
}
