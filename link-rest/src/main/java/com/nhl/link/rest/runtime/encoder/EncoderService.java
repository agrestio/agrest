package com.nhl.link.rest.runtime.encoder;

import static com.nhl.link.rest.property.PropertyBuilder.dataObjectProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.di.Inject;

import com.nhl.link.rest.EntityProperty;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.encoder.EncoderFilter;
import com.nhl.link.rest.encoder.EntityEncoder;
import com.nhl.link.rest.encoder.EntityMetadataEncoder;
import com.nhl.link.rest.encoder.EntityToOneEncoder;
import com.nhl.link.rest.encoder.FilterChainEncoder;
import com.nhl.link.rest.encoder.ListEncoder;
import com.nhl.link.rest.encoder.MapByEncoder;
import com.nhl.link.rest.encoder.PropertyMetadataEncoder;
import com.nhl.link.rest.encoder.ResourceEncoder;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.property.PropertyBuilder;
import com.nhl.link.rest.runtime.semantics.IRelationshipMapper;

public class EncoderService implements IEncoderService {

	public static final String ENCODER_FILTER_LIST = "linkrest.encoder.filter.list";
	public static final String PROPERTY_METADATA_ENCODER_MAP = "linkrest.metadata.encoder.map";

	protected IAttributeEncoderFactory attributeEncoderFactory;
	private IStringConverterFactory stringConverterFactory;
	protected IRelationshipMapper relationshipMapper;
	private List<EncoderFilter> filters;
	private Map<String, PropertyMetadataEncoder> propertyMetadataEncoders;
	private Map<ResourceEntity<?>, Encoder> entityMetadataEncoders;

	public EncoderService(@Inject(ENCODER_FILTER_LIST) List<EncoderFilter> filters,
			@Inject IAttributeEncoderFactory attributeEncoderFactory,
			@Inject IStringConverterFactory stringConverterFactory, @Inject IRelationshipMapper relationshipMapper,
			@Inject(PROPERTY_METADATA_ENCODER_MAP) Map<String, PropertyMetadataEncoder> propertyMetadataEncoders) {
		this.attributeEncoderFactory = attributeEncoderFactory;
		this.relationshipMapper = relationshipMapper;
		this.stringConverterFactory = stringConverterFactory;
		this.filters = filters;
		this.propertyMetadataEncoders = propertyMetadataEncoders;
		this.entityMetadataEncoders = new ConcurrentHashMap<>();
	}

	@Override
	public <T> Encoder metadataEncoder(ResourceEntity<T> entity) {
		return new ResourceEncoder<>(entity.getLrEntity(), entity.getApplicationBase(), entityMetadataEncoder(entity));
	}

	@Override
	public <T> Encoder dataEncoder(ResourceEntity<T> entity) {

		// TODO: in theory we can support this actually, but leaving it for
		// another day, as fetch limit/offset and other filtering is
		// non-trivial.
		if (entity.getMapBy() != null) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Can't apply 'mapBy' to the root entity.");
		}

		Encoder elementEncoder = collectionElementEncoder(entity);

		// notice that we are not passing either qualifier or ordering to the
		// encoder, as those are presumably applied at the query level.. (unlike
		// with #nestedToManyEncoder)

		ListEncoder rootEncoder = new ListEncoder(elementEncoder).withTotal("total").withOffset(entity.getFetchOffset())
				.withLimit(entity.getFetchLimit());

		if (entity.isFiltered()) {
			rootEncoder.shouldFilter();
		}
		return rootEncoder;
	}

	private Encoder nestedToManyEncoder(ResourceEntity<?> resourceEntity) {

		Encoder elementEncoder = collectionElementEncoder(resourceEntity);
		boolean isMapBy = resourceEntity.getMapBy() != null;

		// if mapBy is involved, apply filters at MapBy level, not inside
		// sublists...
		ListEncoder listEncoder = new ListEncoder(
				elementEncoder,
				isMapBy? null : resourceEntity.getQualifier(),
				resourceEntity.getOrderings()
		);

		listEncoder.withOffset(resourceEntity.getFetchOffset()).withLimit(resourceEntity.getFetchLimit());

		if (resourceEntity.isFiltered()) {
			listEncoder.shouldFilter();
		}

		return isMapBy? new MapByEncoder(resourceEntity.getMapByPath(), resourceEntity.getQualifier(),
					resourceEntity.getMapBy(), listEncoder, stringConverterFactory) : listEncoder;
	}



	private Encoder collectionElementEncoder(ResourceEntity<?> resourceEntity) {
		Encoder encoder = entityEncoder(resourceEntity);
		return filteredEncoder(encoder, resourceEntity);
	}

	protected Encoder toOneEncoder(ResourceEntity<?> resourceEntity, LrRelationship relationship) {

		// to-one encoder is made of the following decorator layers (from outer
		// to inner):
		// (1) custom filters ->
		// (2) value encoder
		// different structure from to-many, so building it differently

		Encoder valueEncoder = entityEncoder(resourceEntity);
		Encoder compositeValueEncoder = new EntityToOneEncoder(valueEncoder);
		return filteredEncoder(compositeValueEncoder, resourceEntity);
	}

	protected Encoder entityMetadataEncoder(ResourceEntity<?> resourceEntity) {
		Encoder encoder = entityMetadataEncoders.get(resourceEntity);

		if (encoder == null) {
			encoder = new EntityMetadataEncoder(resourceEntity.getLrEntity(), propertyMetadataEncoders);
			entityMetadataEncoders.put(resourceEntity, encoder);
		}

		return encoder;
	}

	protected Encoder entityEncoder(ResourceEntity<?> resourceEntity) {

		// ensure we sort property encoders alphabetically for cleaner JSON
		// output
		Map<String, EntityProperty> properties = new TreeMap<String, EntityProperty>();

		for (LrAttribute attribute : resourceEntity.getAttributes().values()) {
			EntityProperty property = attributeEncoderFactory.getAttributeProperty(resourceEntity, attribute);
			properties.put(attribute.getName(), property);
		}

		for (Entry<String, ResourceEntity<?>> e : resourceEntity.getChildren().entrySet()) {
			LrRelationship relationship = resourceEntity.getLrEntity().getRelationship(e.getKey());

			Encoder encoder = relationship.isToMany() ? nestedToManyEncoder(e.getValue())
					: toOneEncoder(e.getValue(), relationship);

			properties.put(e.getKey(), dataObjectProperty().encodedWith(encoder));
		}

		properties.putAll(resourceEntity.getExtraProperties());

		EntityProperty idEncoder = resourceEntity.isIdIncluded() ? attributeEncoderFactory.getIdProperty(resourceEntity)
				: PropertyBuilder.doNothingProperty();
		return new EntityEncoder(idEncoder, properties);
	}

	protected Encoder filteredEncoder(Encoder encoder, ResourceEntity<?> resourceEntity) {
		List<EncoderFilter> matchingFilters = null;

		for (EncoderFilter filter : filters) {
			if (filter.matches(resourceEntity)) {
				if (matchingFilters == null) {
					matchingFilters = new ArrayList<>(3);
				}

				matchingFilters.add(filter);
			}
		}

		return matchingFilters != null ? new FilterChainEncoder(encoder, matchingFilters) : encoder;
	}

}
