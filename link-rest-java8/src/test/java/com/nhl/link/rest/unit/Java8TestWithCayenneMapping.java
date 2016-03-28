package com.nhl.link.rest.unit;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.DefaultLrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrEntityOverlay;
import com.nhl.link.rest.meta.LrPersistentAttribute;
import com.nhl.link.rest.meta.parser.IResourceParser;
import com.nhl.link.rest.meta.parser.ResourceParser;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.meta.IResourceMetadataService;
import com.nhl.link.rest.runtime.meta.MetadataService;
import com.nhl.link.rest.runtime.meta.ResourceMetadataService;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.DataSourceFactory;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.di.Module;
import org.apache.cayenne.exp.Property;
import org.apache.cayenne.exp.parser.ASTPath;
import org.apache.cayenne.map.DbAttribute;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A superclass of Cayenne-aware test cases that do not need to access the DB,
 * but need to work with EntityResolver and higher levels of the stack.
 */
public class Java8TestWithCayenneMapping {

    protected static ServerRuntime runtime;

    @BeforeClass
    public static void setUpClass() {

        Module module = binder -> {
            DataSourceFactory dsf = mock(DataSourceFactory.class);
            binder.bind(DataSourceFactory.class).toInstance(dsf);
        };
        runtime = new ServerRuntime("cayenne-linkrest-java8-tests.xml", module);
    }

    @AfterClass
    public static void tearDownClass() {
        runtime.shutdown();
        runtime = null;
    }

    protected ICayennePersister mockCayennePersister;
    protected IMetadataService metadataService;
    protected IResourceMetadataService resourceMetadataService;
    protected IResourceParser resourceParser;

    @Before
    public void initLrDataMap() {

        ObjectContext sharedContext = runtime.newContext();

        this.mockCayennePersister = mock(ICayennePersister.class);
        when(mockCayennePersister.entityResolver()).thenReturn(runtime.getChannel().getEntityResolver());
        when(mockCayennePersister.sharedContext()).thenReturn(sharedContext);
        when(mockCayennePersister.newContext()).thenReturn(runtime.newContext());

        this.metadataService = createMetadataService();
        this.resourceParser = new ResourceParser(metadataService);
        this.resourceMetadataService = createResourceMetadataService();
    }

    protected IMetadataService createMetadataService() {
        return new MetadataService(Collections.<LrEntity<?>> emptyList(),
                Collections.<String, LrEntityOverlay<?>> emptyMap(), mockCayennePersister);
    }

    protected IResourceMetadataService createResourceMetadataService() {
        return new ResourceMetadataService(resourceParser);
    }

    protected <T> LrEntity<T> getLrEntity(Class<T> type) {
        return metadataService.getLrEntity(type);
    }

    protected ObjEntity getEntity(Class<?> type) {
        return runtime.getChannel().getEntityResolver().getObjEntity(type);
    }

    protected <T> ResourceEntity<T> getResourceEntity(Class<T> type) {
        return new ResourceEntity<>(getLrEntity(type));
    }

    protected <T> void appendAttribute(ResourceEntity<?> entity, Property<T> property, Class<T> type) {
        appendAttribute(entity, property.getName(), type);
    }

    protected void appendAttribute(ResourceEntity<?> entity, String name, Class<?> type) {
        entity.getAttributes().put(name, new DefaultLrAttribute(name, type.getName()));
    }


    protected  <T> void appendPersistenceAttribute(ResourceEntity<?> entity, Property<T> property, Class<T> javaType, int jdbcType) {
        appendPersistenceAttribute(entity, property.getName(), javaType, jdbcType);
    }

    protected void appendPersistenceAttribute(ResourceEntity<?> entity, String name, Class<?> javaType, int jdbcType) {
        entity.getAttributes().put(name, new TestLrPersistentAttribute(name, javaType.getName(), jdbcType));
    }

    private class TestLrPersistentAttribute implements LrPersistentAttribute {
        private String name;
        private String javaType;
        private int jdbcType;

        public TestLrPersistentAttribute(String name, String javaType, int jdbcType) {
            this.name = name;
            this.javaType = javaType;
            this.jdbcType = jdbcType;
        }

        @Override
        public int getJdbcType() {
            return jdbcType;
        }

        @Override
        public ObjAttribute getObjAttribute() {
            return null;
        }

        @Override
        public DbAttribute getDbAttribute() {
            return null;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getJavaType() {
            return javaType;
        }

        @Override
        public ASTPath getPathExp() {
            return null;
        }
    }
}
