package io.agrest.cayenne.unit;

import io.agrest.NestedResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.cayenne.compiler.CayenneAgEntityCompiler;
import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.cayenne.processor.CayenneQueryAssembler;
import io.agrest.cayenne.processor.ICayenneQueryAssembler;
import io.agrest.cayenne.qualifier.QualifierParser;
import io.agrest.cayenne.qualifier.QualifierPostProcessor;
import io.agrest.compiler.AgEntityCompiler;
import io.agrest.compiler.AnnotationsAgEntityCompiler;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgEntity;
import io.agrest.meta.LazyAgDataMap;
import io.agrest.meta.parser.IResourceParser;
import io.agrest.meta.parser.ResourceParser;
import io.agrest.runtime.meta.BaseUrlProvider;
import io.agrest.runtime.meta.IResourceMetadataService;
import io.agrest.runtime.meta.ResourceMetadataService;
import io.agrest.runtime.path.IPathDescriptorManager;
import io.agrest.runtime.path.PathDescriptorManager;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.DataSourceFactory;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.di.Module;
import org.apache.cayenne.map.ObjEntity;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    protected IPathDescriptorManager pathDescriptorManager;
    protected AgDataMap dataMap;
    protected IResourceMetadataService resourceMetadataService;
    protected IResourceParser resourceParser;
    protected ICayenneQueryAssembler queryAssembler;

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
        this.resourceParser = new ResourceParser(dataMap);
        this.resourceMetadataService = createResourceMetadataService();

        this.pathDescriptorManager = new PathDescriptorManager();
        this.queryAssembler = new CayenneQueryAssembler(
                mockCayennePersister,
                pathDescriptorManager,
                new QualifierParser(),
                new QualifierPostProcessor(pathDescriptorManager));
    }

    protected List<AgEntityCompiler> createEntityCompilers() {

        AgEntityCompiler c1 = new CayenneAgEntityCompiler(
                mockCayennePersister,
                queryAssembler,
                Collections.emptyMap());

        AgEntityCompiler c2 = new AnnotationsAgEntityCompiler(Collections.emptyMap());

        return asList(c1, c2);
    }

    protected IResourceMetadataService createResourceMetadataService() {
        return new ResourceMetadataService(resourceParser, BaseUrlProvider.forUrl(Optional.empty()));
    }

    protected <T> SelectContext<T> prepareContext(MultivaluedMap<String, String> params, Class<T> type) {
        SelectContext<T> context = new SelectContext<>(type);

        UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getQueryParameters()).thenReturn(params);

        context.setUriInfo(uriInfo);
        return context;
    }

    protected <T> AgEntity<T> getAgEntity(Class<T> type) {
        return dataMap.getEntity(type);
    }

    protected ObjEntity getEntity(Class<?> type) {
        return runtime.getChannel().getEntityResolver().getObjEntity(type);
    }

    protected <T> RootResourceEntity<T> getResourceEntity(Class<T> type) {
        return new RootResourceEntity<>(getAgEntity(type), null);
    }

    protected <T> NestedResourceEntity<T> getChildResourceEntity(Class<T> type, ResourceEntity<?> parent, String incoming) {
        return new NestedResourceEntity<>(getAgEntity(type), null, parent, parent.getAgEntity().getRelationship(incoming));
    }
}
