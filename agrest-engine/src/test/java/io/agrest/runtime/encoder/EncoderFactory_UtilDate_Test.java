package io.agrest.runtime.encoder;

import io.agrest.DataResponse;
import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.annotation.AgAttribute;
import io.agrest.compiler.AgEntityCompiler;
import io.agrest.compiler.AnnotationsAgEntityCompiler;
import io.agrest.converter.valuestring.GenericConverter;
import io.agrest.converter.valuestring.UtilDateConverter;
import io.agrest.converter.valuestring.ValueStringConverter;
import io.agrest.converter.valuestring.ValueStringConverters;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.ValueEncodersProvider;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgSchema;
import io.agrest.meta.LazySchema;
import io.agrest.processor.ProcessingContext;
import io.agrest.runtime.semantics.RelationshipMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class EncoderFactory_UtilDate_Test {

    // using pre-defined moments in time with and without fractional seconds
    private static final long EPOCH_MILLIS = 1458995247000L;
    private static final long EPOCH_MILLIS_WITH_FRACTION = 1458995247001L;

    private EncoderFactory encoderFactory;
    private AgEntity<PUtilDate> utilDateEntity;

    @BeforeEach
    public void before() {

        Map<Class<?>, ValueStringConverter<?>> converterMap = Map.of(
                java.util.Date.class, UtilDateConverter.converter()
        );

        ValueStringConverters converters = new ValueStringConverters(converterMap, GenericConverter.converter());

        this.encoderFactory = new EncoderFactory(
                new EncodablePropertyFactory(new ValueEncodersProvider(converters, Collections.emptyMap()).get()),
                converters,
                new RelationshipMapper());

        AgEntityCompiler compiler = new AnnotationsAgEntityCompiler(Collections.emptyMap());
        AgSchema schema = new LazySchema(List.of(compiler));
        this.utilDateEntity = schema.getEntity(PUtilDate.class);
    }

    /**
     * Since 2.11 we stop distinguishing between different JDBC date types and rely solely on attribute's Java type.
     * Hence, a {@link java.util.Date} attribute is always going to be formatted as ISO 8601 local date-time,
     * regardless of the column's SQL type:
     * {@code yyyy-MM-dd'T'HH:mm:ss[.SSS]}, e.g. 2017-01-01T10:00:00
     * <p>
     * See https://github.com/nhl/link-rest/issues/275
     */
    @Test
    public void utilDate() {
        testUtilDate(new java.util.Date(EPOCH_MILLIS), "yyyy-MM-dd'T'HH:mm:ss");
        testUtilDate(new java.util.Date(EPOCH_MILLIS_WITH_FRACTION), "yyyy-MM-dd'T'HH:mm:ss.SSS");
    }

    private void testUtilDate(java.util.Date date, String expectedPattern) {

        ResourceEntity<PUtilDate> re = new RootResourceEntity<>(utilDateEntity);
        re.ensureAttribute("date", false);

        PUtilDate o = new PUtilDate();
        o.setDate(date);

        String dateString = DateTimeFormatter.ofPattern(expectedPattern).format(toLocalDateTime(date));
        assertEquals("{\"data\":[{\"date\":\"" + dateString + "\"}],\"total\":1}", toJson(o, re));
    }

    private LocalDateTime toLocalDateTime(java.util.Date date) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault());
    }

    private String toJson(Object object, ResourceEntity<?> resourceEntity) {
        Encoder encoder = encoderFactory.encoder(resourceEntity, mock(ProcessingContext.class));
        return Encoders.toJson(DataResponse.of(200, List.of(object)).encoder(encoder).build());
    }

    public static class PUtilDate {
        private java.util.Date date;

        @AgAttribute
        public java.util.Date getDate() {
            return date;
        }

        public void setDate(java.util.Date date) {
            this.date = date;
        }
    }
}
