package com.nhl.link.rest.swagger.codefirst.mavenplugin;

import org.apache.maven.plugins.annotations.Parameter;

import java.util.Set;

/**
 * @author vyarmolovich
 * 8/20/18
 */
public class ResourceSource {

    /**
     * List of java classes that contains swagger annotations to generate .yaml/.json files.
     *
     */
    @Parameter
    private Set<String> srcClasses;

    /**
     * List of java packages that contains swagger annotations to generate .yaml/.json files.
     *
     */
    @Parameter
    private Set<String> srcPackages;

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

    public Set<String> getSrcClasses() {
        return srcClasses;
    }

    public void setSrcClasses(Set<String> srcClasses) {
        this.srcClasses = srcClasses;
    }

    public Set<String> getSrcPackages() {
        return srcPackages;
    }

    public void setSrcPackages(Set<String> srcPackages) {
        this.srcPackages = srcPackages;
    }

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
}
