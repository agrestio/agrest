package com.nhl.link.rest.encoder;

import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.jackson.JacksonService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Encoders {

    private static IJacksonService JACKSON = new JacksonService();

    public static String toJson(Encoder encoder, Object value) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            JACKSON.outputJson(g ->
                    encoder.encode(null, value, g), out);
        } catch (IOException e) {
            throw new RuntimeException("Encoding error: " + e.getMessage());
        }

        return new String(out.toByteArray());
    }
}
