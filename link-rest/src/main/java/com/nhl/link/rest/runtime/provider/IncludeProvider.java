package com.nhl.link.rest.runtime.provider;

import com.nhl.link.rest.protocol.Include;
import com.nhl.link.rest.runtime.protocol.IIncludeParser;
import com.nhl.link.rest.runtime.provider.converter.IncludeConverter;
import org.apache.cayenne.di.Inject;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @since 2.13
 */
@Provider
public class IncludeProvider implements ParamConverterProvider {

    private final IncludeConverter includeConverter;

    public IncludeProvider(@Inject IIncludeParser includeParser) {
        this.includeConverter = new IncludeConverter(includeParser);
    }


    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {

        return (rawType == Include.class) ? (ParamConverter<T>)includeConverter : null;
    }
}
