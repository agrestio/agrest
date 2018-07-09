package com.nhl.link.rest.provider;

import com.nhl.link.rest.runtime.parser.tree.IIncludeProcessor;
import com.nhl.link.rest.runtime.query.Include;
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

    private final IIncludeProcessor includeProcessor;

    public IncludeProvider(@Inject IIncludeProcessor includeProcessor) {
        this.includeProcessor = includeProcessor;
    }


    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {

        return (rawType != Include.class) ? null : (ParamConverter<T>)includeProcessor.getConverter();
    }
}
