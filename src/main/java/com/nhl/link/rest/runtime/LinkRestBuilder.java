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
import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.validation.ValidationException;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.encoder.EncoderFilter;
import com.nhl.link.rest.provider.CayenneRuntimeExceptionMapper;
import com.nhl.link.rest.provider.LinkRestExceptionMapper;
import com.nhl.link.rest.provider.ValidationExceptionMapper;
import com.nhl.link.rest.runtime.adapter.LinkRestAdapter;
import com.nhl.link.rest.runtime.cayenne.CayennePersister;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;
import com.nhl.link.rest.runtime.cayenne.NoCayennePersister;
import com.nhl.link.rest.runtime.constraints.DefaultConstraintsHandler;
import com.nhl.link.rest.runtime.constraints.IConstraintsHandler;
import com.nhl.link.rest.runtime.encoder.AttributeEncoderFactory;
import com.nhl.link.rest.runtime.encoder.EncoderService;
import com.nhl.link.rest.runtime.encoder.IAttributeEncoderFactory;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.encoder.IStringConverterFactory;
import com.nhl.link.rest.runtime.encoder.StringConverterFactory;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.jackson.JacksonService;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.meta.MetadataService;
import com.nhl.link.rest.runtime.parser.IRequestParser;
import com.nhl.link.rest.runtime.parser.RequestParser;
import com.nhl.link.rest.runtime.semantics.IRelationshipMapper;
import com.nhl.link.rest.runtime.semantics.RelationshipMapper;
import com.nhl.link.rest.update.UpdateFilter;

/**
 * A builder of LinkRest runtime that can be loaded into JAX-RS 2 container as a
 * {@link Feature}.
 */
public class LinkRestBuilder {

	private ICayennePersister cayenneService;

	private Class<? extends ILinkRestService> linkRestServiceType;
	private ILinkRestService linkRestService;

	private List<EncoderFilter> encoderFilters;
	private List<DataMap> nonPersistentEntities;
	private Map<Class<?>, Class<?>> exceptionMappers;
	private Collection<LinkRestAdapter> adapters;

	public LinkRestBuilder() {
		this.nonPersistentEntities = new ArrayList<>();
		this.encoderFilters = new ArrayList<>();
		this.linkRestServiceType = EntityDaoLinkRestService.class;
		this.cayenneService = NoCayennePersister.instance();

		this.exceptionMappers = mapDefaultExceptions();
		this.adapters = new ArrayList<>();
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

	public LinkRestBuilder nonPersistentEntities(DataMap dataMap) {
		this.nonPersistentEntities.add(dataMap);
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

	public LinkRestRuntime build() {
		Injector i = createInjector();
		Feature f = new LinkRestFeature(i, createExtraFeatures(), createExtraComponents());
		return new LinkRestRuntime(f, i);
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

				binder.<UpdateFilter> bindList(RequestParser.UPDATE_FILTER_LIST);
				binder.<EncoderFilter> bindList(EncoderService.ENCODER_FILTER_LIST).addAll(encoderFilters);
				binder.<DataMap> bindList(MetadataService.NON_PERSISTENT_ENTITIES_LIST).addAll(nonPersistentEntities);

				if (linkRestServiceType != null) {
					binder.bind(ILinkRestService.class).to(linkRestServiceType);
				} else {
					binder.bind(ILinkRestService.class).toInstance(linkRestService);
				}

				binder.bind(IRequestParser.class).to(RequestParser.class);
				binder.bind(IAttributeEncoderFactory.class).to(AttributeEncoderFactory.class);
				binder.bind(IStringConverterFactory.class).to(StringConverterFactory.class);
				binder.bind(IEncoderService.class).to(EncoderService.class);
				binder.bind(IRelationshipMapper.class).to(RelationshipMapper.class);
				binder.bind(IMetadataService.class).to(MetadataService.class);
				binder.bind(IConstraintsHandler.class).to(DefaultConstraintsHandler.class);

				binder.bind(IJacksonService.class).to(JacksonService.class);
				binder.bind(ICayennePersister.class).toInstance(cayenneService);

				// apply adapter-contributed bindings
				for (LinkRestAdapter a : adapters) {
					a.contributeToRuntime(binder);
				}
			}
		};

		return DIBootstrap.createInjector(module);
	}
}
