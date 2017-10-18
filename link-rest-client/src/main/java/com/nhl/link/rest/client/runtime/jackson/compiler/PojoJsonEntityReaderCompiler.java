package com.nhl.link.rest.client.runtime.jackson.compiler;

import com.nhl.link.rest.client.runtime.jackson.IJsonEntityReader;
import com.nhl.link.rest.parser.converter.JsonValueConverter;
import com.nhl.link.rest.runtime.parser.converter.IJsonValueConverterFactory;

public class PojoJsonEntityReaderCompiler implements JsonEntityReaderCompiler {

    private IJsonValueConverterFactory converterFactory;

    public PojoJsonEntityReaderCompiler(IJsonValueConverterFactory converterFactory) {
        this.converterFactory = converterFactory;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> IJsonEntityReader<T> compile(Class<T> type) {
        JsonValueConverter<?> converter = converterFactory.converter(type);
        return node -> (T) converter.value(node);
    }
}
