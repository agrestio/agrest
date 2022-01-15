package io.agrest.runtime.encoder;

import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.converter.valuestring.ValueStringConverters;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.EntityMetadataEncoder;
import io.agrest.encoder.PropertyMetadataEncoder;
import io.agrest.encoder.ResourceEncoder;
import io.agrest.processor.ProcessingContext;
import io.agrest.runtime.semantics.IRelationshipMapper;
import org.apache.cayenne.di.Inject;

import java.util.Map;

public class EncoderService implements IEncoderService {

    protected final IEncodablePropertyFactory attributeEncoderFactory;
    protected final IRelationshipMapper relationshipMapper;
    protected final ValueStringConverters converters;

    @Deprecated
    protected Map<String, PropertyMetadataEncoder> propertyMetadataEncoders;

    public EncoderService(
            @Inject IEncodablePropertyFactory attributeEncoderFactory,
            @Inject ValueStringConverters converters,
            @Inject IRelationshipMapper relationshipMapper,
            @Inject Map<String, PropertyMetadataEncoder> propertyMetadataEncoders) {

        this.attributeEncoderFactory = attributeEncoderFactory;
        this.relationshipMapper = relationshipMapper;
        this.converters = converters;
        this.propertyMetadataEncoders = propertyMetadataEncoders;
    }

    /**
     * @deprecated since 4.1, as Agrest now integrates with OpenAPI 3 / Swagger.
     */
    @Deprecated
    @Override
    public <T> Encoder metadataEncoder(RootResourceEntity<T> entity) {
        return new ResourceEncoder<>(entity, entity.getApplicationBase(), entityMetadataEncoder(entity));
    }

    @Override
    public <T> Encoder dataEncoder(ResourceEntity<T> entity, ProcessingContext<T> context) {
        return dataEncoderFactory().encoder(entity, context);
    }

    protected DataEncoderFactory dataEncoderFactory() {
        return new DataEncoderFactory(attributeEncoderFactory, converters, relationshipMapper);
    }

    @Deprecated
    protected Encoder entityMetadataEncoder(ResourceEntity<?> resourceEntity) {
        return new EntityMetadataEncoder(resourceEntity, propertyMetadataEncoders);
    }
}
