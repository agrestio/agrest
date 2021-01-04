package io.agrest.base.protocol.exp;

import io.agrest.base.protocol.CayenneExp;

import java.util.Arrays;
import java.util.Objects;

/**
 * @since 3.8
 */
public class CompositeExp implements CayenneExp {

    public static final String AND = "and";
    public static final String OR = "or";

    private final String combineOperand;
    private final CayenneExp[] parts;

    public CompositeExp(String combineOperand, CayenneExp... parts) {
        this.combineOperand = Objects.requireNonNull(combineOperand);
        this.parts = Objects.requireNonNull(parts);
    }

    @Override
    public void visit(ExpVisitor visitor) {
        visitor.visitCompositeExp(this);
    }

    public String getCombineOperand() {
        return combineOperand;
    }

    public CayenneExp[] getParts() {
        return parts;
    }

    @Override
    public CayenneExp and(CayenneExp exp) {
        return AND.equals(combineOperand) ? expand(exp) : CayenneExp.super.and(exp);
    }

    @Override
    public CayenneExp or(CayenneExp exp) {
        return OR.equals(combineOperand) ? expand(exp) : CayenneExp.super.or(exp);
    }

    protected CayenneExp expand(CayenneExp exp) {
        // append another expression to the list of parts instead of creating a netsed expression
        CayenneExp[] expanded = new CayenneExp[parts.length + 1];
        System.arraycopy(parts, 0, expanded, 0, parts.length);
        expanded[parts.length] = exp;
        return new CompositeExp(combineOperand, expanded);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompositeExp that = (CompositeExp) o;
        return combineOperand.equals(that.combineOperand) && Arrays.equals(parts, that.parts);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(combineOperand);
        result = 31 * result + Arrays.hashCode(parts);
        return result;
    }
}
