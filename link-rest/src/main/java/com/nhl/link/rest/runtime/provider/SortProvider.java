package com.nhl.link.rest.runtime.provider;

import com.nhl.link.rest.protocol.Sort;
import com.nhl.link.rest.runtime.protocol.ISortParser;
import com.nhl.link.rest.runtime.provider.converter.SortConverter;
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

        return (rawType == Sort.class) ? (ParamConverter<T>)sortConverter: null;
    }
}
