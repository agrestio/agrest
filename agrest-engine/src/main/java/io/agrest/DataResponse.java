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

    private final List<? extends T> data;
    private final int total;
    private final Encoder encoder;

    /**
     * @since 5.0
     */
    public static <T> DataResponse<T> of(int status) {
        return of(status, Collections.emptyList(), 0, GenericEncoder.encoder());
    }

    /**
     * @since 5.0
     */
    public static <T> DataResponse<T> of(int status, List<? extends T> data) {
        return of(status, data, data.size(), GenericEncoder.encoder());
    }

    /**
     * @since 5.0
     */
    public static <T> DataResponse<T> of(int status, List<? extends T> data, int total) {
        return of(status, data, total, GenericEncoder.encoder());
    }

    /**
     * @since 5.0
     */
    public static <T> DataResponse<T> of(int status, List<? extends T> data, int total, Encoder encoder) {
        return new DataResponse<>(status, data, total, encoder);
    }

    /**
     * @deprecated since 5.0 in favor of {@link #of(int, List, int)} , and other "of" methods.
     */
    @Deprecated
    public static <T> DataResponse<T> forObject(T object) {
        Objects.requireNonNull(object);
        return of(HttpStatus.OK, Collections.singletonList(object), 1);
    }

    /**
     * @deprecated since 5.0 in favor of {@link #of(int, List, int)}, and other "of" methods.
     */
    @Deprecated
    public static <T> DataResponse<T> forObjects(List<T> data) {
        return of(HttpStatus.OK, data, data.size());
    }

    protected DataResponse(int status, List<? extends T> data, int total, Encoder encoder) {
        super(status);
        this.total = total;
        this.data = data;
        this.encoder = encoder;
    }

    /**
     * Returns root objects of the response.
     *
     * @since 5.0
     */
    public List<? extends T> getData() {
        return data;
    }

    /**
     * @deprecated since 5.0 in favor of {@link #getData()}.
     */
    @Deprecated
    public List<? extends T> getObjects() {
        return data;
    }

    public Encoder getEncoder() {
        return encoder;
    }

    /**
     * @return a total count of objects in the resource, which is greater or equal of the number of objects in this
     * response.
     * @since 5.0
     */
    public int getTotal() {
        return total;
    }
}
