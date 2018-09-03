package io.agrest.runtime.provider;

import io.agrest.protocol.CayenneExp;
import io.agrest.runtime.protocol.ICayenneExpParser;
import io.agrest.runtime.provider.converter.CayenneExpConverter;
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
