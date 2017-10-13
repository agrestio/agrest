package com.nhl.link.rest.client.runtime.jackson.compiler;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.client.LinkRestClientException;
import com.nhl.link.rest.client.runtime.jackson.IJsonEntityReader;
import com.nhl.link.rest.client.runtime.jackson.JsonEntityReader;
import org.apache.cayenne.DataObject;

import java.util.Map;

public class CayenneJsonEntityReaderCompiler implements JsonEntityReaderCompiler {

    // TODO: move JsonValueConverter to link-rest-base module or smth
//    private Map<Class<? extends DataObject>, Map<String, JsonValueConverter>>

    @SuppressWarnings("unchecked")
    @Override
    public <T> IJsonEntityReader<T> compile(Class<T> type) {
        if (DataObject.class.isAssignableFrom(type)) {
            return (IJsonEntityReader<T>) compileDataObjectReader((Class<? extends DataObject>)type);
        }
        return null;
    }

    private IJsonEntityReader<?> compileDataObjectReader(Class<? extends DataObject> persistentType) {
        return new IJsonEntityReader<DataObject>() {
            @Override
            public DataObject readEntity(JsonNode node) {
                DataObject object = newInstance(persistentType);

                node.fields().forEachRemaining(e -> {
                    String propertyName = e.getKey();
                    JsonNode value = e.getValue();

                    if (value.isNull() || value.isMissingNode()) {
                        return;
                    }

                    if (value.isArray() || value.isObject() || value.isPojo()) {
                        return;
                    }

                    // TODO: use proper JsonValueConverter based on getter method's return type
                    // TODO: skip Json attributes, for which there does not exist a getter method
                    object.writePropertyDirectly(propertyName, value.asText());
                });

                return object;
            }
        };
    }

    private static <T> T newInstance(Class<T> type) {
        try {
            return type.newInstance();
        } catch (Exception e) {
            throw new LinkRestClientException("Failed to instantiate type: " + type.getName(), e);
        }
    }
}
