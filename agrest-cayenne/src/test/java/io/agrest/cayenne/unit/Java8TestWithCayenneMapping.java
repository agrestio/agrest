package io.agrest.cayenne.unit;

import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.cayenne.compiler.CayenneEntityCompiler;
import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.meta.AgEntity;
import io.agrest.meta.DefaultAgAttribute;
import io.agrest.meta.compiler.AgEntityCompiler;
import io.agrest.meta.compiler.PojoEntityCompiler;
import io.agrest.meta.parser.IResourceParser;
import io.agrest.meta.parser.ResourceParser;
import io.agrest.property.BeanPropertyReader;
import io.agrest.runtime.meta.BaseUrlProvider;
import io.agrest.runtime.meta.IMetadataService;
import io.agrest.runtime.meta.IResourceMetadataService;
import io.agrest.runtime.meta.MetadataService;
import io.agrest.runtime.meta.ResourceMetadataService;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.DataSourceFactory;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.di.Module;
import org.apache.cayenne.exp.Property;
import org.apache.cayenne.exp.parser.ASTObjPath;
import org.apache.cayenne.exp.parser.ASTPath;
import org.apache.cayenne.map.ObjEntity;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A superclass of Cayenne-aware test cases that do not need to access the DB,
 * but need to work with EntityResolver and higher levels of the stack.
 */
public class Java8TestWithCayenneMapping {

    protected static ServerRuntime runtime;

    protected ICayennePersister mockCayennePersister;
    protected IMetadataService metadataService;
    protected IResourceMetadataService resourceMetadataService;
    protected IResourceParser resourceParser;

    @BeforeClass
    public static void setUpClass() {

        Module module = binder -> {
            DataSourceFactory dsf = mock(DataSourceFactory.class);
            binder.bind(DataSourceFactory.class).toInstance(dsf);
        };

        runtime = ServerRuntime
                .builder()
                .addConfig("cayenne-agrest-java8-tests.xml")
                .addModule(module)
                .build();
    }

    @AfterClass
    public static void tearDownClass() {
        runtime.shutdown();
        runtime = null;
    }

    @Before
    public void initAgDataMap() {

        ObjectContext sharedContext = runtime.newContext();

        this.mockCayennePersister = mock(ICayennePersister.class);
        when(mockCayennePersister.entityResolver()).thenReturn(runtime.getChannel().getEntityResolver());
        when(mockCayennePersister.sharedContext()).thenReturn(sharedContext);
        when(mockCayennePersister.newContext()).thenReturn(runtime.newContext());

        this.metadataService = new MetadataService(createEntityCompilers());
        this.resourceParser = new ResourceParser(metadataService);
        this.resourceMetadataService = createResourceMetadataService();
    }

    protected List<AgEntityCompiler> createEntityCompilers() {

        AgEntityCompiler c1 = new CayenneEntityCompiler(
                mockCayennePersister,
                Collections.emptyMap());

        AgEntityCompiler c2 = new PojoEntityCompiler(Collections.emptyMap());

        return asList(c1, c2);
    }

    protected IResourceMetadataService createResourceMetadataService() {
        return new ResourceMetadataService(resourceParser, BaseUrlProvider.forUrl(Optional.empty()));
    }

    protected <T> AgEntity<T> getAgEntity(Class<T> type) {
        return metadataService.getAgEntity(type);
    }

    protected ObjEntity getEntity(Class<?> type) {
        return runtime.getChannel().getEntityResolver().getObjEntity(type);
    }

    protected <T> RootResourceEntity<T> getResourceEntity(Class<T> type) {
        return new RootResourceEntity<>(getAgEntity(type), null);
    }

    protected <T> void appendAttribute(ResourceEntity<?> entity, Property<T> property, Class<T> type) {
        appendAttribute(entity, property.getName(), type);
    }

    protected void appendAttribute(ResourceEntity<?> entity, String name, Class<?> type) {
        entity.getAttributes().put(name, new DefaultAgAttribute(name, type, new ASTObjPath(name), BeanPropertyReader.reader()));
    }

    protected <T> void appendPersistenceAttribute(ResourceEntity<?> entity, Property<T> property, Class<T> javaType) {
        appendPersistenceAttribute(entity, property.getName(), javaType);
    }

    protected void appendPersistenceAttribute(ResourceEntity<?> entity, String name, Class<?> javaType) {
        entity.getAttributes().put(name,
                new TestAgPersistentAttribute(name, javaType));
    }

    private class TestAgPersistentAttribute extends DefaultAgAttribute {

        public TestAgPersistentAttribute(String name, Class<?> javaType) {
            super(name, javaType, new ASTObjPath(name), BeanPropertyReader.reader());
        }

        @Override
        public ASTPath getPathExp() {
            return null;
        }
    }
}
