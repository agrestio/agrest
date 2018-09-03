package io.agrest.sencha.runtime.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.EntityProperty;
import io.agrest.ResourceEntity;
import io.agrest.encoder.CollectionEncoder;
import io.agrest.encoder.DataResponseEncoder;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.EncoderFilter;
import io.agrest.encoder.GenericEncoder;
import io.agrest.encoder.PropertyMetadataEncoder;
import io.agrest.meta.LrRelationship;
import io.agrest.runtime.encoder.EncoderService;
import io.agrest.runtime.encoder.IAttributeEncoderFactory;
import io.agrest.runtime.encoder.IStringConverterFactory;
import io.agrest.runtime.semantics.IRelationshipMapper;
import io.agrest.sencha.encoder.SenchaEntityToOneEncoder;
import org.apache.cayenne.di.Inject;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @since 1.5
 */
public class SenchaEncoderService extends EncoderService {

	public SenchaEncoderService(@Inject List<EncoderFilter> filters,
								@Inject IAttributeEncoderFactory attributeEncoderFactory,
								@Inject IStringConverterFactory stringConverterFactory, @Inject IRelationshipMapper relationshipMapper,
								@Inject Map<String, PropertyMetadataEncoder> propertyMetadataEncoders) {
		super(filters, attributeEncoderFactory, stringConverterFactory, relationshipMapper, propertyMetadataEncoders);
	}

	@Override
	public <T> Encoder dataEncoder(ResourceEntity<T> entity) {
		CollectionEncoder resultEncoder = resultEncoder(entity);
		return new DataResponseEncoder("data", resultEncoder, "total", GenericEncoder.encoder()) {
			@Override
			protected void encodeObjectBody(Object object, JsonGenerator out) throws IOException {
				out.writeFieldName("success");
				out.writeBoolean(true);
				super.encodeObjectBody(object, out);
			}
		};
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
