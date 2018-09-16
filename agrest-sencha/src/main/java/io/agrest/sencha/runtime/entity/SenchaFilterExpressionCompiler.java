package io.agrest.sencha.runtime.entity;

import io.agrest.AgException;
import io.agrest.meta.AgEntity;
import io.agrest.runtime.entity.IExpressionPostProcessor;
import io.agrest.runtime.path.IPathDescriptorManager;
import io.agrest.sencha.ops.FilterUtil;
import io.agrest.sencha.protocol.Filter;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.parser.ASTObjPath;

import javax.ws.rs.core.Response.Status;
import java.util.List;

import static org.apache.cayenne.exp.ExpressionFactory.expFalse;
import static org.apache.cayenne.exp.ExpressionFactory.greaterExp;
import static org.apache.cayenne.exp.ExpressionFactory.greaterOrEqualExp;
import static org.apache.cayenne.exp.ExpressionFactory.inExp;
import static org.apache.cayenne.exp.ExpressionFactory.lessExp;
import static org.apache.cayenne.exp.ExpressionFactory.lessOrEqualExp;
import static org.apache.cayenne.exp.ExpressionFactory.likeIgnoreCaseExp;
import static org.apache.cayenne.exp.ExpressionFactory.matchExp;
import static org.apache.cayenne.exp.ExpressionFactory.noMatchExp;

public class SenchaFilterExpressionCompiler implements ISenchaFilterExpressionCompiler {

    private static final int MAX_VALUE_LENGTH = 1024;

    private IPathDescriptorManager pathCache;
    private IExpressionPostProcessor postProcessor;

    public SenchaFilterExpressionCompiler(
            @Inject IPathDescriptorManager pathCache,
            @Inject IExpressionPostProcessor postProcessor) {
        this.pathCache = pathCache;
        this.postProcessor = postProcessor;
    }

    @Override
    public Expression process(AgEntity<?> entity, List<Filter> filters) {

        Expression combined = null;

        for (Filter filter : filters) {

            if(filter.isDisabled()) {
                continue;
            }

            Expression qualifier;
            switch (filter.getOperator()) {
                case "like":
                    qualifier = like(filter);
                    break;
                case "=":
                    qualifier = eq(filter);
                    break;
                case "!=":
                    qualifier = neq(filter);
                    break;
                case ">":
                    qualifier = gt(filter);
                    break;
                case ">=":
                    qualifier = gte(filter);
                    break;
                case "<":
                    qualifier = lt(filter);
                    break;
                case "<=":
                    qualifier = lte(filter);
                    break;
                case "in":
                    qualifier = in(filter);
                    break;
                default:
                    throw new AgException(Status.BAD_REQUEST, "Invalid filter operator: " + filter.getOperator());
            }

            // validate property path
            if (qualifier.getOperandCount() == 2) {
                pathCache.getPathDescriptor(entity, (ASTObjPath) qualifier.getOperand(0));
            }

            combined = combined != null ? combined.andExp(qualifier) : qualifier;
        }

        return postProcessor.process(entity, combined);
    }

    Expression eq(Filter filter) {
        return matchExp(filter.getProperty(), filter.getValue());
    }

    Expression neq(Filter filter) {
        return noMatchExp(filter.getProperty(), filter.getValue());
    }

    Expression like(Filter filter) {
        if (filter.getValue() == null || filter.isExactMatch() || filter.getValue() instanceof Boolean) {
            return eq(filter);
        }

        String string = filter.getValue().toString();
        checkValueLength(string);
        string = FilterUtil.escapeValueForLike(string) + "%";
        return likeIgnoreCaseExp(filter.getProperty(), string);
    }

    Expression gt(Filter filter) {
        return (filter.getValue() == null) ? expFalse() : greaterExp(filter.getProperty(), filter.getValue());
    }

    Expression gte(Filter filter) {
        return (filter.getValue() == null) ? expFalse() : greaterOrEqualExp(filter.getProperty(), filter.getValue());
    }

    Expression lt(Filter filter) {
        return (filter.getValue() == null) ? expFalse() : lessExp(filter.getProperty(), filter.getValue());
    }

    Expression lte(Filter filter) {
        return (filter.getValue() == null) ? expFalse() : lessOrEqualExp(filter.getProperty(), filter.getValue());
    }

    @SuppressWarnings("rawtypes")
    Expression in(Filter filter) {

        if (!(filter.getValue() instanceof List)) {
            return eq(filter);
        }

        return inExp(filter.getProperty(), (List) filter.getValue());
    }

    private void checkValueLength(String value) {
        if (value.length() > MAX_VALUE_LENGTH) {
            throw new AgException(Status.BAD_REQUEST, "filter 'value' is to long: " + value);
        }
    }
}
