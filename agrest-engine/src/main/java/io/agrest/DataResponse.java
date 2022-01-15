package io.agrest;

import io.agrest.encoder.Encoder;
import io.agrest.encoder.GenericEncoder;

import java.util.Collections;
import java.util.List;

/**
 * A response object that represents a 'Collection Document' from Agrest protocol.
 */
public class DataResponse<T> extends AgResponse {

    private final Class<T> type;
    private List objects;
    private Encoder encoder;

    public static <T> DataResponse<T> forObject(T object) {

        if (object == null) {
            throw new NullPointerException("Null object");
        }

        return forObjects(Collections.singletonList(object));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> DataResponse<T> forObjects(List<T> objects) {

        if (objects.isEmpty()) {
            return new DataResponse(Object.class);
        } else {
            Class<T> type = (Class<T>) objects.get(0).getClass();
            DataResponse<T> response = new DataResponse<>(type);
            response.setObjects(objects);
            return response;
        }
    }

    public static <T> DataResponse<T> forType(Class<T> type) {
        return new DataResponse<>(type);
    }

    DataResponse(Class<T> type) {
        this.type = type;
        this.encoder = GenericEncoder.encoder();
        this.objects = Collections.emptyList();
    }

    public Class<T> getType() {
        return type;
    }

    /**
     * @since 1.24
     */
    public void setObjects(List<? extends T> objects) {
        this.objects = objects;
    }

    /**
     * @since 1.24
     */
    public void setObject(T object) {
        setObjects(Collections.singletonList(object));
    }

    /**
     * Returns all objects returned from DB.
     */
    public List<T> getObjects() {
        return objects;
    }

    public Encoder getEncoder() {
        return encoder;
    }

    /**
     * @since 1.24
     */
    public void setEncoder(Encoder encoder) {
        this.encoder = encoder;
    }
}
