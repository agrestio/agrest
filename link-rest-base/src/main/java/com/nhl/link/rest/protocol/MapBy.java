package com.nhl.link.rest.protocol;

/**
 * Represents 'mapBy' LinkRest protocol parameter
 */
public class MapBy {
    
    public static final String MAP_BY = "mapBy";

    private String path;

    public MapBy(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

}
