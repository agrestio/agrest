package com.nhl.link.rest.provider;

import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpProcessor;
import com.nhl.link.rest.runtime.query.CayenneExp;
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
public class CayenneExpProvider implements ParamConverterProvider {

    private final IJacksonService jsonParser;
    private final ICayenneExpProcessor expProcessor;


    public CayenneExpProvider(@Inject IJacksonService jsonParser, @Inject ICayenneExpProcessor expProcessor) {
        this.jsonParser = jsonParser;
        this.expProcessor = expProcessor;
    }

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {

        return (rawType != CayenneExp.class) ? null : (ParamConverter<T>)expProcessor.getConverter();
    }
}
