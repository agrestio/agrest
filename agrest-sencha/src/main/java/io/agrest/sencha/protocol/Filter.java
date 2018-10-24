package io.agrest.sencha.protocol;

/**
 * Represents 'filter' Sencha Agrest protocol parameter.
 *
 * @since 2.13
 */
public class Filter {

    private String property;
    private Object value;
    private String operator;
    private boolean disabled;
    private boolean exactMatch;

    public Filter(String property, Object value, String operator, boolean disabled, boolean exactMatch) {
        this.property = property;
        this.value = value;
        this.operator = operator;
        this.disabled = disabled;
        this.exactMatch = exactMatch;
    }

    public String getProperty() {
        return property;
    }

    public Object getValue() {
        return value;
    }

    public String getOperator() {
        return operator;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public boolean isExactMatch() {
        return exactMatch;
    }
}
