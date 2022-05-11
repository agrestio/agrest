package io.agrest.jpa.exp;

import java.util.ArrayList;
import java.util.List;

import io.agrest.protocol.Exp;
import io.agrest.protocol.exp.CompositeExp;
import io.agrest.protocol.exp.ExpVisitor;
import io.agrest.protocol.exp.KeyValueExp;
import io.agrest.protocol.exp.NamedParamsExp;
import io.agrest.protocol.exp.PositionalParamsExp;
import io.agrest.protocol.exp.SimpleExp;

/**
 * @since 5.0
 */
public class JpaExpParser implements IJpaExpParser {

    @Override
    public JpaExpression parse(Exp qualifier) {
        StringBuilder sb = new StringBuilder();
        JpaExpVisitor visitor = new JpaExpVisitor(sb);
        if(qualifier != null) {
            qualifier.visit(visitor);
        }
        JpaExpression expression = new JpaExpression(sb.toString());
        if(visitor.params != null) {
            visitor.params.forEach(expression::addParameter);
        }
        return expression;
    }

    private static class JpaExpVisitor implements ExpVisitor {

        private final StringBuilder sb;

        private List<Object> params;

        private JpaExpVisitor(StringBuilder sb) {
            this.sb = sb;
        }

        @Override
        public void visitSimpleExp(SimpleExp exp) {
            // TODO: perform actual parsing
            sb.append(exp.getTemplate());
        }

        @Override
        public void visitNamedParamsExp(NamedParamsExp exp) {
            throw new UnsupportedOperationException("Not implemented yet");
        }

        @Override
        public void visitPositionalParamsExp(PositionalParamsExp exp) {
            throw new UnsupportedOperationException("Not implemented yet");
        }

        @Override
        public void visitKeyValueExp(KeyValueExp exp) {
            if(sb.length() > 0) {
                sb.append(" and ");
            }
            addParam(exp.getValue());
            sb.append("e.").append(exp.getKey()).append(" ").append(exp.getOp()).append(" ?").append(params.size() - 1);
        }

        @Override
        public void visitCompositeExp(CompositeExp exp) {
            for(Exp part : exp.getParts()) {
                part.visit(this);
            }
        }

        private void addParam(Object param) {
            if(params == null) {
                params = new ArrayList<>();
            }
            params.add(param);
        }
    }
}
