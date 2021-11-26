package io.agrest.filter;

import io.agrest.base.protocol.Exp;

import java.util.Optional;

/**
 * @since 4.8
 */
public class ExpObjectFilter implements ObjectFilter {

    private final Exp exp;

    public ExpObjectFilter(Exp exp) {
        this.exp = exp;
    }

    @Override
    public boolean isAccessible(Object object) {
        return false;
    }

    @Override
    public Optional<Exp> asExp() {
        return Optional.of(exp);
    }
}
