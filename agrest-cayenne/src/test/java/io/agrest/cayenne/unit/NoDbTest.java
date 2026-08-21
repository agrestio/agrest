package io.agrest.cayenne.unit;

import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.ToManyResourceEntity;
import io.agrest.ToOneResourceEntity;
import io.agrest.cayenne.compiler.CayenneAgEntityCompiler;
import io.agrest.cayenne.exp.CayenneExpParser;
import io.agrest.cayenne.exp.CayenneExpPostProcessor;
import io.agrest.cayenne.path.IPathResolver;
import io.agrest.cayenne.path.PathResolver;
import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.cayenne.processor.CayenneQueryAssembler;
import io.agrest.compiler.AgEntityCompiler;
import io.agrest.compiler.AnnotationsAgEntityCompiler;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgSchema;
import io.agrest.meta.LazySchema;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.DataNodeDescriptor;
import org.apache.cayenne.dba.JdbcAdapter;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.runtime.CayenneRuntime;
import org.junit.jupiter.api.BeforeEach;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A superclass of Cayenne-aware test cases that do not need to access the DB, but need to work with EntityResolver
 * and higher levels of the stack.
 */
public abstract class NoDbTest {

    protected ICayennePersister mockCayennePersister;
    protected IPathResolver pathDescriptorManager;
    protected AgSchema schema;
    protected CayenneQueryAssembler queryAssembler;

    protected static CayenneRuntime createRuntime(String project) {
        // no DB access is needed here, so use a mock DataSource with an explicit adapter, bypassing
        // adapter auto-detection that would try to open a connection
        DataNodeDescriptor node = DataNodeDescriptor.of("test")
                .dataSource(mock(DataSource.class))
                .adapter(JdbcAdapter.class)
                .build();

        return CayenneRuntime
                .builder()
                .addConfig(project)
                .defaultDataNode(node)
                .build();
    }

    protected abstract CayenneRuntime getRuntime();

    @BeforeEach
    public void initAgSchema() {

        ObjectContext sharedContext = getRuntime().newContext();

        this.mockCayennePersister = mock(ICayennePersister.class);
        when(mockCayennePersister.entityResolver()).thenReturn(getRuntime().getChannel().getEntityResolver());
        when(mockCayennePersister.sharedContext()).thenReturn(sharedContext);
        when(mockCayennePersister.newContext()).thenReturn(getRuntime().newContext());

        this.schema = new LazySchema(createEntityCompilers());
        this.pathDescriptorManager = new PathResolver(mockCayennePersister);
        this.queryAssembler = new CayenneQueryAssembler(
                mockCayennePersister,
                pathDescriptorManager,
                new CayenneExpParser(),
                new CayenneExpPostProcessor(pathDescriptorManager, mockCayennePersister));
    }

    protected List<AgEntityCompiler> createEntityCompilers() {

        AgEntityCompiler c1 = new CayenneAgEntityCompiler(
                mockCayennePersister,
                queryAssembler,
                Collections.emptyMap());

        AgEntityCompiler c2 = new AnnotationsAgEntityCompiler(Collections.emptyMap());

        return asList(c1, c2);
    }

    protected <T> AgEntity<T> getAgEntity(Class<T> type) {
        return schema.getEntity(type);
    }

    protected ObjEntity getEntity(Class<?> type) {
        return getRuntime().getChannel().getEntityResolver().getObjEntity(type);
    }

    protected <T> RootResourceEntity<T> getResourceEntity(Class<T> type) {
        return new RootResourceEntity<>(getAgEntity(type));
    }

    protected <T> ToOneResourceEntity<T> getToOneChildEntity(Class<T> type, ResourceEntity<?> parent, String incoming) {
        return new ToOneResourceEntity<>(getAgEntity(type), parent, parent.getAgEntity().getRelationship(incoming));
    }

    protected <T> ToManyResourceEntity<T> getToManyChildEntity(Class<T> type, ResourceEntity<?> parent, String incoming) {
        return new ToManyResourceEntity<>(getAgEntity(type), parent, parent.getAgEntity().getRelationship(incoming));
    }
}
