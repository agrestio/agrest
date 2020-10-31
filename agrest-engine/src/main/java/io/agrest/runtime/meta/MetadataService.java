package io.agrest.runtime.meta;

import io.agrest.AgException;
import io.agrest.compiler.AgEntityCompiler;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgEntity;
import io.agrest.meta.LazyAgDataMap;
import org.apache.cayenne.di.Inject;

import javax.ws.rs.core.Response.Status;
import java.util.List;

public class MetadataService implements IMetadataService {

    private AgDataMap dataMap;

    public MetadataService(@Inject List<AgEntityCompiler> entityCompilers) {
        this.dataMap = new LazyAgDataMap(entityCompilers);
    }

    /**
     * @since 1.12
     */
    @Override
    public <T> AgEntity<T> getAgEntity(Class<T> type) {
        if (type == null) {
            throw new NullPointerException("Null type");
        }

        AgEntity<T> e = dataMap.getEntity(type);

        if (e == null) {
            throw new AgException(Status.BAD_REQUEST, "Invalid entity: " + type.getName());
        }

        return e;
    }
}
