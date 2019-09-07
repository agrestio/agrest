package io.agrest.meta.cayenne;

import io.agrest.meta.AgAttribute;
import org.apache.cayenne.map.DbAttribute;

public interface CayenneAgAttribute extends AgAttribute {

    DbAttribute getDbAttribute();
}
