package com.nhl.link.rest.runtime;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Feature;
import javax.ws.rs.ext.ExceptionMapper;

import org.apache.cayenne.CayenneRuntimeException;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.di.Binder;
import org.apache.cayenne.di.DIBootstrap;
import org.apache.cayenne.di.Injector;
import org.apache.cayenne.di.Module;
import org.apache.cayenne.validation.ValidationException;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.EntityConstraint;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.MetadataResponse;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.encoder.EncoderFilter;
import com.nhl.link.rest.encoder.PropertyMetadataEncoder;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrEntityBuilder;
import com.nhl.link.rest.meta.LrEntityOverlay;
import com.nhl.link.rest.meta.parser.IResourceParser;
import com.nhl.link.rest.meta.parser.ResourceParser;
import com.nhl.link.rest.provider.CayenneRuntimeExceptionMapper;
import com.nhl.link.rest.provider.DataResponseWriter;
import com.nhl.link.rest.provider.LinkRestExceptionMapper;
import com.nhl.link.rest.provider.MetadataResponseWriter;
import com.nhl.link.rest.provider.SimpleResponseWriter;
import com.nhl.link.rest.provider.ValidationExceptionMapper;
import com.nhl.link.rest.runtime.adapter.LinkRestAdapter;
import com.nhl.link.rest.runtime.cayenne.CayennePersister;
import com.nhl.link.rest.runtime.cayenne.CayenneProcessorFactory;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;
import com.nhl.link.rest.runtime.cayenne.NoCayennePersister;
import com.nhl.link.rest.runtime.constraints.ConstraintsHandler;
import com.nhl.link.rest.runtime.constraints.IConstraintsHandler;
import com.nhl.link.rest.runtime.encoder.AttributeEncoderFactory;
import com.nhl.link.rest.runtime.encoder.EncoderService;
import com.nhl.link.rest.runtime.encoder.IAttributeEncoderFactory;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.encoder.IStringConverterFactory;
import com.nhl.link.rest.runtime.encoder.StringConverterFactory;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.jackson.JacksonService;
import com.nhl.link.rest.runtime.listener.IListenerService;
import com.nhl.link.rest.runtime.listener.ListenerService;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.meta.IResourceMetadataService;
import com.nhl.link.rest.runtime.meta.MetadataService;
import com.nhl.link.rest.runtime.meta.ResourceMetadataService;
import com.nhl.link.rest.runtime.parser.IRequestParser;
import com.nhl.link.rest.runtime.parser.RequestParser;
import com.nhl.link.rest.runtime.parser.cache.IPathCache;
import com.nhl.link.rest.runtime.parser.cache.PathCache;
import com.nhl.link.rest.runtime.parser.converter.DefaultJsonValueConverterFactory;
import com.nhl.link.rest.runtime.parser.converter.IJsonValueConverterFactory;
import com.nhl.link.rest.runtime.parser.filter.CayenneExpProcessor;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpProcessor;
import com.nhl.link.rest.runtime.parser.filter.IKeyValueExpProcessor;
import com.nhl.link.rest.runtime.parser.filter.KeyValueExpProcessor;
import com.nhl.link.rest.runtime.parser.sort.ISortProcessor;
import com.nhl.link.rest.runtime.parser.sort.SortProcessor;
import com.nhl.link.rest.runtime.parser.tree.ITreeProcessor;
import com.nhl.link.rest.runtime.parser.tree.IncludeExcludeProcessor;
import com.nhl.link.rest.runtime.processor.IProcessorFactory;
import com.nhl.link.rest.runtime.semantics.IRelationshipMapper;
import com.nhl.link.rest.runtime.semantics.RelationshipMapper;

/**
 * A builder of LinkRest runtime that can be loaded into JAX-RS 2 container as a
 * {@link Feature}.
 */
public class LinkRestBuilder {

	private ICayennePersister cayenneService;

	private Class<? extends ILinkRestService> linkRestServiceType;
	private ILinkRestService linkRestService;

	private List<EncoderFilter> encoderFilters;
	private List<LrEntity<?>> extraEntities;
	private Map<String, LrEntityOverlay<?>> entityOverlays;
	private Map<Class<?>, Class<?>> exceptionMappers;
	private Collection<LinkRestAdapter> adapters;
	private Map<String, PropertyMetadataEncoder> metadataEncoders;

	/**
	 * A shortcut that creates a LinkRest stack based on Cayenne runtime and
	 * default settings.
	 * 
	 * @since 1.14
	 */
	public static LinkRestRuntime build(ServerRuntime cayenneRuntime) {
		return builder(cayenneRuntime).build();
	}

