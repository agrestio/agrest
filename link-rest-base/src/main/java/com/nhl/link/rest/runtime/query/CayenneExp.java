package com.nhl.link.rest.runtime.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents 'cayenneExp' LinkRest protocol parameter.
 *
 * @since 2.13
 */
public class CayenneExp {
    public static final String CAYENNE_EXP = "cayenneExp";

    private String exp;
    private Map<String, Object> params = new HashMap<>();
    private List<Object> inPositionParams = new ArrayList<>();

    public CayenneExp(String exp) {
        this.exp = exp;
    }

    public CayenneExp(String exp, Object... params) {
        this.exp = exp;
        Collections.addAll(this.inPositionParams, params);
    }

    public CayenneExp(String exp, Map<String, Object> params) {
        this.exp = exp;
        this.params = params;
    }

    public String getExp() {
        return exp;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public List<Object> getInPositionParams() {
        return inPositionParams;
    }
}
