package io.agrest.encoder;

import io.agrest.runtime.jackson.IJacksonService;
import io.agrest.runtime.jackson.JacksonService;

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

    public static String jsonString(String s) {
        // it's fine, if the string itself contains quotes
        return "\"" + s + "\"";
    }
}
