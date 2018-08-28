package com.nhl.link.rest.swagger.codefirst.mavenplugin;

import org.apache.maven.plugins.annotations.Parameter;

import java.util.Set;

/**
 * @author vyarmolovich
 * 8/13/18
 */
public class ComponentSource {

    /**
     * List of api Parameter classes that contains annotations to generate .yaml/.json files.
     *
     */
    @Parameter
    private Set<String> srcParameters;

    /**
     * List of domain Model classes that contains annotations to generate .yaml/.json files.
     *
     */
    @Parameter
    private Set<String> srcSchemas;

    /**
     * Scanner class to read annotated component classes
     */
    @Parameter String scanner;

    /**
     * Name (path) of generated file
     */
    @Parameter
    private String outputName;

    /**
     * Format of generated file. Maybe .json and/or .yaml
     */
    @Parameter
    private String outputFormats;


    public String getOutputName() {
        return outputName;
    }

    public void setOutputName(String outputName) {
        this.outputName = outputName;
    }

    public String getOutputFormats() {
        return outputFormats;
    }

    public void setOutputFormats(String outputFormats) {
        this.outputFormats = outputFormats;
    }

    public Set<String> getSrcParameters() {
        return srcParameters;
    }

    public void setSrcParameters(Set<String> srcParameters) {
        this.srcParameters = srcParameters;
    }

    public Set<String> getSrcSchemas() {
        return srcSchemas;
    }

    public void setSrcSchemas(Set<String> srcSchemas) {
        this.srcSchemas = srcSchemas;
    }

    public String getScanner() {
        return scanner;
    }

    public void setScanner(String scanner) {
        this.scanner = scanner;
    }
}
