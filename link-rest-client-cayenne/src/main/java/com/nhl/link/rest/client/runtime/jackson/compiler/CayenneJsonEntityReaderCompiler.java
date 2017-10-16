package com.nhl.link.rest.client.runtime.jackson.compiler;

import com.nhl.link.rest.client.runtime.jackson.CayenneJsonEntityReader;
import com.nhl.link.rest.client.runtime.jackson.IJsonEntityReader;
import com.nhl.link.rest.runtime.parser.converter.IJsonValueConverterFactory;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.di.Inject;

public class CayenneJsonEntityReaderCompiler implements JsonEntityReaderCompiler {

    private IJsonValueConverterFactory converterFactory;

    public CayenneJsonEntityReaderCompiler(@Inject IJsonValueConverterFactory converterFactory) {
        this.converterFactory = converterFactory;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> IJsonEntityReader<T> compile(Class<T> type) {
        if (DataObject.class.isAssignableFrom(type)) {
            return (IJsonEntityReader<T>) compileDataObjectReader((Class<? extends DataObject>)type);
        }
        return null;
    }

    private IJsonEntityReader<?> compileDataObjectReader(Class<? extends DataObject> persistentType) {
        return new CayenneJsonEntityReader<>(persistentType, converterFactory);
    }
}
