package io.agrest.runtime.encoder;

import io.agrest.AgException;
import io.agrest.RelatedResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.converter.valuestring.ValueStringConverters;
import io.agrest.encoder.DataResponseEncoder;
import io.agrest.encoder.EncodableProperty;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.EntityEncoder;
import io.agrest.encoder.EntityNoIdEncoder;
import io.agrest.encoder.GenericEncoder;
import io.agrest.encoder.InheritanceAwareEntityEncoder;
import io.agrest.encoder.ListEncoder;
import io.agrest.encoder.MapByEncoder;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;
import io.agrest.processor.ProcessingContext;
import io.agrest.reader.DataReader;
import io.agrest.runtime.semantics.IRelationshipMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

/**
 * @since 3.4
 */
public class DataEncoderFactory {

    protected final IEncodablePropertyFactory propertyFactory;
    protected final IRelationshipMapper relationshipMapper;
    protected final ValueStringConverters converters;

    public DataEncoderFactory(
            IEncodablePropertyFactory propertyFactory,
            ValueStringConverters converters,
            IRelationshipMapper relationshipMapper) {

        this.propertyFactory = propertyFactory;
        this.relationshipMapper = relationshipMapper;
        this.converters = converters;
    }

    public <T> Encoder encoder(ResourceEntity<T> entity, ProcessingContext<T> context) {
        Encoder dataEncoder = dataEncoder(entity, context);
        return new DataResponseEncoder("data", dataEncoder, "total", GenericEncoder.encoder());
    }

    protected Encoder dataEncoder(ResourceEntity<?> entity, ProcessingContext<?> context) {
        Encoder elementEncoder = collectionElementEncoder(entity, context);
        Encoder listEncoder = new ListEncoder(elementEncoder);
        return entity.getMapBy() != null ? mapByEncoder(entity, listEncoder, context) : listEncoder;
    }

    protected Encoder toOneEncoder(ResourceEntity<?> resourceEntity, ProcessingContext<?> context) {
        return entityEncoder(resourceEntity, context);
    }

    protected Encoder relatedToManyEncoder(ResourceEntity<?> entity, ProcessingContext<?> context) {
        Encoder elementEncoder = collectionElementEncoder(entity, context);
        ListEncoder listEncoder = new ListEncoder(elementEncoder);
        return entity.getMapBy() != null ? mapByEncoder(entity, listEncoder, context) : listEncoder;
    }

    protected Encoder collectionElementEncoder(ResourceEntity<?> resourceEntity, ProcessingContext<?> context) {
        return entityEncoder(resourceEntity, context);
    }

    /**
     * Recursively builds an Encoder for the ResourceEntity tree.
     *
     * @param entity root entity to be encoded
     * @return a new Encoder for the provided ResourceEntity tree
     */
    protected <T> Encoder entityEncoder(ResourceEntity<T> entity, ProcessingContext<?> context) {
        return entity.getAgEntity().getSubEntities().isEmpty()
                ? singleEntityEncoder(entity, entity.getAgEntity(), context)
                : inheritanceAwareEntityEncoder(entity, context);
    }

    protected <T> Encoder inheritanceAwareEntityEncoder(ResourceEntity<T> entity, ProcessingContext<?> context) {

        Map<Class<?>, Encoder> hierarchyEncoders = collectEncodersInInheritanceHierarchy(
                new HashMap<>(), entity, entity.getAgEntity(), context
        );

        return new InheritanceAwareEntityEncoder(hierarchyEncoders);
    }

    protected <T> Map<Class<?>, Encoder> collectEncodersInInheritanceHierarchy(
            Map<Class<?>, Encoder> toCollect,
            ResourceEntity<? super T> entity,
            AgEntity<T> agEntity,
            ProcessingContext<?> context) {

        agEntity.getSubEntities().forEach(ae -> collectEncodersInInheritanceHierarchy(toCollect, entity, ae, context));

        if (!agEntity.isAbstract()) {
            Encoder encoder = singleEntityEncoder(entity, agEntity, context);
            toCollect.put(agEntity.getType(), encoder);
        }

        return toCollect;
    }

    protected <T> Encoder singleEntityEncoder(
            ResourceEntity<T> entity,
            AgEntity<? extends T> subEntity,
            ProcessingContext<?> context) {

        Map<String, EncodableProperty> encoders = propertyEncoders(entity, subEntity, context);

        EncodableProperty ide = entity.isIdIncluded()
                ? propertyFactory.getIdProperty(entity).orElse(null)
                : null;

        return ide != null
                ? new EntityEncoder(ide, encoders)
                : new EntityNoIdEncoder(encoders);
    }

