package io.agrest.runtime.encoder;

import io.agrest.EntityProperty;
import io.agrest.ResourceEntity;
import io.agrest.backend.util.converter.ExpressionConverter;
import io.agrest.backend.util.converter.ExpressionMatcher;
import io.agrest.backend.util.converter.OrderingConverter;
import io.agrest.backend.util.converter.OrderingSorter;
import io.agrest.encoder.CollectionEncoder;
import io.agrest.encoder.DataResponseEncoder;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.EncoderFilter;
import io.agrest.encoder.EntityEncoder;
import io.agrest.encoder.EntityMetadataEncoder;
import io.agrest.encoder.EntityToOneEncoder;
import io.agrest.encoder.FilterChainEncoder;
import io.agrest.encoder.GenericEncoder;
import io.agrest.encoder.ListEncoder;
import io.agrest.encoder.MapByEncoder;
import io.agrest.encoder.PropertyMetadataEncoder;
import io.agrest.encoder.ResourceEncoder;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgRelationship;
import io.agrest.property.PropertyBuilder;
import io.agrest.runtime.semantics.IRelationshipMapper;
import org.apache.cayenne.di.Inject;

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
    private ExpressionConverter expressionConverter;
    private ExpressionMatcher expressionMatcher;
    private OrderingConverter orderingConverter;
    private OrderingSorter orderingSorter;

    public EncoderService(@Inject List<EncoderFilter> filters,
                          @Inject IAttributeEncoderFactory attributeEncoderFactory,
                          @Inject IStringConverterFactory stringConverterFactory,
                          @Inject IRelationshipMapper relationshipMapper,
                          @Inject Map<String, PropertyMetadataEncoder> propertyMetadataEncoders,
                          @Inject ExpressionConverter expressionConverter,
                          @Inject ExpressionMatcher expressionMatcher,
                          @Inject OrderingConverter orderingConverter,
                          @Inject OrderingSorter orderingSorter) {
        this.attributeEncoderFactory = attributeEncoderFactory;
        this.relationshipMapper = relationshipMapper;
        this.stringConverterFactory = stringConverterFactory;
        this.filters = filters;
        this.propertyMetadataEncoders = propertyMetadataEncoders;
        this.entityMetadataEncoders = new ConcurrentHashMap<>();
        this.expressionConverter = expressionConverter;
        this.expressionMatcher = expressionMatcher;
        this.orderingConverter = orderingConverter;
        this.orderingSorter = orderingSorter;
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

        // notice that we are not passing either qualifier or ordering to the encoder, as those are presumably applied
        // at the query level.. (unlike with #nestedToManyEncoder)

        CollectionEncoder encoder = new ListEncoder(elementEncoder)
                .withOffset(entity.getFetchOffset())
                .withLimit(entity.getFetchLimit())
                .shouldFilter(entity.isFiltered());

        return isMapBy ?
                new MapByEncoder(
                        entity.getMapByPath(),
                        null,
                        entity.getMapBy(),
                        encoder,
                        stringConverterFactory,
                        attributeEncoderFactory)
                : encoder;
    }

    protected Encoder nestedToManyEncoder(ResourceEntity<?> resourceEntity) {

        Encoder elementEncoder = collectionElementEncoder(resourceEntity);
        boolean isMapBy = resourceEntity.getMapBy() != null;
        boolean isQualifier = resourceEntity.getQualifier() != null;

        // if mapBy is involved, apply filters at MapBy level, not inside sublists...
        ListEncoder listEncoder = new ListEncoder(
                elementEncoder,
                isMapBy ? null : isQualifier ? expressionMatcher.match(expressionConverter.apply(resourceEntity.getQualifier())) : null,
                orderingSorter.sort(orderingConverter.apply(resourceEntity.getOrderings())))
                .withOffset(resourceEntity.getFetchOffset())
                .withLimit(resourceEntity.getFetchLimit());

        if (resourceEntity.isFiltered()) {
            listEncoder.shouldFilter();
        }

        return isMapBy ?
                new MapByEncoder(
                        resourceEntity.getMapByPath(),
                        isQualifier ? expressionMatcher.match(expressionConverter.apply(resourceEntity.getQualifier())) : null,
                        resourceEntity.getMapBy(),
                        listEncoder,
                        stringConverterFactory,
                        attributeEncoderFactory)
                : listEncoder;
    }

    protected Encoder collectionElementEncoder(ResourceEntity<?> resourceEntity) {
        Encoder encoder = entityEncoder(resourceEntity);
        return filteredEncoder(encoder, resourceEntity);
    }

    protected Encoder toOneEncoder(ResourceEntity<?> resourceEntity, AgRelationship relationship) {

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
        Map<String, EntityProperty> attributeEncoders = new TreeMap<String, EntityProperty>();

        for (AgAttribute attribute : resourceEntity.getAttributes().values()) {
            EntityProperty property = attributeEncoderFactory.getAttributeProperty(resourceEntity.getAgEntity(),
                    attribute);
            attributeEncoders.put(attribute.getName(), property);
        }

        Map<String, EntityProperty> relationshipEncoders = new TreeMap<String, EntityProperty>();
        for (Entry<String, ResourceEntity<?>> e : resourceEntity.getChildren().entrySet()) {
            AgRelationship relationship = resourceEntity.getAgEntity().getRelationship(e.getKey());

            Encoder encoder = relationship.isToMany() ? nestedToManyEncoder(e.getValue())
                    : toOneEncoder(e.getValue(), relationship);

            EntityProperty property = attributeEncoderFactory.getRelationshipProperty(resourceEntity.getAgEntity(),
                    relationship, encoder);
            relationshipEncoders.put(e.getKey(), property);
        }

        Map<String, EntityProperty> extraEncoders = new TreeMap<String, EntityProperty>();

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

}
