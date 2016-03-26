package com.nhl.link.rest.runtime.encoder;

import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.encoder.ISOLocalDateEncoder;
import com.nhl.link.rest.encoder.ISOLocalDateTimeEncoder;
import com.nhl.link.rest.encoder.ISOLocalTimeEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Java8AttributeEncoderFactory extends AttributeEncoderFactory {

    static final String LOCAL_DATE = LocalDate.class.getName();
    static final String LOCAL_TIME = LocalTime.class.getName();
    static final String LOCAL_DATETIME = LocalDateTime.class.getName();

    @Override
    protected Encoder buildEncoder(String javaType, int jdbcType) {

        if (LOCAL_DATE.equals(javaType)) {
            return ISOLocalDateEncoder.encoder();
        } else if (LOCAL_TIME.equals(javaType)) {
            return ISOLocalTimeEncoder.encoder();
        } else if (LOCAL_DATETIME.equals(javaType)) {
            return ISOLocalDateTimeEncoder.encoder();
        }

        return super.buildEncoder(javaType, jdbcType);
    }
}
