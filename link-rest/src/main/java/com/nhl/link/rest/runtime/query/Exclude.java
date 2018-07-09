package com.nhl.link.rest.runtime.query;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 2.13
 *
 * Represents Exclude query parameter
 */
public class Exclude {
    private String path;
    private List<Exclude> excludes = new ArrayList<>();

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<Exclude> getExcludes() {
        return excludes;
    }

    public void setExcludes(List<Exclude> excludes) {
        this.excludes = excludes;
    }
}
