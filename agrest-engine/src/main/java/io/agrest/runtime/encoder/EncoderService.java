package io.agrest.runtime.encoder;

import io.agrest.ResourceEntity;
import io.agrest.converter.valuestring.ValueStringConverters;
import io.agrest.encoder.Encoder;
import io.agrest.processor.ProcessingContext;
import io.agrest.runtime.semantics.IRelationshipMapper;
import org.apache.cayenne.di.Inject;

public class EncoderService implements IEncoderService {

    protected final IEncodablePropertyFactory attributeEncoderFactory;
    protected final IRelationshipMapper relationshipMapper;
    protected final ValueStringConverters converters;

    public EncoderService(
            @Inject IEncodablePropertyFactory attributeEncoderFactory,
            @Inject ValueStringConverters converters,
            @Inject IRelationshipMapper relationshipMapper) {

        this.attributeEncoderFactory = attributeEncoderFactory;
        this.relationshipMapper = relationshipMapper;
        this.converters = converters;
    }

    @Override
    public <T> Encoder dataEncoder(ResourceEntity<T> entity, ProcessingContext<T> context) {
        return dataEncoderFactory().encoder(entity, context);
    }

    protected DataEncoderFactory dataEncoderFactory() {
        return new DataEncoderFactory(attributeEncoderFactory, converters, relationshipMapper);
    }
}
