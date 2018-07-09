package com.nhl.link.rest.provider;

import com.nhl.link.rest.runtime.parser.tree.IExcludeProcessor;
import com.nhl.link.rest.runtime.query.Exclude;
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
    private final IExcludeProcessor excludeProcessor;

    public ExcludeProvider(@Inject IExcludeProcessor excludeProcessor) {
        this.excludeProcessor = excludeProcessor;
    }

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {

        return (rawType != Exclude.class) ? null : (ParamConverter<T>)excludeProcessor.getConverter();
    }
}
