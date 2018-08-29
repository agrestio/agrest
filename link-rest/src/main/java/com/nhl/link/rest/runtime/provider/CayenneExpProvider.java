package com.nhl.link.rest.runtime.provider;

import com.nhl.link.rest.protocol.CayenneExp;
import com.nhl.link.rest.runtime.protocol.ICayenneExpParser;
import com.nhl.link.rest.runtime.provider.converter.CayenneExpConverter;
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

    private final CayenneExpConverter cayenneExpConverter;


    public CayenneExpProvider(@Inject ICayenneExpParser cayenneExpParser) {
        this.cayenneExpConverter = new CayenneExpConverter(cayenneExpParser);
    }

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {

        return (rawType == CayenneExp.class) ? (ParamConverter<T>)cayenneExpConverter : null;
    }
}
