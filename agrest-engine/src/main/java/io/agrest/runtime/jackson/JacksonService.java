package io.agrest.runtime.jackson;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.agrest.AgException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class JacksonService implements IJacksonService {

    private final ObjectMapper objectMapper;
    private final JsonFactory factory;

    /**
     * @since 5.0
     */
    public static JacksonService create() {

        ObjectMapper mapper = new ObjectMapper();

        // TODO: revisit this code from 2015. Maybe this hack is not longer needed?

        SerializableString LINE_SEPARATOR = new SerializedString("\\u2028");
        SerializableString PARAGRAPH_SEPARATOR = new SerializedString("\\u2029");

        mapper.getFactory().setCharacterEscapes(new CharacterEscapes() {

            private static final long serialVersionUID = 3995801066651016289L;

            @Override
            public int[] getEscapeCodesForAscii() {
                return standardAsciiEscapesForJSON();
            }

            @Override
            public SerializableString getEscapeSequence(int ch) {
                // see ECMA-262 Section 7.3;
                // in most cases our client is browser,
                // and JSON is parsed into JS;
                // therefore these two whitespace characters,
                // which are perfectly valid in JSON but invalid in JS strings,
                // need to be escaped...
                switch (ch) {
                    case '\u2028':
                        return LINE_SEPARATOR;
                    case '\u2029':
                        return PARAGRAPH_SEPARATOR;
                    default:
                        return null;
                }
            }
        });

        // make sure mapper does not attempt closing streams it does not
        // manage... why is this even a default in jackson?
        mapper.getFactory().disable(Feature.AUTO_CLOSE_TARGET);

        // do not flush every time. why would we want to do that?
        // this is having a HUGE impact on serializers (5x speedup)
        mapper.disable(SerializationFeature.FLUSH_AFTER_WRITE_VALUE);

        return create(mapper);
    }

    /**
     * Creates a JacksonService instance based on a preconfigured ObjectMapper. Allows for external configuration of
     * the Jackson stack.
     *
     * @since 5.0
     */
    public static JacksonService create(ObjectMapper mapper) {
        return new JacksonService(mapper, mapper.getFactory());
    }

    protected JacksonService(ObjectMapper objectMapper, JsonFactory factory) {
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.factory = Objects.requireNonNull(factory);
    }

    @Override
    public JsonFactory getJsonFactory() {
        return factory;
    }

    @Override
    public void outputJson(JsonConvertable processor, OutputStream out) throws IOException {
        // TODO: UTF-8 is hardcoded, it is likely we may have alt. encodings
        try (JsonGenerator generator = factory.createGenerator(out, JsonEncoding.UTF8)) {
            processor.generateJSON(generator);
        }
    }

    /**
     * @since 1.5
     */
    @Override
    public JsonNode parseJson(String json) {
        if (json == null) {
            return null;
        }

        try {
            JsonParser parser = getJsonFactory().createParser(json);
            return objectMapper.readTree(parser);
        } catch (IOException e) {
            throw AgException.badRequest(e, "Error parsing JSON");
        }
    }

    /**
     * @since 1.20
     */
    @Override
    public JsonNode parseJson(InputStream json) {

        try {
            JsonParser parser = getJsonFactory().createParser(json);
            return objectMapper.readTree(parser);
        } catch (IOException e) {
            throw AgException.badRequest(e, "Error parsing JSON");
        }
    }
}
