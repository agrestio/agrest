package com.nhl.link.rest.runtime.query;

public class MapBy {
    private static final String MAP_BY = "mapBy";

    private String path;

    public MapBy(String path) {
        this.path = path;
    }

    public static String getName() {
        return MAP_BY;
    }

    public String getPath() {
        return path;
    }

}
