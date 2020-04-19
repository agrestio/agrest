package io.agrest.runtime.entity;

import io.agrest.ResourceEntity;
import io.agrest.base.protocol.CayenneExp;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;

/**
 * @since 2.13
 */
public class CayenneExpMerger implements ICayenneExpMerger {

    private IExpressionPostProcessor postProcessor;
    private IExpressionParser expressionParser;

    public CayenneExpMerger(
            @Inject IExpressionParser expressionParser,
            @Inject IExpressionPostProcessor postProcessor) {
        this.postProcessor = postProcessor;
        this.expressionParser = expressionParser;
    }

    @Override
    public void merge(ResourceEntity<?> resourceEntity, CayenneExp cayenneExp) {

        Expression exp = postProcessor.process(
                resourceEntity.getAgEntity(),
                expressionParser.parse(cayenneExp));

        if (exp != null) {
            resourceEntity.andQualifier(exp);
        }
    }
}