	/**
	 * A shortcut that creates a LinkRestBuilder, setting its Cayenne runtime. A
	 * caller can continue customizing the returned builder.
	 * 
	 * @since 1.14
	 */
	public static LinkRestBuilder builder(ServerRuntime cayenneRuntime) {
		return new LinkRestBuilder().cayenneRuntime(cayenneRuntime);
	}

	public LinkRestBuilder() {
		this.extraEntities = new ArrayList<>();
		this.entityOverlays = new HashMap<>();
		this.encoderFilters = new ArrayList<>();
		this.linkRestServiceType = DefaultLinkRestService.class;
		this.cayenneService = NoCayennePersister.instance();

		this.exceptionMappers = mapDefaultExceptions();
		this.adapters = new ArrayList<>();
		this.metadataEncoders = new HashMap<>();
	}

	protected Map<Class<?>, Class<?>> mapDefaultExceptions() {

		Map<Class<?>, Class<?>> map = new HashMap<>();
		map.put(CayenneRuntimeException.class, CayenneRuntimeExceptionMapper.class);
		map.put(LinkRestException.class, LinkRestExceptionMapper.class);
		map.put(ValidationException.class, ValidationExceptionMapper.class);

		return map;
	}

	/**
	 * Maps an ExceptionMapper for a given type of Exceptions. While this method
	 * can be used for arbitrary exceptions, it is most useful to override the
	 * default exception handlers defined in LinkRest for the following
	 * exceptions: {@link LinkRestException}, {@link CayenneRuntimeException},
	 * {@link ValidationException}.
	 * 
	 * @since 1.1
	 */
	public <E extends Throwable> LinkRestBuilder mapException(Class<? extends ExceptionMapper<E>> mapper) {

		for (Type t : mapper.getGenericInterfaces()) {

			if (t instanceof ParameterizedType) {
				ParameterizedType pt = (ParameterizedType) t;
				if (ExceptionMapper.class.equals(pt.getRawType())) {
					Type[] args = pt.getActualTypeArguments();
					exceptionMappers.put((Class<?>) args[0], mapper);
					return this;
				}
			}

		}

		throw new IllegalArgumentException("Failed to register ExceptionMapper: " + mapper.getName());
	}

	public LinkRestBuilder linkRestService(ILinkRestService linkRestService) {
		this.linkRestService = linkRestService;
		this.linkRestServiceType = null;
		return this;
	}

	public LinkRestBuilder linkRestService(Class<? extends ILinkRestService> linkRestServiceType) {
		this.linkRestService = null;
		this.linkRestServiceType = linkRestServiceType;
		return this;
	}

	public LinkRestBuilder cayenneRuntime(ServerRuntime cayenneRuntime) {
		this.cayenneService = new CayennePersister(cayenneRuntime);
		return this;
	}

	public LinkRestBuilder cayenneService(ICayennePersister cayenneService) {
		this.cayenneService = cayenneService;
		return this;
	}

	public LinkRestBuilder encoderFilter(EncoderFilter filter) {
		this.encoderFilters.add(filter);
		return this;
	}

	public LinkRestBuilder encoderFilters(Collection<EncoderFilter> filters) {
		this.encoderFilters.addAll(filters);
		return this;
	}

	/**
	 * Adds a user-configured entity to the LinkRest runtime model. Usually used
	 * for POJOs, as Cayenne entities are all automatically loaded from
	 * CayenneRuntime.
	 * 
	 * @since 1.12
	 * @see LrEntityBuilder
	 */
	public LinkRestBuilder extraEntity(LrEntity<?> entity) {
		this.extraEntities.add(entity);
		return this;
	}

	/**
	 * Adds a list of user-configured entities to the LinkRest runtime model.
	 * Usually used for POJOs, as Cayenne entities are all automatically loaded
	 * from CayenneRuntime.
	 * 
	 * @since 1.12
	 * @see LrEntityBuilder
	 */
	public LinkRestBuilder extraEntities(Collection<? extends LrEntity<?>> entities) {
		this.extraEntities.addAll(entities);
		return this;
	}

	/**
	 * Exposes a non-persistent property of a persistent type. Once declared
	 * such property can be rendered in responses, referenced in include/exclude
	 * keys, etc.
	 * 
	 * @since 1.12
	 */
	public LinkRestBuilder transientProperty(Class<?> type, String propertyName) {

		LrEntityOverlay<?> overlay = entityOverlays.get(type.getName());
		if (overlay == null) {
			overlay = new LrEntityOverlay<>(type);
			entityOverlays.put(type.getName(), overlay);
		}

		overlay.getTransientAttributes().add(propertyName);

		return this;
	}

