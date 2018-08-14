package com.nhl.link.rest.runtime.provider;

import com.nhl.link.rest.protocol.Limit;
import com.nhl.link.rest.protocol.Start;
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
public class SizeProvider implements ParamConverterProvider {

    private final SortConverter sortConverter;

    public SizeProvider(@Inject ISortParser sortParser) {
        this.sortConverter = new SortConverter(sortParser);
    }


    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {

        if (rawType == Start.class) {
            return (ParamConverter<T>) new ParamConverter<Start>() {

                    @Override
                    public Start fromString(String value) {
                        return  new Start(Integer.parseInt(value));
                    }

                    @Override
                    public String toString(Start value) {
                        return null;
                    }
                };
        } else if (rawType == Limit.class) {
            return (ParamConverter<T>) new ParamConverter<Limit>() {

                    @Override
                    public Limit fromString(String value) {
                        return  new Limit(Integer.parseInt(value));
                    }

                    @Override
                    public String toString(Limit value) {
                        return null;
                    }
                };
        }

        return null;
    }
}
