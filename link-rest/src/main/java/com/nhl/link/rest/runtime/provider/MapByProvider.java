package com.nhl.link.rest.runtime.provider;

import com.nhl.link.rest.protocol.MapBy;
import com.nhl.link.rest.runtime.protocol.IMapByParser;
import com.nhl.link.rest.runtime.provider.converter.MapByConverter;
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
