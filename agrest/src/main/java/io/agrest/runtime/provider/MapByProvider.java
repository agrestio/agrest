package io.agrest.runtime.provider;

import io.agrest.protocol.MapBy;
import io.agrest.runtime.protocol.IMapByParser;
import io.agrest.runtime.provider.converter.MapByConverter;
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
public class MapByProvider implements ParamConverterProvider {

    private final MapByConverter mapByConverter;

    public MapByProvider(@Inject IMapByParser mapByParser) {
        this.mapByConverter = new MapByConverter(mapByParser);
    }


    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {

        return (rawType == MapBy.class) ? (ParamConverter<T>)mapByConverter: null;
    }
}
