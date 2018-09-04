package io.swagger.codegen.languages.features;

public interface AgRESTServerFeatures {
    // Generates models and API's for testing purpose on app testing path
    public static final String GENERATE_FOR_TESTING = "generateForTesting";

    public void setGenerateForTesting(boolean generateForTesting);
}
