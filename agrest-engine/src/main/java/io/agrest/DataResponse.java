package io.agrest;

import io.agrest.encoder.Encoder;
import io.agrest.encoder.GenericEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A response object that represents a 'Collection Document' from Agrest protocol.
 */
public class DataResponse<T> extends AgResponse {

    private final List<? extends T> objects;
    private final Encoder encoder;

    /**
     * @since 5.0
     */
    public static <T> DataResponse<T> of(int status) {
        return of(status, Collections.emptyList(), GenericEncoder.encoder());
    }

    /**
     * @since 5.0
     */
    public static <T> DataResponse<T> of(int status, List<? extends T> objects) {
        return of(status, objects, GenericEncoder.encoder());
    }

    /**
     * @since 5.0
     */
    public static <T> DataResponse<T> of(int status, List<? extends T> objects, Encoder encoder) {
        return new DataResponse<>(status, objects, encoder);
    }

    /**
     * @deprecated since 5.0 in favor of {@link #of(int, List)}, and other "of" methods.
     */
    @Deprecated
    public static <T> DataResponse<T> forObject(T object) {
        Objects.requireNonNull(object);
        return of(HttpStatus.OK, Collections.singletonList(object));
    }

    /**
     * @deprecated since 5.0 in favor of {@link #of(int, List)}, and other "of" methods.
     */
    @Deprecated
    public static <T> DataResponse<T> forObjects(List<T> objects) {
        return of(HttpStatus.OK, objects);
    }

    protected DataResponse(int status, List<? extends T> objects, Encoder encoder) {
        super(status);
        this.objects = objects;
        this.encoder = encoder;
    }

    /**
     * Returns all objects returned from DB.
     */
    public List<? extends T> getObjects() {
        return objects;
    }

    public Encoder getEncoder() {
        return encoder;
    }
}
