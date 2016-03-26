package com.nhl.link.rest.runtime.encoder;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.encoder.EncoderFilter;
import com.nhl.link.rest.encoder.PropertyMetadataEncoder;
import com.nhl.link.rest.it.fixture.cayenne.iso.ISODateTestEntity;
import com.nhl.link.rest.it.fixture.cayenne.iso.ISOTimeTestEntity;
import com.nhl.link.rest.it.fixture.cayenne.iso.ISOTimestampTestEntity;
import com.nhl.link.rest.runtime.jackson.JacksonService;
import com.nhl.link.rest.runtime.semantics.RelationshipMapper;
import com.nhl.link.rest.unit.TestWithCayenneMapping;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Types;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class ISOEncoderTest extends TestWithCayenneMapping {

    private EncoderService encoderService;

    @Before
    public void before() {
        IAttributeEncoderFactory attributeEncoderFactory = new AttributeEncoderFactory();
        IStringConverterFactory stringConverterFactory = mock(IStringConverterFactory.class);

        encoderService = new EncoderService(Collections.<EncoderFilter>emptyList(), attributeEncoderFactory, stringConverterFactory,
                new RelationshipMapper(), Collections.<String, PropertyMetadataEncoder>emptyMap());
    }

    @Test
    public void testISODate() throws IOException {
        ResourceEntity<ISODateTestEntity> resourceEntity = getResourceEntity(ISODateTestEntity.class);
        appendPersistenceAttribute(resourceEntity, ISODateTestEntity.UTIL_DATE, java.util.Date.class, Types.DATE);
        appendPersistenceAttribute(resourceEntity, ISODateTestEntity.SQL_DATE, java.sql.Date.class, Types.DATE);

        java.sql.Date date = new java.sql.Date(System.currentTimeMillis());

        ISODateTestEntity isoDateTestEntity = new ISODateTestEntity();
        isoDateTestEntity.setUtilDate(date);
        isoDateTestEntity.setSqlDate(date);

        assertEquals("[{\"sqlDate\":\"" + date + "\",\"utilDate\":\"" + date + "\"}]", toJson(isoDateTestEntity, resourceEntity));
    }

    @Test
    public void testISOTime() throws IOException {
        ResourceEntity<ISOTimeTestEntity> resourceEntity = getResourceEntity(ISOTimeTestEntity.class);
        appendPersistenceAttribute(resourceEntity, ISOTimeTestEntity.TIME, java.sql.Time.class, Types.TIME);

        java.sql.Time time = new java.sql.Time(System.currentTimeMillis());

        ISOTimeTestEntity isoTimeTestEntity = new ISOTimeTestEntity();
        isoTimeTestEntity.setTime(time);

        assertEquals("[{\"time\":\"" + time + "\"}]", toJson(isoTimeTestEntity, resourceEntity));
    }

    @Test
    public void testISOTimestamp() throws IOException {
        ResourceEntity<ISOTimestampTestEntity> resourceEntity = getResourceEntity(ISOTimestampTestEntity.class);
        appendPersistenceAttribute(resourceEntity, ISOTimestampTestEntity.TIMESTAMP, java.sql.Timestamp.class, Types.TIMESTAMP);

        java.sql.Timestamp timestamp = new java.sql.Timestamp(1458995247000l);

        ISOTimestampTestEntity isoTimestampTestEntity = new ISOTimestampTestEntity();
        isoTimestampTestEntity.setTimestamp(timestamp);

        assertEquals("[{\"timestamp\":\"2016-03-26T12:27:27Z\"}]", toJson(isoTimestampTestEntity, resourceEntity));
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
