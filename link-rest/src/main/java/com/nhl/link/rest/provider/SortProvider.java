package com.nhl.link.rest.provider;

import com.nhl.link.rest.runtime.parser.sort.ISortProcessor;
import com.nhl.link.rest.runtime.query.Sort;
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

    private final ISortProcessor sortProcessor;

    public SortProvider(@Inject ISortProcessor sortProcessor) {
        this.sortProcessor = sortProcessor;
    }


    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {

        return (rawType != Sort.class) ? null : (ParamConverter<T>)sortProcessor.getConverter();
    }
}
