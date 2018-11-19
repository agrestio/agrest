package io.agrest.runtime.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.agrest.AgException;
import io.agrest.backend.exp.Expression;
import io.agrest.backend.exp.TraversalHelper;
import io.agrest.backend.exp.parser.ASTDbPath;
import io.agrest.backend.exp.parser.ASTObjPath;
import io.agrest.backend.exp.parser.ASTPath;
import io.agrest.backend.exp.parser.ConditionNode;
import io.agrest.backend.exp.parser.SimpleNode;
import io.agrest.meta.AgEntity;
import io.agrest.parser.converter.JsonValueConverter;
import io.agrest.parser.converter.UtcDateConverter;
import io.agrest.runtime.path.IPathDescriptorManager;
import io.agrest.runtime.path.PathDescriptor;
import org.apache.cayenne.di.Inject;

import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExpressionPostProcessor implements IExpressionPostProcessor {

    private IPathDescriptorManager pathCache;
    private Map<Class<?>, JsonValueConverter<?>> converters;

    private Map<AgEntity<?>, ExpressionProcessor> postProcessors;

    public ExpressionPostProcessor(@Inject IPathDescriptorManager pathCache) {
        this.pathCache = pathCache;

        // TODO: instead of manually assembling converters we must switch to
        // IJsonValueConverterFactory already used by DataObjectProcessor.
        // The tricky part is the "id" attribute that is converted to DbPath
        // during CayenneExpProcessorWorker traversal, so its type can not be
        // mapped with existing tools
        Map<Class<?>, JsonValueConverter<?>> converters = new HashMap<>();
        converters.put(Date.class, UtcDateConverter.converter());
        converters.put(java.sql.Date.class, UtcDateConverter.converter());
        converters.put(java.sql.Time.class, UtcDateConverter.converter());
        converters.put(java.sql.Timestamp.class, UtcDateConverter.converter());
        this.converters = converters;

        postProcessors = new ConcurrentHashMap<>();
    }

    @Override
    public Expression process(AgEntity<?> entity, Expression exp) {
        return (exp == null) ? null : validateAndCleanup(entity, exp);
    }

    private Expression validateAndCleanup(AgEntity<?> entity, Expression exp) {

        // change expression in-place
        // note - this will not fully handle an expression whose root is
        // ASTObjPath, so will manually process it below
        exp.traverse(getOrCreateExpressionProcessor(entity));

        // process root ASTObjPath that can't be properly handled by
        // 'expressionPostProcessor'. If it happens to be "id", it will be
        // converted to "db:id".
        if (exp instanceof ASTObjPath) {
            exp = pathCache.getPathDescriptor(entity, (ASTObjPath) exp).getPathExp();
        }

        return exp;
    }

    private ExpressionProcessor getOrCreateExpressionProcessor(AgEntity<?> entity) {

        ExpressionProcessor postProcessor = postProcessors.get(entity);
        if (postProcessor == null) {
            postProcessor = new ExpressionProcessor(entity);
            ExpressionProcessor existing = postProcessors.putIfAbsent(entity, postProcessor);
            if (existing != null) {
                postProcessor = existing;
            }
        }
        return postProcessor;
    }

    private class ExpressionProcessor extends TraversalHelper {

        private AgEntity<?> entity;

        ExpressionProcessor(AgEntity<?> entity) {
            this.entity = entity;
        }

        @Override
        public void startNode(Expression node, Expression parentNode) {
            if (node instanceof ASTDbPath) {
                // probably a good idea to disallow DbPath's
                throw new AgException(Response.Status.BAD_REQUEST,
                        "AgExpression contains a DB_PATH expression that is not allowed here: " + parentNode);
            }

        }

        @Override
        public void finishedChild(Expression parentNode, int childIndex, boolean hasMoreChildren) {

            Object childNode = parentNode.getOperand(childIndex);
            if (childNode instanceof ASTObjPath) {

                // validate and replace if needed ... note that we can only
                // replace non-root nodes during the traversal. Root node is
                // validated and replaced explicitly by the caller.
                ASTPath replacement = pathCache.getPathDescriptor(entity, (ASTObjPath) childNode).getPathExp();
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
            }
            else if (leaf instanceof String) {
                for (int i = 0; i < parentNode.getOperandCount(); i++) {
                    if (leaf == parentNode.getOperand(i)) {
                        parentNode.setOperand(i, convert((SimpleNode) parentNode, TextNode.valueOf((String) leaf)));
                    }
                }
            }
        }

        private Object convert(SimpleNode parentExp, JsonNode node) {

            ASTObjPath peerPath = findPeerPath(parentExp, node);

            if (peerPath != null) {

                PathDescriptor pd = pathCache.getPathDescriptor(entity, peerPath);
                if (pd.isAttribute()) {
                    JsonValueConverter<?> converter = converters.get(pd.getType());
                    if (converter != null) {

                        try {
                            return converter.value(node);
                        } catch (Exception e) {
                            throw new AgException(Response.Status.BAD_REQUEST,
                                    "AgExpression parameters contain an incorrectly formatted value: '" + node.asText() + "'", e);
                        }
                    }
                }
            }

            return node.asText();
        }

        private ASTObjPath findPeerPath(SimpleNode exp, Object child) {

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

                ASTObjPath path = findChildPath((Expression) operand);
                if (path != null) {
                    return path;
                }
            }

            return null;
        }

        private ASTObjPath findChildPath(Expression exp) {
            if (exp instanceof ASTObjPath) {
                return (ASTObjPath) exp;
            }

            int len = exp.getOperandCount();
            for (int i = 0; i < len; i++) {
                Object operand = exp.getOperand(i);
                if (!(operand instanceof Expression)) {
                    continue;
                }

                ASTObjPath path = findChildPath((Expression) operand);
                if (path != null) {
                    return path;
                }
            }

            return null;
        }
    }
}
