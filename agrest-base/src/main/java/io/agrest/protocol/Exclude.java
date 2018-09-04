package io.agrest.protocol;

import java.util.Collections;
import java.util.List;

/**
 * Represents 'exclude' AgREST protocol parameter.
 *
 * @since 2.13
 */
public class Exclude {

    private String path;
    private List<Exclude> excludes;

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
        return excludes != null ? excludes : Collections.emptyList();
    }

}
