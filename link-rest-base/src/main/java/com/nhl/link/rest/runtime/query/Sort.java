package com.nhl.link.rest.runtime.query;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents 'sort' LinkRest protocol parameter.
 *
 * @since 2.13
 */
public class Sort {
    public static final String SORT = "sort";

    private String property;
    private Dir direction;
    private List<Sort> sorts = new ArrayList<>();

    public Sort(String property) {
        this.property = property;
    }

    public Sort(String property, Dir direction) {
        this.property = property;
        this.direction = direction;
    }

    public Sort(List<Sort> sorts) {
        this.sorts = sorts;
    }

    public String getProperty() {
        return property;
    }

    public Dir getDirection() {
        return direction;
    }

    public List<Sort> getSorts() {
        return sorts;
    }

}
