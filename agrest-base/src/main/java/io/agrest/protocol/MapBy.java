package io.agrest.protocol;

/**
 * Represents 'mapBy' AgREST protocol parameter
 */
public class MapBy {

    private String path;

    public MapBy(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

}
