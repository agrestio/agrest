package io.agrest.unit;

import io.agrest.ResourceEntity;
import io.agrest.backend.util.converter.ExpressionMatcher;
import io.agrest.backend.util.converter.OrderingConverter;
import io.agrest.backend.util.converter.OrderingSorter;
import io.agrest.meta.DefaultAgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgPersistentAttribute;
import io.agrest.meta.compiler.CayenneEntityCompiler;
import io.agrest.meta.compiler.AgEntityCompiler;
import io.agrest.meta.parser.IResourceParser;
import io.agrest.meta.parser.ResourceParser;
import io.agrest.runtime.IAgPersister;
import io.agrest.runtime.cayenne.converter.CayenneExpressionMatcher;
import io.agrest.runtime.cayenne.converter.CayenneOrderingConverter;
import io.agrest.runtime.cayenne.converter.CayenneOrderingSorter;
import io.agrest.runtime.meta.BaseUrlProvider;
import io.agrest.runtime.meta.IMetadataService;
import io.agrest.runtime.meta.IResourceMetadataService;
import io.agrest.runtime.meta.MetadataService;
import io.agrest.runtime.meta.ResourceMetadataService;
import io.agrest.runtime.parser.converter.DefaultJsonValueConverterFactoryProvider;
import io.agrest.runtime.parser.converter.IJsonValueConverterFactory;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.DataSourceFactory;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.di.Module;
import org.apache.cayenne.exp.Property;
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

	protected IAgPersister mockCayennePersister;
	protected IMetadataService metadataService;
	protected IResourceMetadataService resourceMetadataService;
	protected IResourceParser resourceParser;
	protected ExpressionMatcher expressionMatcher;
	protected OrderingConverter orderingConverter;
	protected OrderingSorter orderingSorter;

	private IJsonValueConverterFactory converterFactory;

	@Before
	public void initAgDataMap() {

		ObjectContext sharedContext = runtime.newContext();

		this.mockCayennePersister = mock(IAgPersister.class);
		when(mockCayennePersister.entityResolver()).thenReturn(runtime.getChannel().getEntityResolver());
		when(mockCayennePersister.sharedContext()).thenReturn(sharedContext);
		when(mockCayennePersister.newContext()).thenReturn(runtime.newContext());

		this.converterFactory = new DefaultJsonValueConverterFactoryProvider(Collections.emptyMap()).get();
		this.metadataService = createMetadataService();
		this.resourceParser = new ResourceParser(metadataService);
		this.resourceMetadataService = createResourceMetadataService();
		this.expressionMatcher = new CayenneExpressionMatcher();
		this.orderingConverter = new CayenneOrderingConverter();
		this.orderingSorter = new CayenneOrderingSorter();
	}

	protected IMetadataService createMetadataService() {

		List<AgEntityCompiler> compilers = asList(new CayenneEntityCompiler(mockCayennePersister, Collections.emptyMap(), converterFactory));
		return new MetadataService(compilers);
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

	protected <T, E> ResourceEntity<T, E> getResourceEntity(Class<T> type) {
		return new ResourceEntity<>(getAgEntity(type));
	}

	protected <T> void appendAttribute(ResourceEntity<?, ?> entity, Property<T> property, Class<T> type) {
		appendAttribute(entity, property.getName(), type);
	}

	protected void appendAttribute(ResourceEntity<?, ?> entity, String name, Class<?> type) {
		entity.getAttributes().put(name, new DefaultAgAttribute(name, type));
	}

	protected <T> void appendPersistenceAttribute(ResourceEntity<?, ?> entity, Property<T> property, Class<T> javaType,
			int jdbcType) {
		appendPersistenceAttribute(entity, property.getName(), javaType, jdbcType);
	}

	protected void appendPersistenceAttribute(ResourceEntity<?, ?> entity, String name, Class<?> javaType, int jdbcType) {
		entity.getAttributes().put(name,
				new TestAgPersistentAttribute(name, javaType, jdbcType));
	}

	private class TestAgPersistentAttribute extends DefaultAgAttribute implements AgPersistentAttribute<String> {
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
		public String getPathExp() {
			return null;
		}
	}
}
