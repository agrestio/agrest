package com.nhl.link.rest.client;

/**
 * @since 2.0
 */
public class Sort {

    enum SortDirection {
        ASCENDING("ASC"), DESCENDING("DESC");

        private String abbrev;

        SortDirection(String abbrev) {
            this.abbrev = abbrev;
        }

        String abbrev() {
            return abbrev;
        }
    }

    public static Sort property(String propertyName) {
        return new Sort(propertyName, SortDirection.ASCENDING);
    }

    private String propertyName;
    private SortDirection direction;

    private Sort(String propertyName, SortDirection direction) {
        this.propertyName = propertyName;
        this.direction = direction;
    }

    public Sort asc() {
        direction = SortDirection.ASCENDING;
        return this;
    }

    public Sort desc() {
        direction = SortDirection.DESCENDING;
        return this;
    }

    String getPropertyName() {
        return propertyName;
    }

    SortDirection getDirection() {
        return direction;
    }
}
