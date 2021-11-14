package io.agrest.runtime.encoder;

import io.agrest.AgException;
import io.agrest.NestedResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.encoder.*;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgRelationship;
import io.agrest.property.PropertyReader;
import io.agrest.runtime.semantics.IRelationshipMapper;

import java.util.*;

/**
 * @since 3.4
 */
public class DataEncoderFactory {

    protected final IEncodablePropertyFactory encodablePropertyFactory;
    protected final IRelationshipMapper relationshipMapper;
    protected final IStringConverterFactory stringConverterFactory;

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

        return isMapBy ? mapByEncoder(entity, encoder) : encoder;
    }

    protected Encoder nestedToManyEncoder(ResourceEntity<?> entity) {

        Encoder elementEncoder = collectionElementEncoder(entity);
        boolean isMapBy = entity.getMapBy() != null;

        // if mapBy is involved, apply filters at MapBy level, not inside sublists...
        ListEncoder encoder = new ListEncoder(elementEncoder)
                .withOffset(entity.getFetchOffset())
                .withLimit(entity.getFetchLimit());

        if (entity.isFiltered()) {
            encoder.shouldFilter();
        }

        return isMapBy ? mapByEncoder(entity, encoder) : encoder;
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

    protected MapByEncoder mapByEncoder(ResourceEntity<?> entity, CollectionEncoder encoder) {
        return mapByEncoder(entity.getMapBy(), new ArrayList<>(), encoder, entity.getMapByPath());
    }

    protected MapByEncoder mapByEncoder(
            ResourceEntity<?> mapBy,
            List<PropertyReader> readerChain,
            CollectionEncoder encoder,
            String mapByPath) {

        // map by id
        if (mapBy.isIdIncluded()) {
            validateLeafMapBy(mapBy, mapByPath);
            readerChain.add(mapBy.getAgEntity().getIdReader());

            return new MapByEncoder(mapByPath,
                    readerChain,
                    encoder,
                    true,
                    stringConverterFactory.getConverter(mapBy.getAgEntity()));
        }

        // map by property
        if (!mapBy.getAttributes().isEmpty()) {
            validateLeafMapBy(mapBy, mapByPath);

            Map.Entry<String, AgAttribute> attribute = mapBy.getAttributes().entrySet().iterator().next();
            readerChain.add(encodablePropertyFactory.getAttributeProperty(mapBy, attribute.getValue()).getReader());
            return new MapByEncoder(mapByPath,
                    readerChain,
                    encoder,
                    false,
                    stringConverterFactory.getConverter(mapBy.getAgEntity(), attribute.getKey()));
        }

        // descend into relationship
        if (!mapBy.getChildren().isEmpty()) {

            Map.Entry<String, NestedResourceEntity<?>> child = mapBy.getChildren().entrySet().iterator().next();

            // TODO: to account for overlaid relationships (and avoid NPEs), we should not access agEntity...
            //  instead should look for incoming rel of a child ResourceEntity.. Is is present in MapBy case?
            AgRelationship relationship = mapBy.getChild(child.getKey()).getIncoming();
            readerChain.add(encodablePropertyFactory.getRelationshipProperty(mapBy, relationship, null).getReader());

            return mapByEncoder(mapBy.getChildren().get(child.getKey()), readerChain, encoder, mapByPath);
        }

        // map by relationship (implicitly by id)
        readerChain.add(mapBy.getAgEntity().getIdReader());

        return new MapByEncoder(mapByPath,
                readerChain,
                encoder,
                true,
                stringConverterFactory.getConverter(mapBy.getAgEntity()));
    }

    protected void validateLeafMapBy(ResourceEntity<?> mapBy, String mapByPath) {

        if (!mapBy.getChildren().isEmpty()) {

            String pathSegment = (mapBy instanceof NestedResourceEntity)
                    ? ((NestedResourceEntity<?>) mapBy).getIncoming().getName()
                    : "";

            throw AgException.badRequest(
                    "'mapBy' path segment '%s' should not have children. Full 'mapBy' path: %s",
                    pathSegment,
                    mapByPath);
        }
    }
}
