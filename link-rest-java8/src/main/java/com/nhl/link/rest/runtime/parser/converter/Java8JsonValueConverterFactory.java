package com.nhl.link.rest.runtime.parser.converter;

import com.nhl.link.rest.parser.converter.ISOLocalDateConverter;
import com.nhl.link.rest.parser.converter.ISOLocalDateTimeConverter;
import com.nhl.link.rest.parser.converter.ISOLocalTimeConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Java8JsonValueConverterFactory extends DefaultJsonValueConverterFactory {

    public Java8JsonValueConverterFactory() {
        super();

        convertersByJavaTypeName.put(LocalDate.class.getName(), ISOLocalDateConverter.converter());
        convertersByJavaTypeName.put(LocalTime.class.getName(), ISOLocalTimeConverter.converter());
        convertersByJavaTypeName.put(LocalDateTime.class.getName(), ISOLocalDateTimeConverter.converter());

        convertersByJavaType.put(LocalDate.class, ISOLocalDateConverter.converter());
        convertersByJavaType.put(LocalTime.class, ISOLocalTimeConverter.converter());
        convertersByJavaType.put(LocalDateTime.class, ISOLocalDateTimeConverter.converter());
    }
}
