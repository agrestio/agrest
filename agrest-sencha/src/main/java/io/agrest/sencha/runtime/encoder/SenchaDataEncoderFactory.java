package io.agrest.sencha.runtime.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.encoder.EncodableProperty;
import io.agrest.ResourceEntity;
import io.agrest.encoder.CollectionEncoder;
import io.agrest.encoder.DataResponseEncoder;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.GenericEncoder;
import io.agrest.meta.AgRelationship;
import io.agrest.runtime.encoder.DataEncoderFactory;
import io.agrest.runtime.encoder.IEncodablePropertyFactory;
import io.agrest.runtime.encoder.IStringConverterFactory;
import io.agrest.runtime.semantics.IRelationshipMapper;
import io.agrest.sencha.encoder.SenchaEntityToOneEncoder;

import java.io.IOException;
import java.util.Optional;


/**
 * @since 3.4
 */
public class SenchaDataEncoderFactory extends DataEncoderFactory {

    public SenchaDataEncoderFactory(
            IEncodablePropertyFactory encodablePropertyFactory,
            IStringConverterFactory stringConverterFactory,
            IRelationshipMapper relationshipMapper) {
        super(encodablePropertyFactory, stringConverterFactory, relationshipMapper);
    }

    @Override
    public <T> Encoder encoder(ResourceEntity<T> entity) {

        CollectionEncoder dataEncoder = dataEncoder(entity);

        return new DataResponseEncoder("data", dataEncoder, "total", GenericEncoder.encoder()) {
            @Override
            protected void encodeObjectBody(Object object, JsonGenerator out) throws IOException {
                out.writeFieldName("success");
                out.writeBoolean(true);
                super.encodeObjectBody(object, out);
            }
        };
    }

    @Override
    protected Encoder toOneEncoder(ResourceEntity<?> resourceEntity, AgRelationship relationship) {
        // to-one encoder is made of the following decorator layers (from outer
        // to inner):
        // (1) custom filters ->
        // (2) composite [value + id encoder]
        // different structure from to-many, so building it differently

        Encoder valueEncoder = entityEncoder(resourceEntity);
        Optional<EncodableProperty> idEncoder = encodablePropertyFactory.getIdProperty(resourceEntity);
        Encoder compositeValueEncoder = idEncoder
                .map(ide -> senchaToOnEntityEncoder(relationship, valueEncoder, ide))
                .orElse(valueEncoder);
        
        return filteredEncoder(compositeValueEncoder, resourceEntity);
    }

    private Encoder senchaToOnEntityEncoder(AgRelationship relationship, Encoder valueEncoder, EncodableProperty idEncoder) {
        return new SenchaEntityToOneEncoder(relationshipMapper.toRelatedIdName(relationship), valueEncoder, idEncoder);
    }
}
