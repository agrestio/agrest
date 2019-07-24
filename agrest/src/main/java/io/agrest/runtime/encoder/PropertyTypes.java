package io.agrest.runtime.encoder;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Date;

class PropertyTypes {

    static final Class<?> UTIL_DATE = Date.class;
    static final Class<?> SQL_DATE = java.sql.Date.class;
    static final Class<?> SQL_TIME = Time.class;
    static final Class<?> SQL_TIMESTAMP = Timestamp.class;
    static final Class<?> LOCAL_DATE = LocalDate.class;
    static final Class<?> LOCAL_TIME = LocalTime.class;
    static final Class<?> LOCAL_DATETIME = LocalDateTime.class;
    static final Class<?> OFFSET_DATETIME = OffsetDateTime.class;
}
