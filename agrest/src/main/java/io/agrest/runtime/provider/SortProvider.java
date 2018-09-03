package io.agrest.runtime.provider;

import io.agrest.protocol.Dir;
import io.agrest.protocol.Sort;
import io.agrest.runtime.protocol.ISortParser;
import io.agrest.runtime.provider.converter.SortConverter;
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
public class SortProvider implements ParamConverterProvider {

    private final SortConverter sortConverter;

    public SortProvider(@Inject ISortParser sortParser) {
        this.sortConverter = new SortConverter(sortParser);
    }


    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {

        if (rawType == Sort.class) {
            return (ParamConverter<T>) sortConverter;
        } else if (rawType == Dir.class) {
            return (ParamConverter<T>) new ParamConverter<Dir>() {
                @Override
                public Dir fromString(String value) {
                    return Dir.valueOf(value);
                }

                @Override
                public String toString(Dir value) {
                    return null;
                }
            };
        }

        return null;
    }
}
