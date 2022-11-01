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
import org.apache.cayenne.configuration.server.DataSourceFactory;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.di.Module;
import org.apache.cayenne.map.ObjEntity;
import org.junit.jupiter.api.BeforeEach;

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

    protected static ServerRuntime createRuntime(String project) {
        Module module = binder -> {
            DataSourceFactory dsf = mock(DataSourceFactory.class);
            binder.bind(DataSourceFactory.class).toInstance(dsf);
        };

        return ServerRuntime
                .builder()
                .addConfig(project)
                .addModule(module)
                .build();
    }

    protected abstract ServerRuntime getRuntime();

    @BeforeEach
    public void initAgSchema() {

        ObjectContext sharedContext = getRuntime().newContext();

        this.mockCayennePersister = mock(ICayennePersister.class);
        when(mockCayennePersister.entityResolver()).thenReturn(getRuntime().getChannel().getEntityResolver());
        when(mockCayennePersister.sharedContext()).thenReturn(sharedContext);
        when(mockCayennePersister.newContext()).thenReturn(getRuntime().newContext());

        this.schema = new LazySchema(createEntityCompilers());
        this.pathDescriptorManager = new PathResolver();
        this.queryAssembler = new CayenneQueryAssembler(
                mockCayennePersister,
                pathDescriptorManager,
                new CayenneExpParser(),
                new CayenneExpPostProcessor(pathDescriptorManager));
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
