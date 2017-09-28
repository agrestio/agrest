package com.nhl.link.rest.runtime.encoder;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nhl.link.rest.AggregationType;
import com.nhl.link.rest.EntityProperty;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.encoder.CollectionEncoder;
import com.nhl.link.rest.encoder.DataResponseEncoder;
import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.encoder.EncoderFilter;
import com.nhl.link.rest.encoder.EncoderVisitor;
import com.nhl.link.rest.encoder.EntityEncoder;
import com.nhl.link.rest.encoder.EntityMetadataEncoder;
import com.nhl.link.rest.encoder.EntityToOneEncoder;
import com.nhl.link.rest.encoder.FilterChainEncoder;
import com.nhl.link.rest.encoder.GenericEncoder;
import com.nhl.link.rest.encoder.ListEncoder;
import com.nhl.link.rest.encoder.MapByEncoder;
import com.nhl.link.rest.encoder.PropertyMetadataEncoder;
import com.nhl.link.rest.encoder.ResourceEncoder;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.property.PropertyBuilder;
import com.nhl.link.rest.runtime.semantics.IRelationshipMapper;
import org.apache.cayenne.di.Inject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class EncoderService implements IEncoderService {

    protected IAttributeEncoderFactory attributeEncoderFactory;
    protected IRelationshipMapper relationshipMapper;
    private IStringConverterFactory stringConverterFactory;
    private List<EncoderFilter> filters;
    private Map<String, PropertyMetadataEncoder> propertyMetadataEncoders;
    private Map<ResourceEntity<?>, Encoder> entityMetadataEncoders;

    public EncoderService(@Inject List<EncoderFilter> filters,
                          @Inject IAttributeEncoderFactory attributeEncoderFactory,
                          @Inject IStringConverterFactory stringConverterFactory, @Inject IRelationshipMapper relationshipMapper,
                          @Inject Map<String, PropertyMetadataEncoder> propertyMetadataEncoders) {
        this.attributeEncoderFactory = attributeEncoderFactory;
        this.relationshipMapper = relationshipMapper;
        this.stringConverterFactory = stringConverterFactory;
        this.filters = filters;
        this.propertyMetadataEncoders = propertyMetadataEncoders;
        this.entityMetadataEncoders = new ConcurrentHashMap<>();
    }

    @Override
    public <T> Encoder metadataEncoder(ResourceEntity<T> entity) {
        return new ResourceEncoder<>(entity, entity.getApplicationBase(), entityMetadataEncoder(entity));
    }

    @Override
    public <T> Encoder dataEncoder(ResourceEntity<T> entity) {
        CollectionEncoder resultEncoder = resultEncoder(entity);
        return new DataResponseEncoder("data", resultEncoder, "total", GenericEncoder.encoder());
    }

    protected CollectionEncoder resultEncoder(ResourceEntity<?> entity) {
        Encoder elementEncoder = collectionElementEncoder(entity);
        boolean isMapBy = entity.getMapBy() != null;

        // notice that we are not passing either qualifier or ordering to the
        // encoder, as those are presumably applied at the query level.. (unlike
        // with #nestedToManyEncoder)

        CollectionEncoder encoder = new ListEncoder(elementEncoder).withOffset(entity.getFetchOffset())
                .withLimit(entity.getFetchLimit()).shouldFilter(entity.isFiltered());

        return isMapBy
                ? new MapByEncoder(entity.getMapByPath(), null, entity.getMapBy(), encoder, stringConverterFactory, attributeEncoderFactory)
                : encoder;
    }

    protected Encoder nestedToManyEncoder(ResourceEntity<?> resourceEntity) {

        Encoder elementEncoder = collectionElementEncoder(resourceEntity);
        boolean isMapBy = resourceEntity.getMapBy() != null;

        // if mapBy is involved, apply filters at MapBy level, not inside
        // sublists...
        ListEncoder listEncoder = new ListEncoder(elementEncoder, isMapBy ? null : resourceEntity.getQualifier(),
                resourceEntity.getOrderings());

        listEncoder.withOffset(resourceEntity.getFetchOffset()).withLimit(resourceEntity.getFetchLimit());

        if (resourceEntity.isFiltered()) {
            listEncoder.shouldFilter();
        }

        return isMapBy ? new MapByEncoder(resourceEntity.getMapByPath(), resourceEntity.getQualifier(),
                resourceEntity.getMapBy(), listEncoder, stringConverterFactory, attributeEncoderFactory) : listEncoder;
    }

    protected Encoder collectionElementEncoder(ResourceEntity<?> resourceEntity) {
        Encoder encoder = entityEncoder(resourceEntity);
        return filteredEncoder(encoder, resourceEntity);
    }

    protected Encoder toOneEncoder(ResourceEntity<?> resourceEntity, LrRelationship relationship) {

        // to-one encoder is made of the following decorator layers (from outer
        // to inner):
        // (1) custom filters ->
        // (2) value encoder
        // different structure from to-many, so building it differently

        Encoder valueEncoder = entityEncoder(resourceEntity);
        Encoder compositeValueEncoder = new EntityToOneEncoder(valueEncoder);
        return filteredEncoder(compositeValueEncoder, resourceEntity);
    }

    protected Encoder entityMetadataEncoder(ResourceEntity<?> resourceEntity) {
        Encoder encoder = entityMetadataEncoders.get(resourceEntity);

        if (encoder == null) {
            encoder = new EntityMetadataEncoder(resourceEntity, propertyMetadataEncoders);
            entityMetadataEncoders.put(resourceEntity, encoder);
        }

        return encoder;
    }

    protected Encoder entityEncoder(ResourceEntity<?> resourceEntity) {

        // ensure we sort property encoders alphabetically for cleaner JSON
        // output
        Map<String, EntityProperty> attributeEncoders = new TreeMap<>();

        for (LrAttribute attribute : resourceEntity.getAttributes().values()) {
            EntityProperty property = attributeEncoderFactory.getAttributeProperty(resourceEntity.getLrEntity(),
                    attribute);
            attributeEncoders.put(attribute.getName(), property);
        }

        for (AggregationType aggregationType : AggregationType.values()) {
            resourceEntity.getAggregatedAttributes(aggregationType).forEach(attribute -> {
                EntityProperty property = attributeEncoderFactory.getAttributeProperty(resourceEntity.getLrEntity(),
                    attribute);
                String key = toFunctionName(aggregationType, attribute.getName());
                attributeEncoders.put(key, property);
            });
        }

        Map<String, EntityProperty> relationshipEncoders = new TreeMap<>();
        for (Entry<String, ResourceEntity<?>> e : resourceEntity.getChildren().entrySet()) {
            ResourceEntity<?> child = e.getValue();

            // TODO: same when the parent's parent is aggregate (need to pass context throughout the hierarchy)
            if (resourceEntity.isAggregate()) {
                String propertyName = e.getKey();
                relationshipEncoders.put(propertyName, new PropertyEncoder(entityEncoder(child), propertyName));

            } else {
                LrRelationship relationship = child.getIncoming();
                Encoder encoder = relationship.isToMany() ? nestedToManyEncoder(e.getValue())
                        : toOneEncoder(e.getValue(), relationship);
                EntityProperty property = attributeEncoderFactory.getRelationshipProperty(resourceEntity.getLrEntity(),
                        relationship, encoder);
                relationshipEncoders.put(e.getKey(), property);
            }
        }

        for (Entry<String, ResourceEntity<?>> e : resourceEntity.getAggregateChildren().entrySet()) {
            ResourceEntity<?> child = e.getValue();
            String propertyName = "@aggregated:" + e.getKey();
            relationshipEncoders.put(propertyName, new PropertyEncoder(entityEncoder(child), propertyName));
        }

        Map<String, EntityProperty> extraEncoders = new TreeMap<>();
        extraEncoders.putAll(resourceEntity.getExtraProperties());

        EntityProperty idEncoder = resourceEntity.isIdIncluded() ? attributeEncoderFactory.getIdProperty(resourceEntity)
                : PropertyBuilder.doNothingProperty();

        return new EntityEncoder(idEncoder, attributeEncoders, relationshipEncoders, extraEncoders);
    }

    protected Encoder filteredEncoder(Encoder encoder, ResourceEntity<?> resourceEntity) {
        List<EncoderFilter> matchingFilters = null;

        for (EncoderFilter filter : filters) {
            if (filter.matches(resourceEntity)) {
                if (matchingFilters == null) {
                    matchingFilters = new ArrayList<>(3);
                }

                matchingFilters.add(filter);
            }
        }

        return matchingFilters != null ? new FilterChainEncoder(encoder, matchingFilters) : encoder;
    }

    private static String toFunctionName(AggregationType aggregationType, String attributeName) {
        return aggregationType.functionName().toLowerCase() + "(" + attributeName + ")";
    }

    private static class PropertyEncoder implements EntityProperty {

        private Encoder encoder;
        private String name;

        public PropertyEncoder(Encoder encoder, String name) {
            this.encoder = encoder;
            this.name = name;
        }

        @Override
        public void encode(Object root, String propertyName, JsonGenerator out) throws IOException {
            encoder.encode(name, root, out);
        }

        @Override
        public Object read(Object root, String propertyName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int visit(Object object, String propertyName, EncoderVisitor visitor) {
            throw new UnsupportedOperationException();
        }
    }
}
