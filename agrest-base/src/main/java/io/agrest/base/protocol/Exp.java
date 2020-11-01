package io.agrest.base.protocol;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * Represents {@link AgProtocol#exp} protocol parameter.
 *
 * @since 4.1
 */
public class Exp {

    private final String exp;
    private final Map<String, Object> namedParams;
    private final Object[] positionalParams;

    public Exp(String exp) {
        this.exp = Objects.requireNonNull(exp);
        this.namedParams = null;
        this.positionalParams = null;
    }

    public Exp(String exp, Object... params) {
        this.exp = Objects.requireNonNull(exp);
        this.namedParams = null;

        Objects.requireNonNull(params);
        this.positionalParams = params.length > 0 ? params : null;
    }

    public Exp(String exp, Map<String, Object> params) {
        this.exp = Objects.requireNonNull(exp);

        Objects.requireNonNull(params);
        this.namedParams = !params.isEmpty() ? params : null;
        this.positionalParams = null;
    }

    /**
     * @since 3.7
     */
    public boolean usesPositionalParameters() {
        return positionalParams != null;
    }

    /**
     * @since 3.7
     */
    public boolean usesNamedParameters() {
        return namedParams != null;
    }

    public String getExp() {
        return exp;
    }

    public Map<String, Object> getNamedParams() {
        if (usesNamedParameters()) {
            return namedParams;
        }

        throw new IllegalStateException("The expression is not using named parameters");
    }

    public Object[] getPositionalParams() {
        if (usesPositionalParameters()) {
            return positionalParams;
        }

        throw new IllegalStateException("The expression is not using positional parameters");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Exp that = (Exp) o;
        return exp.equals(that.exp) &&
                Objects.equals(namedParams, that.namedParams) &&
                Arrays.equals(positionalParams, that.positionalParams);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exp, namedParams, positionalParams);
    }
}
