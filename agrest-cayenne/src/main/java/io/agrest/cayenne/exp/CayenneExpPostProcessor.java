package io.agrest.cayenne.exp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.agrest.AgException;
import io.agrest.cayenne.path.IPathResolver;
import io.agrest.cayenne.path.PathDescriptor;
import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.converter.jsonvalue.JsonValueConverter;
import io.agrest.converter.jsonvalue.SqlDateConverter;
import io.agrest.converter.jsonvalue.SqlTimeConverter;
import io.agrest.converter.jsonvalue.SqlTimestampConverter;
import io.agrest.converter.jsonvalue.UtilDateConverter;
import org.apache.cayenne.Persistent;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.exp.TraversalHelper;
import org.apache.cayenne.exp.parser.ASTDbPath;
import org.apache.cayenne.exp.parser.ASTExists;
import org.apache.cayenne.exp.parser.ASTNotExists;
import org.apache.cayenne.exp.parser.ASTObjPath;
import org.apache.cayenne.exp.parser.ASTPath;
import org.apache.cayenne.exp.parser.ASTSubquery;
import org.apache.cayenne.exp.parser.ConditionNode;
import org.apache.cayenne.exp.parser.Node;
import org.apache.cayenne.exp.parser.SimpleNode;
import org.apache.cayenne.map.DbJoin;
import org.apache.cayenne.map.DbRelationship;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.apache.cayenne.query.FluentSelect;
import org.apache.cayenne.query.ObjectSelect;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CayenneExpPostProcessor implements ICayenneExpPostProcessor {

    private static final String EMPTY_PATH = "";

    private final EntityResolver entityResolver;
    private final IPathResolver pathCache;
    private final Map<String, JsonValueConverter<?>> converters;
    private final Map<String, ExpressionProcessor> postProcessors;

    public CayenneExpPostProcessor(
            @Inject IPathResolver pathCache,
            @Inject ICayennePersister persister) {

        this.pathCache = pathCache;
        this.entityResolver = persister.entityResolver();

        // TODO: instead of manually assembling converters we must switch to
        //  IJsonValueConverterFactory already used by DataObjectProcessor.
        //  The tricky part is the "id" attribute that is converted to DbPath
        //  , so its type can not be mapped with existing tools
        this.converters = new HashMap<>();
        converters.put(Date.class.getName(), UtilDateConverter.converter());
        converters.put(java.sql.Date.class.getName(), SqlDateConverter.converter());
        converters.put(java.sql.Time.class.getName(), SqlTimeConverter.converter());
        converters.put(java.sql.Timestamp.class.getName(), SqlTimestampConverter.converter());

        postProcessors = new ConcurrentHashMap<>();
    }

    @Override
    public Expression process(String entityName, Expression exp) {
        return exp == null ? null : validateAndCleanup(entityResolver.getObjEntity(entityName), exp);
    }

    private Expression validateAndCleanup(ObjEntity entity, Expression exp) {

        // Normalize paths in-place. Will not be able to change the root note that is handled explicitly below
        exp.traverse(getOrCreateExpressionProcessor(entity));

        // process root ASTExits|ASTNotExists that can't be properly handled by ExpressionProcessor.
        if (exp instanceof ASTExists || exp instanceof ASTNotExists) {
            exp = optimizeExistsExp(exp);
        }

        // Now process the root
        return (Expression) normalizeIfPath(entity, exp);
    }

    private ExpressionProcessor getOrCreateExpressionProcessor(ObjEntity entity) {
        return postProcessors.computeIfAbsent(entity.getName(), e -> new ExpressionProcessor(entity));
    }

    private static Expression optimizeExistsExp(Expression exp) {
        Expression pathExistExp = ((Expression) exp.getOperand(0));
        if (pathExistExp instanceof ASTSubquery) {
            return exp;
        }
        return exp instanceof ASTExists
                ? pathExistExp
                : pathExistExp.notExp();
    }
  
    private Object normalizeIfPath(ObjEntity entity, Object expNode) {
        return expNode instanceof ASTObjPath
                ? pathCache.resolve(entity.getName(), ((ASTObjPath) expNode).getPath()).getPathExp()
                : expNode;
    }

    private class ExpressionProcessor extends TraversalHelper {

        private final ObjEntity entity;

        ExpressionProcessor(ObjEntity entity) {
            this.entity = entity;
        }

        @Override
        public void startNode(Expression node, Expression parentNode) {
            if (node instanceof ASTDbPath) {
                // probably a good idea to disallow DbPath's
                throw AgException.badRequest(
                        "Expression contains a DB_PATH expression that is not allowed here: %s",
                        parentNode);
            }
        }

        @Override
        public void finishedChild(Expression parentNode, int childIndex, boolean hasMoreChildren) {
            Object childNode = parentNode.getOperand(childIndex);
            Object replacementNode = normalizeIfPath(entity, childNode);
            if (replacementNode != childNode) {
                parentNode.setOperand(childIndex, replacementNode);
            }
            if (parentNode instanceof ASTExists || parentNode instanceof ASTNotExists) {
                if (!(childNode instanceof ASTPath)) {
                    throw AgException.badRequest("%s only supports path value", parentNode.expName());
                }
                ObjPathMarker marker = createPathMarker(entity, (ASTPath) childNode);
                Expression pathExistExp = markerToExpression(marker);
                ((ConditionNode) parentNode).jjtAddChild(
                        marker.relationship != null
                                ? new ASTSubquery(subquery(marker.relationship, pathExistExp))
                                : (Node) pathExistExp,
                        childIndex
                );
            }
            if (childNode instanceof ASTExists || childNode instanceof ASTNotExists) {
                parentNode.setOperand(childIndex, optimizeExistsExp((Expression) childNode));
            }
        }

        @Override
        public void objectNode(Object leaf, Expression parentNode) {

            if (leaf instanceof JsonNode) {
                for (int i = 0; i < parentNode.getOperandCount(); i++) {
                    if (leaf == parentNode.getOperand(i)) {
                        parentNode.setOperand(i, convert((SimpleNode) parentNode, (JsonNode) leaf));
                    }
                }
            }
            // this is ASTList child case
            else if (leaf instanceof Object[]) {

                Object[] array = (Object[]) leaf;
                for (int i = 0; i < array.length; i++) {
                    if (array[i] instanceof JsonNode) {
                        array[i] = convert((SimpleNode) parentNode, (JsonNode) array[i]);
                    }
                }
            } else if (leaf instanceof String) {
                for (int i = 0; i < parentNode.getOperandCount(); i++) {
                    if (leaf == parentNode.getOperand(i)) {
                        parentNode.setOperand(i, convert((SimpleNode) parentNode, TextNode.valueOf((String) leaf)));
                    }
                }
            }
        }

        private Object convert(SimpleNode parentExp, JsonNode node) {

            String peerPath = findPeerPath(parentExp, node);

            if (peerPath != null) {

                PathDescriptor pd = pathCache.resolve(entity.getName(), peerPath);
                if (pd.isAttributeOrId()) {
                    JsonValueConverter<?> converter = converters.get(pd.getType());
                    if (converter != null) {

                        try {
                            return converter.value(node);
                        } catch (Exception e) {
                            throw AgException.badRequest(
                                    e, "Expression parameters contain an incorrectly formatted value: '" + node.asText() + "'");
                        }
                    }
                }
            }

            return node.asText();
        }

        private ObjPathMarker createPathMarker(ObjEntity entity, ASTPath o) {
            String path = o.getPath();
            String newPath;
            String firstSegment;
            int dotIndex = path.indexOf(".");
            if (dotIndex == -1) {
                firstSegment = path;
                newPath = EMPTY_PATH;
            } else {
                firstSegment = path.substring(0, dotIndex);
                newPath = path.substring(dotIndex + 1);
            }
            // mark relationship that this path relates to and transform path
            ObjRelationship relationship = entity.getRelationship(firstSegment);
            if (relationship == null) {
                newPath = path;
            }
            return new ObjPathMarker(newPath, relationship);
        }

        private Expression markerToExpression(ObjPathMarker marker) {
            // special case for an empty path
            // we don't need additional qualifier, just plain exists subquery
            if (marker.getPath().equals(EMPTY_PATH)) {
                return null;
            }
            return ExpressionFactory.noMatchExp(marker, null);
        }

        private FluentSelect<?> subquery(ObjRelationship relationship, Expression exp) {
            List<DbRelationship> dbRelationships = relationship.getDbRelationships();
            for (DbRelationship dbRelationship : dbRelationships) {
                for (DbJoin join : dbRelationship.getJoins()) {
                    Expression joinMatchExp = ExpressionFactory.matchDbExp(join.getTargetName(),
                            ExpressionFactory.enclosingObjectExp(ExpressionFactory.dbPathExp(join.getSourceName())));
                    if (exp == null) {
                        exp = joinMatchExp;
                    } else {
                        exp = exp.andExp(joinMatchExp);
                    }
                }
            }
            return ObjectSelect.query(Persistent.class)
                    .dbEntityName(dbRelationships.get(0).getTargetEntityName())
                    .where(exp);
        }

        private String findPeerPath(SimpleNode exp, Object child) {

            if (exp == null) {
                return null;
            }

            if (!(exp instanceof ConditionNode)) {
                return findPeerPath((SimpleNode) exp.jjtGetParent(), exp);
            }

            // terminate walk up at a ConditionNode, start a walk down
            int len = exp.getOperandCount();
            for (int i = 0; i < len; i++) {
                Object operand = exp.getOperand(i);
                if (operand == child || !(operand instanceof Expression)) {
                    continue;
                }

                String path = findChildPath((Expression) operand);
                if (path != null) {
                    return path;
                }
            }

            return null;
        }

        private String findChildPath(Expression exp) {
            if (exp instanceof ASTObjPath) {
                return ((ASTObjPath) exp).getPath();
            }

            int len = exp.getOperandCount();
            for (int i = 0; i < len; i++) {
                Object operand = exp.getOperand(i);
                if (!(operand instanceof Expression)) {
                    continue;
                }

                String path = findChildPath((Expression) operand);
                if (path != null) {
                    return path;
                }
            }

            return null;
        }
    }

    static class ObjPathMarker extends ASTObjPath {

        final ObjRelationship relationship;

        ObjPathMarker(String path, ObjRelationship relationship) {
            super(path);
            this.relationship = relationship;
        }
    }
}