    protected <T> Map<String, EncodableProperty> propertyEncoders(
            ResourceEntity<T> entity,
            AgEntity<? extends T> subEntity,
            ProcessingContext<?> context) {

        List<Map.Entry<String, EncodableProperty>> entries = new ArrayList<>();

        for (AgAttribute attribute : entity.getAttributes(subEntity)) {
            EncodableProperty property = propertyFactory.getAttributeProperty(entity, attribute);
            entries.add(entry(attribute.getName(), property));
        }

        for(AgRelationship relationship : entity.getRelationships(subEntity)) {

            // TODO: "related.getIncoming()" will not be the same as "relationship" in case of inheritance,
            //   The hope is that "getIncoming()" is not used downstream, but this is not ideal and can cause subtle bugs

            RelatedResourceEntity<?> related = entity.getChild(relationship.getName());
            Encoder encoder = relationship.isToMany()
                    ? relatedToManyEncoder(related, context)
                    : toOneEncoder(related, context);

            EncodableProperty property = propertyFactory.getRelationshipProperty(
                    entity,
                    relationship,
                    encoder,
                    context);

            entries.add(entry(relationship.getName(), property));
        }

        switch (entries.size()) {
            case 0:
                return Collections.emptyMap();
            case 1:
                return Map.ofEntries(entries.get(0));
            default:
                // sort properties alphabetically to ensure predictable and user-friendly JSON
                entries.sort(Map.Entry.comparingByKey());
                Map<String, EncodableProperty> sorted = new LinkedHashMap<>((int) Math.ceil(entries.size() / 0.75));
                entries.forEach(e -> sorted.put(e.getKey(), e.getValue()));

                return sorted;
        }
    }

    protected MapByEncoder mapByEncoder(ResourceEntity<?> entity, Encoder encoder, ProcessingContext<?> context) {
        return mapByEncoder(entity.getMapBy(), new ArrayList<>(), encoder, context);
    }

    protected MapByEncoder mapByEncoder(
            ResourceEntity<?> mapBy,
            List<DataReader> readerChain,
            Encoder encoder,
            ProcessingContext<?> context) {

        // map by id
        if (mapBy.isIdIncluded()) {
            validateLeafMapBy(mapBy);
            readerChain.add(mapBy.getAgEntity().getIdReader());

            return new MapByEncoder(
                    readerChain,
                    encoder,
                    true,
                    converters.getConverter(Object.class));
        }

        // map by property
        if (!mapBy.getAttributes().isEmpty()) {
            validateLeafMapBy(mapBy);

            Map.Entry<String, AgAttribute> attribute = mapBy.getAttributes().entrySet().iterator().next();
            readerChain.add(propertyFactory.getAttributeProperty(mapBy, attribute.getValue()).getReader());
            return new MapByEncoder(
                    readerChain,
                    encoder,
                    false,
                    converters.getConverter(attribute.getValue().getType()));
        }

        // descend into relationship
        if (!mapBy.getChildren().isEmpty()) {

            Map.Entry<String, RelatedResourceEntity<?>> child = mapBy.getChildren().entrySet().iterator().next();

            // TODO: to account for overlaid relationships (and avoid NPEs), we should not access agEntity...
            //  instead should look for incoming rel of a child ResourceEntity.. Is is present in MapBy case?
            AgRelationship relationship = mapBy.getChild(child.getKey()).getIncoming();
            readerChain.add(propertyFactory.getRelationshipProperty(mapBy, relationship, null, context).getReader());

            return mapByEncoder(mapBy.getChildren().get(child.getKey()), readerChain, encoder, context);
        }

        // map by relationship (implicitly by id)
        readerChain.add(mapBy.getAgEntity().getIdReader());

        return new MapByEncoder(
                readerChain,
                encoder,
                true,
                converters.getConverter(Object.class));
    }

    protected void validateLeafMapBy(ResourceEntity<?> mapBy) {

        if (!mapBy.getChildren().isEmpty()) {

            String pathSegment = (mapBy instanceof RelatedResourceEntity)
                    ? ((RelatedResourceEntity<?>) mapBy).getIncoming().getName()
                    : "";

            throw AgException.badRequest("'mapBy' path segment '%s' should not have children", pathSegment);
        }
    }
}
