package io.agrest.runtime.encoder;

import io.agrest.DataResponse;
import io.agrest.runtime.jackson.IJacksonService;
import io.agrest.runtime.jackson.JacksonService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

class Encoders {

    private static final IJacksonService JACKSON = new JacksonService();

    public static String toJson(DataResponse<?> response) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            JACKSON.outputJson(g -> response.getEncoder().encode(null, response, g), out);
        } catch (IOException e) {
            throw new RuntimeException("Encoding error: " + e.getMessage());
        }

        return out.toString();
    }
}