	/**
	 * Adds an adapter that may contribute custom configuration to
	 * {@link LinkRestRuntime}.
	 * 
	 * @since 1.3
	 */
	public LinkRestBuilder adapter(LinkRestAdapter adapter) {
		this.adapters.add(adapter);
		return this;
	}

	public LinkRestBuilder metadataEncoder(String type, PropertyMetadataEncoder encoder) {
		this.metadataEncoders.put(type, encoder);
		return this;
	}

	public LinkRestRuntime build() {
		Injector i = createInjector();
		return new LinkRestRuntime(i, createExtraFeatures(), createExtraComponents());
	}

	private Collection<Class<?>> createExtraComponents() {
		// for now the only extra components are exception mappers
		return exceptionMappers.values();
	}

	private Collection<Feature> createExtraFeatures() {

		if (adapters.isEmpty()) {
			return Collections.emptyList();
		}

		Collection<Feature> features = new ArrayList<>(adapters.size());
		for (LinkRestAdapter a : adapters) {
			a.contributeToJaxRs(features);
		}

		return features;
	}

	private Injector createInjector() {

		if (linkRestService == null && linkRestServiceType == null) {
			throw new IllegalStateException("Required 'linkRestService' is not set");
		}

		Module module = new Module() {

			@Override
			public void configure(Binder binder) {

				binder.<EncoderFilter> bindList(EncoderService.ENCODER_FILTER_LIST).addAll(encoderFilters);
				binder.<LrEntity<?>> bindList(MetadataService.EXTRA_ENTITIES_LIST).addAll(extraEntities);
				binder.<LrEntityOverlay<?>> bindMap(MetadataService.ENTITY_OVERLAY_MAP).putAll(entityOverlays);
				binder.<Class<?>> bindMap(LinkRestRuntime.BODY_WRITERS_MAP)
						.put(SimpleResponse.class.getName(), SimpleResponseWriter.class)
						.put(DataResponse.class.getName(), DataResponseWriter.class)
						.put(MetadataResponse.class.getName(), MetadataResponseWriter.class);

				binder.<EntityConstraint> bindList(ConstraintsHandler.DEFAULT_READ_CONSTRAINTS_LIST);
				binder.<EntityConstraint> bindList(ConstraintsHandler.DEFAULT_WRITE_CONSTRAINTS_LIST);
				binder.<PropertyMetadataEncoder> bindMap(EncoderService.PROPERTY_METADATA_ENCODER_MAP)
						.putAll(metadataEncoders);

				if (linkRestServiceType != null) {
					binder.bind(ILinkRestService.class).to(linkRestServiceType);
				} else {
					binder.bind(ILinkRestService.class).toInstance(linkRestService);
				}

				binder.bind(IProcessorFactory.class).to(CayenneProcessorFactory.class);
				binder.bind(IRequestParser.class).to(RequestParser.class);
				binder.bind(IJsonValueConverterFactory.class).to(DefaultJsonValueConverterFactory.class);
				binder.bind(IAttributeEncoderFactory.class).to(AttributeEncoderFactory.class);
				binder.bind(IStringConverterFactory.class).to(StringConverterFactory.class);
				binder.bind(IEncoderService.class).to(EncoderService.class);
				binder.bind(IRelationshipMapper.class).to(RelationshipMapper.class);
				binder.bind(IMetadataService.class).to(MetadataService.class);
				binder.bind(IListenerService.class).to(ListenerService.class);
				binder.bind(IResourceMetadataService.class).to(ResourceMetadataService.class);
				binder.bind(IConstraintsHandler.class).to(ConstraintsHandler.class);
				binder.bind(ICayenneExpProcessor.class).to(CayenneExpProcessor.class);
				binder.bind(IKeyValueExpProcessor.class).to(KeyValueExpProcessor.class);

				binder.bind(IJacksonService.class).to(JacksonService.class);
				binder.bind(ICayennePersister.class).toInstance(cayenneService);

				binder.bind(IPathCache.class).to(PathCache.class);
				binder.bind(ISortProcessor.class).to(SortProcessor.class);
				binder.bind(ITreeProcessor.class).to(IncludeExcludeProcessor.class);

				binder.bind(IResourceParser.class).to(ResourceParser.class);

				// apply adapter-contributed bindings
				for (LinkRestAdapter a : adapters) {
					a.contributeToRuntime(binder);
				}
			}
		};

		return DIBootstrap.createInjector(module);
	}
}
