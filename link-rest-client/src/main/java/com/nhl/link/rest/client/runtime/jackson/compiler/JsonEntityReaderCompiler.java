package com.nhl.link.rest.client.runtime.jackson.compiler;

import com.nhl.link.rest.client.runtime.jackson.IJsonEntityReader;

public interface JsonEntityReaderCompiler {

    <T> IJsonEntityReader<T> compile(Class<T> type);
}
