package io.agrest.meta.cayenne;

import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityBuilder;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.meta.AgRelationship;
import io.agrest.meta.DefaultAgEntity;
import io.agrest.meta.compiler.AgEntityCompiler;
import io.agrest.meta.compiler.LazyAgEntity;
import io.agrest.runtime.cayenne.ICayennePersister;
import io.agrest.runtime.parser.converter.IJsonValueConverterFactory;
import org.apache.cayenne.dba.TypesMapping;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.map.DbAttribute;
import org.apache.cayenne.map.DbRelationship;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static io.agrest.meta.Types.typeForName;

/**
 * @since 1.24
 */
public class CayenneEntityCompiler implements AgEntityCompiler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CayenneEntityCompiler.class);

    private EntityResolver resolver;
    private Map<String, AgEntityOverlay> entityOverlays;
    private IJsonValueConverterFactory converterFactory;

    public CayenneEntityCompiler(
            @Inject ICayennePersister cayennePersister,
            @Inject Map<String, AgEntityOverlay> entityOverlays,
            @Inject IJsonValueConverterFactory converterFactory) {

        this.resolver = cayennePersister.entityResolver();
        this.entityOverlays = entityOverlays;
        this.converterFactory = converterFactory;
    }

    @Override
    public <T> AgEntity<T> compile(Class<T> type, AgDataMap dataMap) {

        ObjEntity objEntity = resolver.getObjEntity(type);
        if (objEntity == null) {
            return null;
        }
        return new LazyAgEntity<>(type, () -> doCompile(type, dataMap));
    }

    private <T> AgEntity<T> doCompile(Class<T> type, AgDataMap dataMap) {

        LOGGER.debug("compiling Cayenne entity for type: " + type);

        ObjEntity objEntity = resolver.getObjEntity(type);
        DefaultAgEntity<T> agEntity = new DefaultAgEntity<>(objEntity.getName(), type);
        loadCayenneEntity(agEntity, objEntity, dataMap);
        loadAnnotatedProperties(agEntity, dataMap);
        loadOverlays(dataMap, agEntity);
        return agEntity;
    }

    protected <T> void loadCayenneEntity(DefaultAgEntity<T> agEntity, ObjEntity objEntity, AgDataMap dataMap) {

        for (ObjAttribute a : objEntity.getAttributes()) {
            Class<?> type = typeForName(a.getType());
            CayenneAgObjAttribute agAttribute = new CayenneAgObjAttribute(a, type);
            agEntity.addAttribute(agAttribute);
        }

        for (ObjRelationship r : objEntity.getRelationships()) {
            List<DbRelationship> dbRelationshipsList = r.getDbRelationships();

            Class<?> targetEntityType = resolver.getClassDescriptor(r.getTargetEntityName()).getObjectClass();
            AgEntity<?> targetEntity = dataMap.getEntity(targetEntityType);

            // take last element from list of db relationships
            // in order to behave correctly if
            // db entities are connected through intermediate tables
            DbRelationship targetRelationship = dbRelationshipsList.get(dbRelationshipsList.size() - 1);
            int targetJdbcType = targetRelationship.getJoins().get(0).getTarget().getType();
            Class<?> type = typeForName(TypesMapping.getJavaBySqlType(targetJdbcType));

            AgRelationship agRelationship = new CayenneAgRelationship(r, targetEntity, converterFactory.converter(type));
            agEntity.addRelationship(agRelationship);
        }

        for (DbAttribute pk : objEntity.getDbEntity().getPrimaryKeys()) {
            ObjAttribute attribute = objEntity.getAttributeForDbAttribute(pk);
            Class<?> type;
            AgAttribute id;
            if (attribute == null) {
                type = typeForName(TypesMapping.getJavaBySqlType(pk.getType()));
                id = new CayenneAgDbAttribute(pk.getName(), pk, type);
            } else {
                type = typeForName(attribute.getType());
                id = new CayenneAgObjAttribute(attribute, type);
            }
            agEntity.addId(id);
        }

    }

    protected <T> void loadAnnotatedProperties(DefaultAgEntity<T> entity, AgDataMap dataMap) {

        // load a separate entity built purely from annotations, then merge it
        // with our entity... Note that we are not cloning attributes or
        // relationship during merge... they have no references to parent and
        // can be used as is.

        AgEntity<T> annotatedEntity = new AgEntityBuilder<>(entity.getType(), dataMap).build();

        if (annotatedEntity.getIds().size() > 0) {
            for (AgAttribute id : annotatedEntity.getIds()) {

                // there is a good chance a designated ID attribute is also a regular persistent attribute.. so make
                // sure we replace non-persistent with persistent...

                AgAttribute existingNonId = entity.getAttribute(id.getName());
                AgAttribute existing = entity.addId(existingNonId != null ? existingNonId : id);

                if (existing != null && LOGGER.isDebugEnabled()) {
                    LOGGER.debug("ID attribute '" + existing.getName() + "' is overridden from annotations.");
                }
            }

            // remove Cayenne-mandated ids
            Iterator<AgAttribute> it = entity.getIds().iterator();
            while (it.hasNext()) {
                AgAttribute id = it.next();
                if (annotatedEntity.getId(id.getName()) == null) {
                    it.remove();
                }
            }
        }

        for (AgAttribute attribute : annotatedEntity.getAttributes()) {

            AgAttribute existing = entity.addAttribute(attribute);
            if (existing != null && LOGGER.isDebugEnabled()) {
                LOGGER.debug("Attribute '" + existing.getName() + "' is overridden from annotations.");
            }
        }

        for (AgRelationship relationship : annotatedEntity.getRelationships()) {

            AgRelationship existing = entity.addRelationship(relationship);
            if (existing != null && LOGGER.isDebugEnabled()) {
                LOGGER.debug("Relationship '" + existing.getName() + "' is overridden from annotations.");
            }
        }
    }

    protected <T> void loadOverlays(AgDataMap dataMap, DefaultAgEntity<T> entity) {
        AgEntityOverlay<?> overlay = entityOverlays.get(entity.getType().getName());
        if (overlay != null) {
            overlay.getAttributes().forEach(entity::addAttribute);
            overlay.getRelatonships(dataMap).forEach(entity::addRelationship);
        }
    }

}
