package com.nhl.link.rest.runtime.adapter.sencha;

import java.util.List;

import org.apache.cayenne.di.Inject;

import com.nhl.link.rest.EntityProperty;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.encoder.EncoderFilter;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.runtime.encoder.EncoderService;
import com.nhl.link.rest.runtime.encoder.IAttributeEncoderFactory;
import com.nhl.link.rest.runtime.encoder.IStringConverterFactory;
import com.nhl.link.rest.runtime.semantics.IRelationshipMapper;

/**
 * @since 1.5
 */
public class SenchaEncoderService extends EncoderService {

	public SenchaEncoderService(@Inject(ENCODER_FILTER_LIST) List<EncoderFilter> filters,
			@Inject IAttributeEncoderFactory attributeEncoderFactory,
			@Inject IStringConverterFactory stringConverterFactory, @Inject IRelationshipMapper relationshipMapper) {
		super(filters, attributeEncoderFactory, stringConverterFactory, relationshipMapper);
	}

	@Override
	protected Encoder toOneEncoder(ResourceEntity<?> resourceEntity, final LrRelationship relationship) {
		// to-one encoder is made of the following decorator layers (from outer
		// to inner):
		// (1) custom filters ->
		// (2) composite [value + id encoder]
		// different structure from to-many, so building it differently

		Encoder valueEncoder = entityEncoder(resourceEntity);
		EntityProperty idEncoder = attributeEncoderFactory.getIdProperty(resourceEntity);
		Encoder compositeValueEncoder = new SenchaEntityToOneEncoder(valueEncoder, idEncoder) {

			// we know that created encoder will only be used for encoding a
			// single known property, so hardcode the ID property to avoid
			// relationshipMapper lookups in a loop
			final String idPropertyName = relationshipMapper.toRelatedIdName(relationship);

			@Override
			protected String idPropertyName(String propertyName) {
				return idPropertyName;
			}
		};

		return filteredEncoder(compositeValueEncoder, resourceEntity);
	}

}
