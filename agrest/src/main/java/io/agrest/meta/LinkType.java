package io.agrest.meta;

import io.agrest.annotation.AgResource;

/**
 * @deprecated since 2.10 in favor of {@link io.agrest.annotation.LinkType} used by
 * {@link AgResource} annotation.
 */
@Deprecated
public enum LinkType {
    COLLECTION, ITEM, METADATA, UNDEFINED;

    public io.agrest.annotation.LinkType toType() {
        switch (this) {
            case COLLECTION:
                return io.agrest.annotation.LinkType.COLLECTION;
            case ITEM:
                return io.agrest.annotation.LinkType.ITEM;
            case METADATA:
                return io.agrest.annotation.LinkType.METADATA;
            case UNDEFINED:
                return io.agrest.annotation.LinkType.UNDEFINED;
        }

        throw new RuntimeException("Unexpected");
    }
}
