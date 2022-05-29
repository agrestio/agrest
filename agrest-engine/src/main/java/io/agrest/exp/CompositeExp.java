package io.agrest.exp;

import io.agrest.protocol.Exp;

import java.util.Arrays;
import java.util.Objects;

/**
 * @since 4.4
 */
public class CompositeExp implements Exp {

    public static final String AND = "and";
    public static final String OR = "or";

    private final String combineOperand;
    private final Exp[] parts;

    public CompositeExp(String combineOperand, Exp... parts) {
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

    public Exp[] getParts() {
        return parts;
    }

    @Override
    public Exp and(Exp exp) {
        return AND.equals(combineOperand) ? expand(exp) : Exp.super.and(exp);
    }

    @Override
    public Exp or(Exp exp) {
        return OR.equals(combineOperand) ? expand(exp) : Exp.super.or(exp);
    }

    protected Exp expand(Exp exp) {
        // append another expression to the list of parts instead of creating a netsed expression
        Exp[] expanded = new Exp[parts.length + 1];
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
