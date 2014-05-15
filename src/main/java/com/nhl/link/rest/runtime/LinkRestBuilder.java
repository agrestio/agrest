package com.nhl.link.rest.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.core.Feature;

import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.di.Binder;
import org.apache.cayenne.di.DIBootstrap;
import org.apache.cayenne.di.Injector;
import org.apache.cayenne.di.Module;
import org.apache.cayenne.map.DataMap;

import com.nhl.link.rest.encoder.EncoderFilter;
import com.nhl.link.rest.runtime.cayenne.CayennePersister;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;
import com.nhl.link.rest.runtime.cayenne.NoCayennePersister;
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

	public LinkRestBuilder() {
		this.nonPersistentEntities = new ArrayList<>();
		this.encoderFilters = new ArrayList<>();
		this.linkRestServiceType = EntityDaoLinkRestService.class;
		this.cayenneService = NoCayennePersister.instance();
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

	public LinkRestRuntime build() {
		Injector i = createInjector();
		Feature f = new LinkRestFeature(i);
		return new LinkRestRuntime(f, i);
	}

	private Injector createInjector() {

		if (linkRestService == null && linkRestServiceType == null) {
			throw new IllegalStateException("Required 'linkRestService' is not set");
		}

		Module module = new Module() {

			@Override
			public void configure(Binder binder) {

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

				binder.bind(IJacksonService.class).to(JacksonService.class);
				binder.bind(ICayennePersister.class).toInstance(cayenneService);
			}
		};

		return DIBootstrap.createInjector(module);
	}
}
