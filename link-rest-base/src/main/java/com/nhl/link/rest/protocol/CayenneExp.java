package com.nhl.link.rest.protocol;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

/**
 * Represents 'cayenneExp' LinkRest protocol parameter.
 *
 * @since 2.13
 */
@Schema(name="CayenneExp", description="Cayenne defines a simple yet powerful object-based expression language. " +
        "Cayenne expressions are database independent and are used as query qualifiers and orderings " +
        "and also to perform in-memory evaluation with DataObjects.")
public class CayenneExp {

    @Schema(required = true, example = "name = 'yyy'")
    private String exp;

    private Map<String, Object> params;

    private List<Object> inPositionParams;

    public CayenneExp(String exp) {
        this.exp = exp;
    }

    public CayenneExp(String exp, Object... params) {
        this.exp = exp;
        this.inPositionParams = asList(params);
    }

    public CayenneExp(String exp, Map<String, Object> params) {
        this.exp = exp;
        this.params = params;
    }

    public String getExp() {
        return exp;
    }

    public Map<String, Object> getParams() {
        return params != null ? params : Collections.emptyMap();
    }

    public List<Object> getInPositionParams() {
        return inPositionParams != null ? inPositionParams : Collections.emptyList();
    }
}
