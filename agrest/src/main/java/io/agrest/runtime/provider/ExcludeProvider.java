package io.agrest.runtime.provider;

import io.agrest.protocol.Exclude;
import io.agrest.runtime.protocol.IExcludeParser;
import io.agrest.runtime.provider.converter.ExcludeConverter;
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
public class ExcludeProvider implements ParamConverterProvider {
    private final ExcludeConverter excludeConverter;

    public ExcludeProvider(@Inject IExcludeParser excludeParser) {
        this.excludeConverter = new ExcludeConverter(excludeParser);
    }

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {

        return (rawType == Exclude.class) ? (ParamConverter<T>)excludeConverter : null;
    }
}
