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
import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgEntity;
import io.agrest.meta.LazyAgDataMap;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.DataSourceFactory;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.di.Module;
import org.apache.cayenne.map.ObjEntity;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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
public abstract class CayenneNoDbTest {

    protected static ServerRuntime runtime;

    protected ICayennePersister mockCayennePersister;
    protected IPathResolver pathDescriptorManager;
    protected AgDataMap dataMap;
    protected CayenneQueryAssembler queryAssembler;

    @BeforeAll
    public static void setUpClass() {
        Module module = binder -> {
            DataSourceFactory dsf = mock(DataSourceFactory.class);
            binder.bind(DataSourceFactory.class).toInstance(dsf);
        };

        runtime = ServerRuntime
                .builder()
                .addConfig("cayenne-project.xml")
                .addModule(module)
                .build();
    }

    @AfterAll
    public static void tearDownClass() {
        runtime.shutdown();
        runtime = null;
    }

    @BeforeEach
    public void initAgDataMap() {

        ObjectContext sharedContext = runtime.newContext();

        this.mockCayennePersister = mock(ICayennePersister.class);
        when(mockCayennePersister.entityResolver()).thenReturn(runtime.getChannel().getEntityResolver());
        when(mockCayennePersister.sharedContext()).thenReturn(sharedContext);
        when(mockCayennePersister.newContext()).thenReturn(runtime.newContext());

        this.dataMap = new LazyAgDataMap(createEntityCompilers());
        this.pathDescriptorManager = new PathResolver();
        this.queryAssembler = new CayenneQueryAssembler(
                () -> dataMap,
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
        return dataMap.getEntity(type);
    }

    protected ObjEntity getEntity(Class<?> type) {
        return runtime.getChannel().getEntityResolver().getObjEntity(type);
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
