package io.agrest.runtime.encoder;

import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.annotation.AgAttribute;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.Encoders;
import io.agrest.encoder.ISODateTimeEncoder;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgEntity;
import io.agrest.meta.LazyAgDataMap;
import io.agrest.meta.compiler.AgEntityCompiler;
import io.agrest.meta.compiler.PojoEntityCompiler;
import io.agrest.runtime.semantics.RelationshipMapper;
import io.agrest.unit.ResourceEntityUtils;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class EncoderService_UtilDate_Test {

    // using pre-defined moments in time with and without fractional seconds
    private static final long EPOCH_MILLIS = 1458995247000L;
    private static final long EPOCH_MILLIS_WITH_FRACTION = 1458995247001L;

    private EncoderService encoderService;
    private AgEntity<PUtilDate> utilDateEntity;

    @Before
    public void before() {

        this.encoderService = new EncoderService(
                new AttributeEncoderFactory(new ValueEncodersProvider(Collections.emptyMap()).get()),
                mock(IStringConverterFactory.class),
                new RelationshipMapper(),
                Collections.emptyMap());

        AgEntityCompiler compiler = new PojoEntityCompiler(Collections.emptyMap());
        AgDataMap dataMap = new LazyAgDataMap(Collections.singletonList(compiler));
        this.utilDateEntity = dataMap.getEntity(PUtilDate.class);
    }

    /**
     * Since 2.11 we stop distinguishing between different JDBC date types and rely solely on attribute's Java type.
     * Hence, a {@link java.util.Date} attribute is always going to be formatted as ISO 8601 local date-time,
     * regardless of the column's SQL type:
     * {@code yyyy-MM-dd'T'HH:mm:ss[.SSS]}, e.g. 2017-01-01T10:00:00
     * <p>
     * See https://github.com/nhl/link-rest/issues/275
     *
     * @see ISODateTimeEncoder
     */
    @Test
    public void testUtilDate() {
        testUtilDate(new java.util.Date(EPOCH_MILLIS), "yyyy-MM-dd'T'HH:mm:ss");
        testUtilDate(new java.util.Date(EPOCH_MILLIS_WITH_FRACTION), "yyyy-MM-dd'T'HH:mm:ss.SSS");
    }

    private void testUtilDate(java.util.Date date, String expectedPattern) {

        ResourceEntity<PUtilDate> re = new RootResourceEntity<>(utilDateEntity, null);
        ResourceEntityUtils.appendAttribute(re, "date", java.util.Date.class);

        PUtilDate o = new PUtilDate();
        o.setDate(date);

        String dateString = DateTimeFormatter.ofPattern(expectedPattern).format(toLocalDateTime(date));
        assertEquals("{\"data\":[{\"date\":\"" + dateString + "\"}],\"total\":1}", toJson(o, re));
    }

    private LocalDateTime toLocalDateTime(java.util.Date date) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault());
    }

    private String toJson(Object object, ResourceEntity<?> resourceEntity) {
        Encoder encoder = encoderService.dataEncoder(resourceEntity);
        return Encoders.toJson(encoder, Collections.singletonList(object));
    }

    public class PUtilDate {
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
