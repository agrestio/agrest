package com.nhl.link.rest.runtime.query;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 2.13
 *
 * Represents Exclude query parameter
 */
public class Exclude {
    public static final String EXCLUDE = "exclude";

    private String path;
    private List<Exclude> excludes = new ArrayList<>();

    public Exclude(String path) {
        this.path = path;
    }


    public Exclude(List<Exclude> excludes) {
        this.excludes = excludes;
    }

    public String getPath() {
        return path;
    }

    public List<Exclude> getExcludes() {
        return excludes;
    }

}
