package com.nhl.link.rest.runtime.encoder;

import static com.nhl.link.rest.property.PropertyBuilder.dataObjectProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.di.Inject;
import org.apache.cayenne.map.ObjRelationship;

import com.nhl.link.rest.Entity;
import com.nhl.link.rest.EntityProperty;
import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.encoder.EncoderFilter;
import com.nhl.link.rest.encoder.EntityEncoder;
import com.nhl.link.rest.encoder.EntityToOneEncoder;
import com.nhl.link.rest.encoder.FilterChainEncoder;
import com.nhl.link.rest.encoder.GenericEncoder;
import com.nhl.link.rest.encoder.ListEncoder;
import com.nhl.link.rest.encoder.MapByEncoder;
import com.nhl.link.rest.encoder.RootListEncoder;
import com.nhl.link.rest.property.PropertyBuilder;
import com.nhl.link.rest.runtime.semantics.IRelationshipMapper;

public class EncoderService implements IEncoderService {

	public static final String ENCODER_FILTER_LIST = "linkrest.encoder.filter.list";

	private IAttributeEncoderFactory attributeEncoderFactory;
	private IStringConverterFactory stringConverterFactory;
	private IRelationshipMapper relationshipMapper;
	private List<EncoderFilter> filters;

	public EncoderService(@Inject(ENCODER_FILTER_LIST) List<EncoderFilter> filters,
			@Inject IAttributeEncoderFactory attributeEncoderFactory,
			@Inject IStringConverterFactory stringConverterFactory, @Inject IRelationshipMapper relationshipMapper) {
		this.attributeEncoderFactory = attributeEncoderFactory;
		this.relationshipMapper = relationshipMapper;
		this.stringConverterFactory = stringConverterFactory;
		this.filters = filters;
	}

	@Override
	public <T> DataResponse<T> makeEncoder(DataResponse<T> response) {
		return response.withEncoder(rootEncoder(response));
	}

	private <T> Encoder rootEncoder(DataResponse<T> response) {

		Entity<T> entity = response.getEntity();

		// TODO: in theory we can support this actually, but leaving it for
		// another day, as fetch limit/offset and other filtering is
		// non-trivial.
		if (entity.getMapBy() != null) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Can't apply 'mapBy' to the root entity.");
		}

		// don't bother compiling element encoder for empty response... as long
		// as 'makeEncoder' is called after the objects are fetched, we
		// should be fine

		// TODO: this is flaky -- if this method is called before the
		// objects were set, the result will be garbage

		Encoder elementEncoder = response.getObjects().isEmpty() ? GenericEncoder.encoder()
				: collectionElementEncoder(entity);

		// notice that we are not passing either qualifier or ordering to the
		// encoder, as those are presumably applied at the query level.. (unlike
		// with #nestedToManyEncoder)

		return new RootListEncoder(elementEncoder).withTotal("total").withOffset(response.getFetchOffset())
				.withLimit(response.getFetchLimit());
	}

	private Encoder nestedToManyEncoder(Entity<?> clientEntity) {

		Encoder elementEncoder = collectionElementEncoder(clientEntity);

		if ((clientEntity.getMapBy() != null)) {

			// if mapBy is involved, apply filters at MapBy level, not inside
			// sublists...
			Encoder listEncoder = new ListEncoder(elementEncoder, null, clientEntity.getOrderings());

			return new MapByEncoder(clientEntity.getMapByPath(), clientEntity.getQualifier(), clientEntity.getMapBy(),
					listEncoder, stringConverterFactory);

		} else {
			return new ListEncoder(elementEncoder, clientEntity.getQualifier(), clientEntity.getOrderings());
		}
	}

	private Encoder collectionElementEncoder(Entity<?> clientEntity) {
		Encoder encoder = entityEncoder(clientEntity);
		return filteredEncoder(encoder, clientEntity);
	}

	private Encoder toOneEncoder(Entity<?> clientEntity, final ObjRelationship relationship) {

		// to-one encoder is made of the following decorator layers (from outer
		// to inner):
		// (1) custom filters ->
		// (2) composite [value + id encoder]
		// different structure from to-many, so building it differently

		Encoder valueEncoder = entityEncoder(clientEntity);
		EntityProperty idEncoder = attributeEncoderFactory.getIdProperty(clientEntity);
		Encoder compositeValueEncoder = new EntityToOneEncoder(valueEncoder, idEncoder) {

			// we know that created encoder will only be used for encoding a
			// single known property, so hardcode the ID property to avoid
			// relationshipMapper lookups in a loop
			final String idPropertyName = relationshipMapper.toRelatedIdName(relationship);

			@Override
			protected String idPropertyName(String propertyName) {
				return idPropertyName;
			}
		};

		return filteredEncoder(compositeValueEncoder, clientEntity);
	}

	private Encoder entityEncoder(Entity<?> clientEntity) {

		// ensure we sort property encoders alphabetically for cleaner JSON
		// output
		Map<String, EntityProperty> properties = new TreeMap<String, EntityProperty>();

		for (String attribute : clientEntity.getAttributes()) {
			EntityProperty property = attributeEncoderFactory.getAttributeProperty(clientEntity, attribute);
			properties.put(attribute, property);
		}

		for (Entry<String, Entity<?>> e : clientEntity.getChildren().entrySet()) {
			ObjRelationship relationship = (ObjRelationship) clientEntity.getCayenneEntity().getRelationship(e.getKey());

			Encoder encoder = relationship.isToMany() ? nestedToManyEncoder(e.getValue()) : toOneEncoder(e.getValue(),
					relationship);

			properties.put(e.getKey(), dataObjectProperty().encodedWith(encoder));
		}

		properties.putAll(clientEntity.getExtraProperties());

		EntityProperty idEncoder = clientEntity.isIdIncluded() ? attributeEncoderFactory.getIdProperty(clientEntity)
				: PropertyBuilder.doNothingProperty();
		return new EntityEncoder(idEncoder, properties);
	}

	private Encoder filteredEncoder(Encoder encoder, Entity<?> clientEntity) {
		List<EncoderFilter> matchingFilters = null;

		for (EncoderFilter filter : filters) {
			if (filter.matches(clientEntity)) {
				if (matchingFilters == null) {
					matchingFilters = new ArrayList<>(3);
				}

				matchingFilters.add(filter);
			}
		}

		return matchingFilters != null ? new FilterChainEncoder(encoder, matchingFilters) : encoder;
	}

}
