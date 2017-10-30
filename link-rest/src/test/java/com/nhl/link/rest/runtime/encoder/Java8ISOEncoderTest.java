package com.nhl.link.rest.runtime.encoder;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.encoder.EncoderFilter;
import com.nhl.link.rest.encoder.PropertyMetadataEncoder;
import com.nhl.link.rest.it.fixture.cayenne.iso.Java8ISODateTestEntity;
import com.nhl.link.rest.it.fixture.cayenne.iso.Java8ISOTimeTestEntity;
import com.nhl.link.rest.it.fixture.cayenne.iso.Java8ISOTimestampTestEntity;
import com.nhl.link.rest.runtime.jackson.JacksonService;
import com.nhl.link.rest.runtime.semantics.RelationshipMapper;
import com.nhl.link.rest.unit.Java8TestWithCayenneMapping;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class Java8ISOEncoderTest extends Java8TestWithCayenneMapping {

    private EncoderService encoderService;

    @Before
    public void before() {
        IAttributeEncoderFactory attributeEncoderFactory = new AttributeEncoderFactoryProvider(Collections.emptyMap()).get();
        IStringConverterFactory stringConverterFactory = mock(IStringConverterFactory.class);

        encoderService = new EncoderService(Collections.<EncoderFilter>emptyList(), attributeEncoderFactory, stringConverterFactory,
                new RelationshipMapper(), Collections.<String, PropertyMetadataEncoder> emptyMap());
    }

    @Test
    public void testJava8ISODate() throws IOException {
        ResourceEntity<Java8ISODateTestEntity> resourceEntity = getResourceEntity(Java8ISODateTestEntity.class);
        appendPersistenceAttribute(resourceEntity, Java8ISODateTestEntity.DATE, LocalDate.class, Types.DATE);

        LocalDate localDate = LocalDate.now();

        Java8ISODateTestEntity isoDateTestEntity = new Java8ISODateTestEntity();
        isoDateTestEntity.setDate(localDate);

        assertEquals("{\"data\":[{\"date\":\"" + localDate + "\"}],\"total\":1}", toJson(isoDateTestEntity, resourceEntity));
    }

    @Test
    public void testJava8ISOTime() throws IOException {
        ResourceEntity<Java8ISOTimeTestEntity> resourceEntity = getResourceEntity(Java8ISOTimeTestEntity.class);
        appendPersistenceAttribute(resourceEntity, Java8ISOTimeTestEntity.TIME, LocalTime.class, Types.TIME);

        LocalTime localTime = LocalTime.now();

        Java8ISOTimeTestEntity isoTimeTestEntity = new Java8ISOTimeTestEntity();
        isoTimeTestEntity.setTime(localTime);

        assertEquals("{\"data\":[{\"time\":\"" + localTime.truncatedTo(ChronoUnit.SECONDS) + "\"}],\"total\":1}",
                toJson(isoTimeTestEntity, resourceEntity));
    }

    @Test
    public void testJava8ISOTimestamp() throws IOException {
        ResourceEntity<Java8ISOTimestampTestEntity> resourceEntity = getResourceEntity(Java8ISOTimestampTestEntity.class);
        appendPersistenceAttribute(resourceEntity, Java8ISOTimestampTestEntity.TIMESTAMP, LocalDateTime.class, Types.TIMESTAMP);

        LocalDateTime localDateTime = LocalDateTime.now();

        Java8ISOTimestampTestEntity isoTimestampTestEntity = new Java8ISOTimestampTestEntity();
        isoTimestampTestEntity.setTimestamp(localDateTime);

        assertEquals("{\"data\":[{\"timestamp\":\"" + localDateTime.truncatedTo(ChronoUnit.SECONDS) + "\"}],\"total\":1}",
                toJson(isoTimestampTestEntity, resourceEntity));
    }

    private String toJson(Object object, ResourceEntity<?> resourceEntity) throws IOException {

        Encoder encoder = encoderService.dataEncoder(resourceEntity);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (JsonGenerator generator = new JacksonService().getJsonFactory().createGenerator(out, JsonEncoding.UTF8)) {
            encoder.encode(null, Collections.singletonList(object), generator);
        }

        return new String(out.toByteArray(), "UTF-8");
    }

}
