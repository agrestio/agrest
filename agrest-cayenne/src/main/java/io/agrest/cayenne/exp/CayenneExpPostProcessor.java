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
import org.apache.cayenne.exp.ConditionExp;
import org.apache.cayenne.exp.DbPathExp;
import org.apache.cayenne.exp.ExistsExp;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.exp.NotExistsExp;
import org.apache.cayenne.exp.ObjPathExp;
import org.apache.cayenne.exp.PathExp;
import org.apache.cayenne.exp.SubqueryExp;
import org.apache.cayenne.exp.TraversalHandler;
import org.apache.cayenne.map.DbJoin;
import org.apache.cayenne.map.DbRelationship;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.apache.cayenne.query.FluentSelect;
import org.apache.cayenne.query.ObjectSelect;

import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CayenneExpPostProcessor implements ICayenneExpPostProcessor {

    private static final String EMPTY_PATH = "";

    private final EntityResolver entityResolver;
    private final IPathResolver pathCache;
    private final Map<String, JsonValueConverter<?>> converters;

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
    }

    @Override
    public Expression process(String entityName, Expression exp) {
        return exp == null ? null : validateAndCleanup(entityResolver.getObjEntity(entityName), exp);
    }

    private Expression validateAndCleanup(ObjEntity entity, Expression exp) {

        // Normalize paths in-place. Will not be able to change the root note that is handled explicitly below.
        // The processor keeps the traversal state, so a new instance is needed per expression
        exp.traverse(new ExpressionProcessor(entity));

        // process root ExistsExp|NotExistsExp that can't be properly handled by ExpressionProcessor.
        Object processed = processExistsExp(exp);

        // Now process the root
        return (Expression) normalizeIfPath(entity, processed);
    }

    private static Object processExistsExp(Object exp) {
        if (!(exp instanceof ExistsExp || exp instanceof NotExistsExp)) {
            return exp;
        }
        Expression pathExistExp = (Expression) (((Expression) exp).getOperand(0));
        if (pathExistExp instanceof SubqueryExp) {
            return exp;
        }
        return exp instanceof ExistsExp
                ? pathExistExp
                : pathExistExp.notExp();
    }

    private Object normalizeIfPath(ObjEntity entity, Object expNode) {
        if (expNode instanceof ObjPathExp path) {
            PathDescriptor resolve = pathCache.resolve(entity.getName(), path.getPath().value(), path.getPathAliases());
            return resolve.getPathExp();
        }
        return expNode;
    }

    private class ExpressionProcessor implements TraversalHandler {

        private final ObjEntity entity;

        // Cayenne expression nodes don't know their parents, so we track the ancestors of the current node
        // ourselves. The head of the deque is the node being traversed.
        private final Deque<Expression> ancestors;

        ExpressionProcessor(ObjEntity entity) {
            this.entity = entity;
            this.ancestors = new ArrayDeque<>();
        }

        @Override
        public void startNode(Expression node, Expression parentNode) {
            if (node instanceof DbPathExp) {
                // probably a good idea to disallow DbPath's
                throw AgException.badRequest(
                        "Expression contains a DB_PATH expression that is not allowed here: %s",
                        parentNode);
            }
            ancestors.push(node);
        }

        @Override
        public void endNode(Expression node, Expression parentNode) {
            ancestors.pop();
        }

        @Override
        public void finishedChild(Expression parentNode, int childIndex, boolean hasMoreChildren) {
            Object childNode = parentNode.getOperand(childIndex);
            Object replacementNode = normalizeIfPath(entity, childNode);
            replacementNode = processExistsExp(replacementNode);
            if (replacementNode != childNode) {
                parentNode.setOperand(childIndex, replacementNode);
            }
            prepareExistsPathOperand(parentNode, childNode);
        }

        private void prepareExistsPathOperand(Expression parentNode, Object childNode) {
            if (!(parentNode instanceof ExistsExp || parentNode instanceof NotExistsExp)) {
                return;
            }
            if (!(childNode instanceof PathExp path)) {
                throw AgException.badRequest("%s only supports path value as an argument", parentNode.expName());
            }
            PathMarker marker = createPathMarker(entity, path);
            Expression pathExistExp = markerToExpression(marker);
            parentNode.setOperand(0, marker.relationship() != null
                    ? new SubqueryExp(subquery(marker.relationship(), pathExistExp))
                    : pathExistExp);
        }

        @Override
        public void objectNode(Object leaf, Expression parentNode) {

            switch (leaf) {
                case JsonNode node -> replaceOperand(parentNode, leaf, convert(parentNode, node));

                // this is ListExp child case
                case Object[] array -> {
                    for (int i = 0; i < array.length; i++) {
                        if (array[i] instanceof JsonNode node) {
                            array[i] = convert(parentNode, node);
                        }
                    }
                }
                case String s -> replaceOperand(parentNode, leaf, convert(parentNode, TextNode.valueOf(s)));
                case null, default -> {
                }
            }
        }

        private void replaceOperand(Expression parentNode, Object leaf, Object replacement) {
            for (int i = 0; i < parentNode.getOperandCount(); i++) {
                if (leaf == parentNode.getOperand(i)) {
                    parentNode.setOperand(i, replacement);
                }
            }
        }

        private Object convert(Expression parentExp, JsonNode node) {

            ObjPathExp peerPath = findPeerPath(parentExp, node);

            if (peerPath != null) {

                PathDescriptor pd = pathCache.resolve(entity.getName(), peerPath.getPath().value(), peerPath.getPathAliases());
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

        private PathMarker createPathMarker(ObjEntity entity, PathExp o) {
            String path = o.getPath().value();
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
            return new PathMarker(newPath, relationship);
        }

        private Expression markerToExpression(PathMarker marker) {
            // special case for an empty path
            // we don't need additional qualifier, just plain exists subquery
            if (marker.path().equals(EMPTY_PATH)) {
                return null;
            }
            return ExpressionFactory.noMatchExp(new ObjPathExp(marker.path()), null);
        }

        private FluentSelect<?, ?> subquery(ObjRelationship relationship, Expression exp) {
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

        /**
         * Walks up the ancestors of the "child" value until the closest condition node, and then searches the
         * condition's other branches for a path. Starts with the "exp" node, which must be the current head of the
         * ancestors deque.
         */
        private ObjPathExp findPeerPath(Expression exp, Object child) {

            Object current = child;
            for (Expression ancestor : ancestors) {

                if (!(ancestor instanceof ConditionExp)) {
                    current = ancestor;
                    continue;
                }

                // terminate walk up at a ConditionExp, start a walk down
                int len = ancestor.getOperandCount();
                for (int i = 0; i < len; i++) {
                    Object operand = ancestor.getOperand(i);
                    if (operand == current || !(operand instanceof Expression)) {
                        continue;
                    }

                    ObjPathExp path = findChildPath((Expression) operand);
                    if (path != null) {
                        return path;
                    }
                }

                return null;
            }

            return null;
        }

        private ObjPathExp findChildPath(Expression exp) {
            if (exp instanceof ObjPathExp objPath) {
                return objPath;
            }

            int len = exp.getOperandCount();
            for (int i = 0; i < len; i++) {
                Object operand = exp.getOperand(i);
                if (!(operand instanceof Expression expression)) {
                    continue;
                }

                ObjPathExp path = findChildPath(expression);
                if (path != null) {
                    return path;
                }
            }

            return null;
        }
    }

    /**
     * A path with its first segment split off as a relationship, when the first segment is one.
     */
    record PathMarker(String path, ObjRelationship relationship) {
    }
}
