package io.agrest.unit;

import io.agrest.ResourceEntity;
import io.agrest.meta.AgEntity;
import io.agrest.meta.DefaultAgAttribute;
import io.agrest.meta.AgPersistentAttribute;
import io.agrest.meta.cayenne.CayenneEntityCompiler;
import io.agrest.meta.compiler.AgEntityCompiler;
import io.agrest.meta.compiler.PojoEntityCompiler;
import io.agrest.meta.parser.IResourceParser;
import io.agrest.meta.parser.ResourceParser;
import io.agrest.runtime.cayenne.ICayennePersister;
import io.agrest.runtime.meta.BaseUrlProvider;
import io.agrest.runtime.meta.IMetadataService;
import io.agrest.runtime.meta.IResourceMetadataService;
import io.agrest.runtime.meta.MetadataService;
import io.agrest.runtime.meta.ResourceMetadataService;
import io.agrest.runtime.parser.converter.DefaultJsonValueConverterFactoryProvider;
import io.agrest.runtime.parser.converter.IJsonValueConverterFactory;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.DataSourceFactory;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.di.Module;
import org.apache.cayenne.exp.Property;
import org.apache.cayenne.exp.parser.ASTPath;
import org.apache.cayenne.map.ObjEntity;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A superclass of Cayenne-aware test cases that do not need to access the DB,
 * but need to work with EntityResolver and higher levels of the stack.
 */
public class TestWithCayenneMapping {

	protected static ServerRuntime runtime;

	@BeforeClass
	public static void setUpClass() {
		Module module = binder -> {
            DataSourceFactory dsf = mock(DataSourceFactory.class);
            binder.bind(DataSourceFactory.class).toInstance(dsf);
        };
		runtime = ServerRuntime
				.builder()
				.addConfig("cayenne-linkrest-tests.xml")
				.addModule(module)
				.build();
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
	protected IJsonValueConverterFactory converterFactory;

	@Before
	public void initLrDataMap() {

		ObjectContext sharedContext = runtime.newContext();

		this.mockCayennePersister = mock(ICayennePersister.class);
		when(mockCayennePersister.entityResolver()).thenReturn(runtime.getChannel().getEntityResolver());
		when(mockCayennePersister.sharedContext()).thenReturn(sharedContext);
		when(mockCayennePersister.newContext()).thenReturn(runtime.newContext());

		this.converterFactory = new DefaultJsonValueConverterFactoryProvider(Collections.emptyMap()).get();
		this.metadataService = createMetadataService();
		this.resourceParser = new ResourceParser(metadataService);
		this.resourceMetadataService = createResourceMetadataService();
	}

	protected IMetadataService createMetadataService() {

		List<AgEntityCompiler> compilers = new ArrayList<>();
		compilers.add(new CayenneEntityCompiler(mockCayennePersister, Collections.emptyMap(), converterFactory));
		compilers.add(new PojoEntityCompiler(Collections.emptyMap()));

		return new MetadataService(compilers, mockCayennePersister);
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

	protected <T> AgEntity<T> getLrEntity(Class<T> type) {
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
		entity.getAttributes().put(name, new DefaultAgAttribute(name, type));
	}

	protected <T> void appendPersistenceAttribute(ResourceEntity<?> entity, Property<T> property, Class<T> javaType,
			int jdbcType) {
		appendPersistenceAttribute(entity, property.getName(), javaType, jdbcType);
	}

	protected void appendPersistenceAttribute(ResourceEntity<?> entity, String name, Class<?> javaType, int jdbcType) {
		entity.getAttributes().put(name,
				new TestAgPersistentAttribute(name, javaType, jdbcType));
	}

	private class TestAgPersistentAttribute extends DefaultAgAttribute implements AgPersistentAttribute {
		private int jdbcType;

		public TestAgPersistentAttribute(String name, Class<?> javaType, int jdbcType) {
			super(name, javaType);
			this.jdbcType = jdbcType;
		}

		@Override
		public int getJdbcType() {
			return jdbcType;
		}

		@Override
		public String getColumnName() {
			return getName();
		}

		@Override
		public boolean isMandatory() {
			return false;
		}

		@Override
		public ASTPath getPathExp() {
			return null;
		}
	}
}
