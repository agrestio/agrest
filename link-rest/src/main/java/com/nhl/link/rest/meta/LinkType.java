package com.nhl.link.rest.meta;

/**
 * @deprecated since 2.10 in favor of {@link com.nhl.link.rest.annotation.LinkType} used by
 * {@link com.nhl.link.rest.annotation.LrResource} annotation.
 */
@Deprecated
public enum LinkType {
    COLLECTION, ITEM, METADATA, UNDEFINED;

    public com.nhl.link.rest.annotation.LinkType toType() {
        switch (this) {
            case COLLECTION:
                return com.nhl.link.rest.annotation.LinkType.COLLECTION;
            case ITEM:
                return com.nhl.link.rest.annotation.LinkType.ITEM;
            case METADATA:
                return com.nhl.link.rest.annotation.LinkType.METADATA;
            case UNDEFINED:
                return com.nhl.link.rest.annotation.LinkType.UNDEFINED;
        }

        throw new RuntimeException("Unexpected");
    }
}
