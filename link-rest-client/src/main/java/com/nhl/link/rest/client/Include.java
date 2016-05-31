package com.nhl.link.rest.client;

import org.apache.cayenne.exp.Expression;

import java.util.Collection;

public interface Include {

    String getPath();

    boolean isConstrained();

    String getMapBy();

    Expression getCayenneExp();

    Collection<Sort> getOrderings();

    Long getStart();

    Long getLimit();
}
