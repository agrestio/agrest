package io.agrest.runtime.encoder;

import io.agrest.DataResponse;
import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.annotation.AgAttribute;
import io.agrest.compiler.AgEntityCompiler;
import io.agrest.compiler.AnnotationsAgEntityCompiler;
import io.agrest.converter.valuestring.GenericConverter;
import io.agrest.converter.valuestring.LocalDateConverter;
import io.agrest.converter.valuestring.LocalDateTimeConverter;
import io.agrest.converter.valuestring.LocalTimeConverter;
import io.agrest.converter.valuestring.OffsetDateTimeConverter;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class EncoderFactory_DateTime_Test {

    private AgEntity<PDate> dateEntity;
    private AgEntity<PTime> timeEntity;
    private AgEntity<PDateTime> dateTimeEntity;
    private AgEntity<POffsetDateTime> offsetDateTimeEntity;

    private EncoderFactory encoderFactory;

    @BeforeEach
    public void before() {

        Map<Class<?>, ValueStringConverter<?>> converterMap = Map.of(
                LocalDate.class, LocalDateConverter.converter(),
                LocalTime.class, LocalTimeConverter.converter(),
                LocalDateTime.class, LocalDateTimeConverter.converter(),
                OffsetDateTime.class, OffsetDateTimeConverter.converter()
        );

        ValueStringConverters converters = new ValueStringConverters(converterMap, GenericConverter.converter());

        this.encoderFactory = new EncoderFactory(
                new EncodablePropertyFactory(new ValueEncodersProvider(converters, Map.of()).get()),
                converters,
                new RelationshipMapper());

        AgEntityCompiler compiler = new AnnotationsAgEntityCompiler(Map.of());
        AgSchema schema = new LazySchema(List.of(compiler));
        this.dateEntity = schema.getEntity(PDate.class);
        this.timeEntity = schema.getEntity(PTime.class);
        this.dateTimeEntity = schema.getEntity(PDateTime.class);
        this.offsetDateTimeEntity = schema.getEntity(POffsetDateTime.class);
    }

    @Test
    public void localDate() {
        ResourceEntity<PDate> re = new RootResourceEntity<>(dateEntity);
        re.ensureAttribute("date", false);

        LocalDate localDate = LocalDate.now();

        PDate o = new PDate();
        o.setDate(localDate);

        String dateString = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(localDate);
        assertEquals("{\"data\":[{\"date\":\"" + dateString + "\"}],\"total\":1}", toJson(o, re));
    }

    @Test
    public void localTime() {
        // fractional part is not printed, when less than a millisecond
        testLocalTime(LocalTime.of(10, 0, 0), "HH:mm:ss");
        testLocalTime(LocalTime.of(10, 0, 0, 1), "HH:mm:ss.nnnnnnnnn");
        testLocalTime(LocalTime.of(10, 0, 0, 999_999), "HH:mm:ss.nnnnnnnnn");
        testLocalTime(LocalTime.of(10, 0, 0, 1_000_000), "HH:mm:ss.SSS"); // millisecond is 10^6 nanoseconds
    }

    private void testLocalTime(LocalTime time, String expectedPattern) {

        ResourceEntity<PTime> re = new RootResourceEntity<>(timeEntity);
        re.ensureAttribute("time", false);

        PTime o = new PTime();
        o.setTime(time);

        String timeString = DateTimeFormatter.ofPattern(expectedPattern).format(time);
        assertEquals("{\"data\":[{\"time\":\"" + timeString + "\"}],\"total\":1}", toJson(o, re));
    }

    @Test
    public void localDateTime() {
        // fractional part is not printed, when less than a millisecond
        testLocalDateTime(LocalDateTime.of(2017, 1, 1, 10, 0, 0), "yyyy-MM-dd'T'HH:mm:ss");
        testLocalDateTime(LocalDateTime.of(2017, 1, 1, 10, 0, 0, 1), "yyyy-MM-dd'T'HH:mm:ss.nnnnnnnnn");
        testLocalDateTime(LocalDateTime.of(2017, 1, 1, 10, 0, 0, 999_999), "yyyy-MM-dd'T'HH:mm:ss.nnnnnnnnn");
        testLocalDateTime(LocalDateTime.of(2017, 1, 1, 10, 0, 0, 1_000_000), "yyyy-MM-dd'T'HH:mm:ss.SSS");
    }

    private void testLocalDateTime(LocalDateTime dateTime, String expectedPattern) {

        ResourceEntity<PDateTime> re = new RootResourceEntity<>(dateTimeEntity);
        re.ensureAttribute("timestamp", false);

        PDateTime o = new PDateTime();
        o.setTimestamp(dateTime);

        String dateTimeString = DateTimeFormatter.ofPattern(expectedPattern).format(dateTime);
        assertEquals("{\"data\":[{\"timestamp\":\"" + dateTimeString + "\"}],\"total\":1}", toJson(o, re));
    }

    @Test
    public void offsetDateTime() {
        // fractional part is not printed, when less than a millisecond
        testOffsetDateTime(OffsetDateTime.of(LocalDateTime.of(2017, 1, 1, 10, 0, 0), ZoneOffset.ofHours(3)));
        testOffsetDateTime(OffsetDateTime.of(LocalDateTime.of(2017, 1, 1, 10, 0, 0, 1), ZoneOffset.ofHours(3)));
        testOffsetDateTime(OffsetDateTime.of(LocalDateTime.of(2017, 1, 1, 10, 0, 0, 999_999), ZoneOffset.ofHours(3)));
        // millisecond is 10^6 nanoseconds
        testOffsetDateTime(OffsetDateTime.of(LocalDateTime.of(2017, 1, 1, 10, 0, 0, 1_000_000), ZoneOffset.ofHours(3)));
    }

    private void testOffsetDateTime(OffsetDateTime dateTime) {

        ResourceEntity<POffsetDateTime> re = new RootResourceEntity<>(offsetDateTimeEntity);
        re.ensureAttribute("timestamp", false);

        POffsetDateTime o = new POffsetDateTime();
        o.setTimestamp(dateTime);

        String dateTimeString = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dateTime);
        assertEquals("{\"data\":[{\"timestamp\":\"" + dateTimeString + "\"}],\"total\":1}", toJson(o, re));
    }

    private String toJson(Object object, ResourceEntity<?> resourceEntity) {
        Encoder encoder = encoderFactory.encoder(resourceEntity, mock(ProcessingContext.class));
        return Encoders.toJson(DataResponse.of(200, List.of(object)).encoder(encoder).build());
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