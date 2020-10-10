package io.agrest.runtime.encoder;

import io.agrest.NestedResourceEntity;
import io.agrest.encoder.EncodableProperty;
import io.agrest.ResourceEntity;
import io.agrest.encoder.CollectionEncoder;
import io.agrest.encoder.DataResponseEncoder;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.EntityEncoder;
import io.agrest.encoder.EntityNoIdEncoder;
import io.agrest.encoder.FilterChainEncoder;
import io.agrest.encoder.GenericEncoder;
import io.agrest.encoder.ListEncoder;
import io.agrest.encoder.MapByEncoder;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgRelationship;
import io.agrest.runtime.semantics.IRelationshipMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @since 3.4
 */
public class DataEncoderFactory {

    protected IEncodablePropertyFactory encodablePropertyFactory;
    protected IRelationshipMapper relationshipMapper;
    private IStringConverterFactory stringConverterFactory;

    public DataEncoderFactory(
            IEncodablePropertyFactory encodablePropertyFactory,
            IStringConverterFactory stringConverterFactory,
            IRelationshipMapper relationshipMapper) {

        this.encodablePropertyFactory = encodablePropertyFactory;
        this.relationshipMapper = relationshipMapper;
        this.stringConverterFactory = stringConverterFactory;
    }

    public <T> Encoder encoder(ResourceEntity<T> entity) {
        CollectionEncoder dataEncoder = dataEncoder(entity);
        return new DataResponseEncoder("data", dataEncoder, "total", GenericEncoder.encoder());
    }

    protected CollectionEncoder dataEncoder(ResourceEntity<?> entity) {
        Encoder elementEncoder = collectionElementEncoder(entity);
        boolean isMapBy = entity.getMapBy() != null;

        // notice that we are not passing either qualifier or ordering to the encoder, as those are presumably applied
        // at the query level.. (unlike with #nestedToManyEncoder)

        CollectionEncoder encoder = new ListEncoder(elementEncoder)
                .withOffset(entity.getFetchOffset())
                .withLimit(entity.getFetchLimit())
                .shouldFilter(entity.isFiltered());

        return isMapBy ?
                new MapByEncoder(
                        entity.getMapByPath(),
                        entity.getMapBy(),
                        encoder,
                        stringConverterFactory,
                        encodablePropertyFactory)
                : encoder;
    }

    protected Encoder nestedToManyEncoder(ResourceEntity<?> resourceEntity) {

        Encoder elementEncoder = collectionElementEncoder(resourceEntity);
        boolean isMapBy = resourceEntity.getMapBy() != null;

        // if mapBy is involved, apply filters at MapBy level, not inside sublists...
        ListEncoder listEncoder = new ListEncoder(elementEncoder)
                .withOffset(resourceEntity.getFetchOffset())
                .withLimit(resourceEntity.getFetchLimit());

        if (resourceEntity.isFiltered()) {
            listEncoder.shouldFilter();
        }

        return isMapBy ?
                new MapByEncoder(
                        resourceEntity.getMapByPath(),
                        resourceEntity.getMapBy(),
                        listEncoder,
                        stringConverterFactory,
                        encodablePropertyFactory)
                : listEncoder;
    }

    protected Encoder collectionElementEncoder(ResourceEntity<?> resourceEntity) {
        Encoder encoder = entityEncoder(resourceEntity);
        return filteredEncoder(encoder, resourceEntity);
    }

    protected Encoder toOneEncoder(ResourceEntity<?> resourceEntity, AgRelationship relationship) {

        // to-one encoder is made of the following decorator layers (from outer to inner):
        // (1) custom filters ->
        // (2) value encoder
        // different structure from to-many, so building it differently

        Encoder valueEncoder = entityEncoder(resourceEntity);
        return filteredEncoder(valueEncoder, resourceEntity);
    }

    /**
     * Recursively builds an Encoder for the ResourceEntity tree.
     *
     * @param resourceEntity root entity to be encoded
     * @return a new Encoder for the provided ResourceEntity tree
     */
    protected Encoder entityEncoder(ResourceEntity<?> resourceEntity) {

        Map<String, EncodableProperty> attributeEncoders = new HashMap<>();

        for (AgAttribute attribute : resourceEntity.getAttributes().values()) {
            EncodableProperty property = encodablePropertyFactory.getAttributeProperty(resourceEntity, attribute);
            attributeEncoders.put(attribute.getName(), property);
        }

        Map<String, EncodableProperty> relationshipEncoders = new HashMap<>();
        for (Map.Entry<String, NestedResourceEntity<?>> e : resourceEntity.getChildren().entrySet()) {

            // read relationship vis child entity to account for overlays
            AgRelationship relationship = resourceEntity.getChild(e.getKey()).getIncoming();

            Encoder encoder = relationship.isToMany()
                    ? nestedToManyEncoder(e.getValue())
                    : toOneEncoder(e.getValue(), relationship);

            EncodableProperty property = encodablePropertyFactory.getRelationshipProperty(
                    resourceEntity,
                    relationship,
                    encoder);

            relationshipEncoders.put(e.getKey(), property);
        }

        Optional<EncodableProperty> idEncoder = resourceEntity.isIdIncluded()
                ? encodablePropertyFactory.getIdProperty(resourceEntity)
                : Optional.empty();
        return idEncoder
                .map(ide -> (Encoder) new EntityEncoder(ide, attributeEncoders, relationshipEncoders))
                .orElseGet(() -> new EntityNoIdEncoder(attributeEncoders, relationshipEncoders));
    }

    protected Encoder filteredEncoder(Encoder encoder, ResourceEntity<?> resourceEntity) {
        return resourceEntity.getEntityEncoderFilters().isEmpty()
                ? encoder
                : new FilterChainEncoder(encoder, resourceEntity.getEntityEncoderFilters());
    }
}
