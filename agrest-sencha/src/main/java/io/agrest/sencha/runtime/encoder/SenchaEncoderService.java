package io.agrest.sencha.runtime.encoder;

import io.agrest.encoder.PropertyMetadataEncoder;
import io.agrest.runtime.encoder.DataEncoderFactory;
import io.agrest.runtime.encoder.EncoderService;
import io.agrest.runtime.encoder.IAttributeEncoderFactory;
import io.agrest.runtime.encoder.IStringConverterFactory;
import io.agrest.runtime.semantics.IRelationshipMapper;
import org.apache.cayenne.di.Inject;

import java.util.Map;
import java.util.Optional;

/**
 * @since 1.5
 */
public class SenchaEncoderService extends EncoderService {

    public SenchaEncoderService(
            @Inject IAttributeEncoderFactory attributeEncoderFactory,
            @Inject IStringConverterFactory stringConverterFactory,
			@Inject IRelationshipMapper relationshipMapper,
            @Inject Map<String, PropertyMetadataEncoder> propertyMetadataEncoders) {

        super(attributeEncoderFactory, stringConverterFactory, relationshipMapper, propertyMetadataEncoders);
    }

    @Override
    protected <T> DataEncoderFactory dataEncoderFactory() {
        return new SenchaDataEncoderFactory(
                attributeEncoderFactory,
                stringConverterFactory,
                relationshipMapper);
    }
}
