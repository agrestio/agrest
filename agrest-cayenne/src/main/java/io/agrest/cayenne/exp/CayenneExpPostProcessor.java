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
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.TraversalHandler;
import org.apache.cayenne.exp.parser.ASTDbPath;
import org.apache.cayenne.exp.parser.ASTObjPath;
import org.apache.cayenne.exp.parser.ASTPath;
import org.apache.cayenne.exp.parser.ConditionNode;
import org.apache.cayenne.exp.parser.SimpleNode;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.map.ObjEntity;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CayenneExpPostProcessor implements ICayenneExpPostProcessor {

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

        // change expression in-place
        // note - this will not fully handle an expression whose root is
        // ASTObjPath, so will manually process it below
        exp.traverse(getOrCreateExpressionProcessor(entity));

        // process root ASTObjPath that can't be properly handled by
        // 'expressionPostProcessor'. If it happens to be "id", it will be
        // converted to "db:id".
        if (exp instanceof ASTObjPath) {
            exp = pathCache.resolve(entity.getName(), ((ASTObjPath) exp).getPath().value()).getPathExp();
        }

        return exp;
    }

    private ExpressionProcessor getOrCreateExpressionProcessor(ObjEntity entity) {
        return postProcessors.computeIfAbsent(entity.getName(), e -> new ExpressionProcessor(entity));
    }

    private class ExpressionProcessor implements TraversalHandler {

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
            if (childNode instanceof ASTObjPath) {

                // validate and replace if needed ... note that we can only
                // replace non-root nodes during the traversal. Root node is
                // validated and replaced explicitly by the caller.
                ASTPath replacement = pathCache.resolve(entity.getName(), ((ASTObjPath) childNode).getPath().value()).getPathExp();
                if (replacement != childNode) {
                    parentNode.setOperand(childIndex, replacement);
                }
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
                return ((ASTObjPath) exp).getPath().value();
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
}
