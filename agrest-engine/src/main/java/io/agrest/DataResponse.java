package io.agrest;

import io.agrest.encoder.DataResponseEncoder;
import io.agrest.encoder.Encoder;
import io.agrest.protocol.CollectionResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A response object that represents a 'Collection Document' from Agrest protocol.
 */
public class DataResponse<T> extends AgResponse implements CollectionResponse<T> {

    private final List<? extends T> data;
    private final int total;
    private final Encoder encoder;

    /**
     * @since 5.0
     */
    public static <T> Builder<T> of(int status) {
        return of(status, Collections.emptyList());
    }

    /**
     * @since 5.0
     */
    public static <T> Builder<T> of(int status, List<? extends T> data) {
        return new Builder<>(status, data);
    }

    /**
     * @deprecated in favor of the new builder created via {@link #of(int, List)}
     */
    @Deprecated(since = "5.0")
    public static <T> DataResponse<T> forObject(T object) {
        Objects.requireNonNull(object);
        return of(HttpStatus.OK, List.of(object)).build();
    }

    /**
     * @deprecated in favor of the new builder created via {@link #of(int, List)}
     */
    @Deprecated(since = "5.0")
    public static <T> DataResponse<T> forObjects(List<T> data) {
        return of(HttpStatus.OK, data).build();
    }

    protected DataResponse(
            int status,
            Map<String, List<Object>> headers,
            List<? extends T> data,
            int total,
            Encoder encoder) {

        super(status, headers);
        this.total = total;
        this.data = data;
        this.encoder = encoder;
    }

    /**
     * Returns root objects of the response.
     *
     * @since 5.0
     */
    @Override
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
    @Override
    public int getTotal() {
        return total;
    }

    public static class Builder<T> {

        private List<? extends T> data;
        private Integer status;
        private Integer total;
        private Encoder encoder;
        private Map<String, List<Object>> headers;

        private Builder(int status, List<? extends T> data) {
            this.status = status;
            this.data = data;
        }

        /**
         * @since 5.0
         */
        public Builder headers(Map<String, List<Object>> headers) {
            this.headers = headers;
            return this;
        }

        @Deprecated(since = "5.0")
        public Builder<T> data(List<? extends T> data) {
            this.data = data;
            return this;
        }

        @Deprecated(since = "5.0")
        public Builder<T> status(int status) {
            this.status = status;
            return this;
        }

        public Builder<T> total(int total) {
            this.total = total;
            return this;
        }

        public Builder<T> elementEncoder(Encoder elementEncoder) {
            return encoder(DataResponseEncoder.withElementEncoder(elementEncoder));
        }

        public Builder<T> encoder(Encoder encoder) {
            this.encoder = encoder;
            return this;
        }

        public DataResponse<T> build() {

            List<? extends T> data = this.data != null ? this.data : Collections.emptyList();

            return new DataResponse<>(
                    status != null ? status : HttpStatus.OK,
                    headers != null ? headers : Collections.emptyMap(),
                    data,
                    total != null ? total : data.size(),
                    encoder != null ? encoder : DataResponseEncoder.defaultEncoder()
            );
        }
    }
}
