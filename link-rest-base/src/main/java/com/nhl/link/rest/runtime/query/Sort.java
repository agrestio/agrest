package com.nhl.link.rest.runtime.query;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 2.13
 *
 * Represents Sort query parameter
 */
public class Sort {
    private static final String SORT = "sort";

    private String property;
    private String direction;
    private List<Sort> sorts = new ArrayList<>();

    public Sort(String property) {
        this.property = property;
    }

    public Sort(String property, String direction) {
        this.property = property;
        this.direction = direction;
    }

    public Sort(List<Sort> sorts) {
        this.sorts = sorts;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public static String getName() {
        return SORT;
    }

    public String getProperty() {
        return property;
    }

    public String getDirection() {
        return direction;
    }

    public List<Sort> getSorts() {
        return sorts;
    }

}
