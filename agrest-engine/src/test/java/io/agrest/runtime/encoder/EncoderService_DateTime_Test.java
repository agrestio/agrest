package io.agrest.runtime.encoder;

import io.agrest.DataResponse;
import io.agrest.HttpStatus;
import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.annotation.AgAttribute;
import io.agrest.compiler.AgEntityCompiler;
import io.agrest.compiler.AnnotationsAgEntityCompiler;
import io.agrest.converter.valuestring.GenericConverter;
import io.agrest.converter.valuestring.ISOLocalDateConverter;
import io.agrest.converter.valuestring.ISOLocalDateTimeConverter;
import io.agrest.converter.valuestring.ISOLocalTimeConverter;
import io.agrest.converter.valuestring.ISOOffsetDateTimeConverter;
import io.agrest.converter.valuestring.ValueStringConverter;
import io.agrest.converter.valuestring.ValueStringConverters;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.ValueEncodersProvider;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgEntity;
import io.agrest.meta.LazyAgDataMap;
import io.agrest.processor.ProcessingContext;
import io.agrest.runtime.semantics.RelationshipMapper;
import io.agrest.junit.ResourceEntityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class EncoderService_DateTime_Test {

    private AgEntity<PDate> dateEntity;
    private AgEntity<PTime> timeEntity;
    private AgEntity<PDateTime> dateTimeEntity;
    private AgEntity<POffsetDateTime> offsetDateTimeEntity;

    private EncoderService encoderService;

    @BeforeEach
    public void before() {

        Map<Class<?>, ValueStringConverter> converterMap = Map.of(
                LocalDate.class, ISOLocalDateConverter.converter(),
                LocalTime.class, ISOLocalTimeConverter.converter(),
                LocalDateTime.class, ISOLocalDateTimeConverter.converter(),
                OffsetDateTime.class, ISOOffsetDateTimeConverter.converter()
        );

        ValueStringConverters converters = new ValueStringConverters(converterMap, GenericConverter.converter());

        this.encoderService = new EncoderService(
                new EncodablePropertyFactory(new ValueEncodersProvider(converters, Collections.emptyMap()).get()),
                converters,
                new RelationshipMapper());

        AgEntityCompiler compiler = new AnnotationsAgEntityCompiler(Collections.emptyMap());
        AgDataMap dataMap = new LazyAgDataMap(Collections.singletonList(compiler));
        this.dateEntity = dataMap.getEntity(PDate.class);
        this.timeEntity = dataMap.getEntity(PTime.class);
        this.dateTimeEntity = dataMap.getEntity(PDateTime.class);
        this.offsetDateTimeEntity = dataMap.getEntity(POffsetDateTime.class);
    }

    @Test
    public void testLocalDate() {
        ResourceEntity<PDate> re = new RootResourceEntity<>(dateEntity);
        ResourceEntityUtils.appendAttribute(re, "date", LocalDate.class, PDate::getDate);

        LocalDate localDate = LocalDate.now();

        PDate o = new PDate();
        o.setDate(localDate);

        String dateString = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(localDate);
        assertEquals("{\"data\":[{\"date\":\"" + dateString + "\"}],\"total\":1}", toJson(o, re));
    }

    @Test
    public void testLocalTime() {
        // fractional part is not printed, when less than a millisecond
        testLocalTime(LocalTime.of(10, 0, 0), "HH:mm:ss");
        testLocalTime(LocalTime.of(10, 0, 0, 1), "HH:mm:ss");
        testLocalTime(LocalTime.of(10, 0, 0, 999_999), "HH:mm:ss");
        testLocalTime(LocalTime.of(10, 0, 0, 1_000_000), "HH:mm:ss.SSS"); // millisecond is 10^6 nanoseconds
    }

    private void testLocalTime(LocalTime time, String expectedPattern) {

        ResourceEntity<PTime> re = new RootResourceEntity<>(timeEntity);
        ResourceEntityUtils.appendAttribute(re, "time", LocalTime.class, PTime::getTime);

        PTime o = new PTime();
        o.setTime(time);

        String timeString = DateTimeFormatter.ofPattern(expectedPattern).format(time);
        assertEquals("{\"data\":[{\"time\":\"" + timeString + "\"}],\"total\":1}", toJson(o, re));
    }

    @Test
    public void testLocalDateTime() {
        // fractional part is not printed, when less than a millisecond
        testLocalDateTime(LocalDateTime.of(2017, 1, 1, 10, 0, 0), "yyyy-MM-dd'T'HH:mm:ss");
        testLocalDateTime(LocalDateTime.of(2017, 1, 1, 10, 0, 0, 1), "yyyy-MM-dd'T'HH:mm:ss");
        testLocalDateTime(LocalDateTime.of(2017, 1, 1, 10, 0, 0, 999_999), "yyyy-MM-dd'T'HH:mm:ss");
        testLocalDateTime(LocalDateTime.of(2017, 1, 1, 10, 0, 0, 1_000_000), "yyyy-MM-dd'T'HH:mm:ss.SSS"); // millisecond is 10^6 nanoseconds
    }

    private void testLocalDateTime(LocalDateTime dateTime, String expectedPattern) {

        ResourceEntity<PDateTime> re = new RootResourceEntity<>(dateTimeEntity);
        ResourceEntityUtils.appendAttribute(re, "timestamp", LocalDateTime.class, PDateTime::getTimestamp);

        PDateTime o = new PDateTime();
        o.setTimestamp(dateTime);

        String dateTimeString = DateTimeFormatter.ofPattern(expectedPattern).format(dateTime);
        assertEquals("{\"data\":[{\"timestamp\":\"" + dateTimeString + "\"}],\"total\":1}", toJson(o, re));
    }

    @Test
    public void testOffsetDateTime() {
        // fractional part is not printed, when less than a millisecond
        testOffsetDateTime(OffsetDateTime.of(LocalDateTime.of(2017, 1, 1, 10, 0, 0), ZoneOffset.ofHours(3)));
        testOffsetDateTime(OffsetDateTime.of(LocalDateTime.of(2017, 1, 1, 10, 0, 0, 1), ZoneOffset.ofHours(3)));
        testOffsetDateTime(OffsetDateTime.of(LocalDateTime.of(2017, 1, 1, 10, 0, 0, 999_999), ZoneOffset.ofHours(3)));
        // millisecond is 10^6 nanoseconds
        testOffsetDateTime(OffsetDateTime.of(LocalDateTime.of(2017, 1, 1, 10, 0, 0, 1_000_000), ZoneOffset.ofHours(3)));
    }

    private void testOffsetDateTime(OffsetDateTime dateTime) {

        ResourceEntity<POffsetDateTime> re = new RootResourceEntity<>(offsetDateTimeEntity);
        ResourceEntityUtils.appendAttribute(re, "timestamp", OffsetDateTime.class, POffsetDateTime::getTimestamp);

        POffsetDateTime o = new POffsetDateTime();
        o.setTimestamp(dateTime);

        String dateTimeString = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dateTime);
        assertEquals("{\"data\":[{\"timestamp\":\"" + dateTimeString + "\"}],\"total\":1}", toJson(o, re));
    }

    private String toJson(Object object, ResourceEntity<?> resourceEntity) {
        Encoder encoder = encoderService.dataEncoder(resourceEntity, mock(ProcessingContext.class));
        return Encoders.toJson(encoder, DataResponse.of(HttpStatus.OK, Collections.singletonList(object)));
    }

    public class PDate {

        private LocalDate date;

        @AgAttribute
        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }
    }

    public class PTime {

        private LocalTime time;

        @AgAttribute
        public LocalTime getTime() {
            return time;
        }

        public void setTime(LocalTime time) {
            this.time = time;
        }
    }

    public class PDateTime {

        private LocalDateTime timestamp;

        @AgAttribute
        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }

    public class POffsetDateTime {

        private OffsetDateTime timestamp;

        @AgAttribute
        public OffsetDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(OffsetDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }

}