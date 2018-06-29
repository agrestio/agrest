package com.nhl.link.rest.runtime.query;

import java.util.HashMap;
import java.util.Map;

/**
 * @since 2.13
 *
 * Represents Cayenne Expression query parameter
 */
public class CayenneExp {

    private String exp = null;
    private Map<String, Object> params = new HashMap<>();

    public String getExp() {
        return exp;
    }

    public void setExp(String exp) {
        this.exp = exp;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}
