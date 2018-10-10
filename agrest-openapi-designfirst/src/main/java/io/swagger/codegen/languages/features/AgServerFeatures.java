package io.swagger.codegen.languages.features;

public interface AgServerFeatures {

    // Generates models and API's for testing purpose on app testing path
    String GENERATE_FOR_TESTING = "generateForTesting";

    void setGenerateForTesting(boolean generateForTesting);
}
