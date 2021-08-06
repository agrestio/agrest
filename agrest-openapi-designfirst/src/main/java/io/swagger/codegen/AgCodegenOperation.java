package io.swagger.codegen;


import org.openapitools.codegen.CodegenOperation;
import org.openapitools.codegen.CodegenParameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AgCodegenOperation extends CodegenOperation {

    public boolean hasCompoundId;
    public List<CodegenParameter> modelAttributes = new ArrayList<>();
    public List<AgCodegenOperation> modelRelations = new ArrayList<>();

    public AgCodegenOperation() {
    }

    public AgCodegenOperation(CodegenOperation codegenOperation) {
        super();

        // Makes copy all of CodegenOperation fields
        this.responseHeaders.addAll(codegenOperation.responseHeaders);
        this.hasAuthMethods = codegenOperation.hasAuthMethods;
        this.hasConsumes = codegenOperation.hasConsumes;
        this.hasProduces = codegenOperation.hasProduces;
        this.hasParams = codegenOperation.hasParams;
        this.hasOptionalParams = codegenOperation.hasOptionalParams;
        this.returnTypeIsPrimitive = codegenOperation.returnTypeIsPrimitive;
        this.returnSimpleType = codegenOperation.returnSimpleType;
        this.subresourceOperation = codegenOperation.subresourceOperation;
        this.isMap = codegenOperation.isMap;
        this.isArray = codegenOperation.isArray;
        this.isMultipart = codegenOperation.isMultipart;
        this.isResponseBinary = codegenOperation.isResponseBinary;
        this.hasReference = codegenOperation.hasReference;
        this.isRestfulIndex = codegenOperation.isRestfulIndex;
        this.isRestfulShow = codegenOperation.isRestfulShow;
        this.isRestfulCreate = codegenOperation.isRestfulCreate;
        this.isRestfulUpdate = codegenOperation.isRestfulUpdate;
        this.isRestfulDestroy = codegenOperation.isRestfulDestroy;
        this.isRestful = codegenOperation.isRestful;
        this.path = codegenOperation.path;
        this.operationId = codegenOperation.operationId;
        this.returnType = codegenOperation.returnType;
        this.httpMethod = codegenOperation.httpMethod;
        this.returnBaseType = codegenOperation.returnBaseType;
        this.returnContainer = codegenOperation.returnContainer;
        this.summary = codegenOperation.summary;
        this.unescapedNotes = codegenOperation.unescapedNotes;
        this.notes = codegenOperation.notes;
        this.baseName = codegenOperation.baseName;
        this.defaultResponse = codegenOperation.defaultResponse;
        this.discriminator = codegenOperation.discriminator;
        this.consumes = codegenOperation.consumes;
        this.produces = codegenOperation.produces;
        this.bodyParam = codegenOperation.bodyParam;
        this.allParams = codegenOperation.allParams;
        this.bodyParams = codegenOperation.bodyParams;
        this.pathParams = codegenOperation.pathParams;
        this.queryParams = QueryParamExtensions.extend(codegenOperation.queryParams);
        this.headerParams = codegenOperation.headerParams;
        this.formParams = codegenOperation.formParams;
        this.authMethods = codegenOperation.authMethods;
        this.tags = codegenOperation.tags;
        this.responses = codegenOperation.responses;
        this.imports = codegenOperation.imports;
        this.examples = codegenOperation.examples;
        this.externalDocs = codegenOperation.externalDocs;
        this.vendorExtensions = codegenOperation.vendorExtensions;
        this.nickname = codegenOperation.nickname;
        this.operationIdLowerCase = codegenOperation.operationIdLowerCase;
        this.operationIdCamelCase = codegenOperation.operationIdCamelCase;
    }

    /**
     * Check if act as Restful index method
     *
     * @return true if act as Restful index method, false otherwise
     */
    @Override
    public boolean isRestfulIndex() {
        return "GET".equals(httpMethod) && pathParams.isEmpty();
    }

    /**
     * Check if act as Restful show method
     *
     * @return true if act as Restful show method, false otherwise
     */
    @Override
    public boolean isRestfulShow() {
        return "GET".equals(httpMethod) && isByIdPath();
    }

    /**
     * Check if act as Restful index to retrieve many children method
     *
     * @return true if act as Restful index to many items method, false otherwise
     */
    public boolean isRestfulIndexToMany() {
        return "GET".equals(httpMethod) && isRelatedToManyPath();
    }

    /**
     * Check if act as Restful index to retrieve child via parent method
     *
     * @return true if act as Restful index to child via parent, false otherwise
     */
    public boolean isRestfulIndexRelated() {
        return "GET".equals(httpMethod) && isParentChildPath() ;
    }

    /**
     * Check if act as Restful destroy method
     *
     * @return true if act as Restful destroy method, false otherwise
     */
    @Override
    public boolean isRestfulDestroy() {
        return "DELETE".equalsIgnoreCase(httpMethod)
                && !isRelatedToManyPath()
                && !isParentChildPath();
    }

    /**
     * Check if act as Restful destroy many children method
     *
     * @return true if act as Restful destroy many child items method, false otherwise
     */
    public boolean isRestfulDestroyToMany() {
        return "DELETE".equalsIgnoreCase(httpMethod) && isRelatedToManyPath();
    }

    /**
     * Check if act as Restful child via parent destroy method
     *
     * @return true if act as Restful child via parent destroy method, false otherwise
     */
    public boolean isRestfulRelatedDestroy() {
        return "DELETE".equalsIgnoreCase(httpMethod) && isParentChildPath();
    }

    /**
     * Check if act as Restful bulk update method
     *
     * @return true if act as Restful bulk update method, false otherwise
     */
    public boolean isRestfulBulkUpdate() {
        return Arrays.asList("PUT", "PATCH").contains(httpMethod.toUpperCase()) && !getHasPathParams();
    }

    /**
     * Check if act as Restful child via parent update method
     *
     * @return true if act as Restful child via parent update method, false otherwise
     */
    public boolean isRestfulRelatedUpdate() {
        return Arrays.asList("PUT", "PATCH").contains(httpMethod.toUpperCase()) && isParentChildPath() ;
    }

    /**
     * Check if act as Restful many children via parent update method
     *
     * @return true if act as Restful many children via parent update method, false otherwise
     */
    public boolean isRestfulRelatedToManyUpdate() {
        return Arrays.asList("PUT", "PATCH").contains(httpMethod.toUpperCase()) && isRelatedToManyPath() ;
    }

    /**
     * Check if act as Restful child via parent create method
     *
     * @return true if act as Restful child via parent create method, false otherwise
     */
    public boolean isRestfulRelatedCreate() {
        return "POST".equalsIgnoreCase(httpMethod)  && isParentChildPath() ;
    }


    /**
     * Check if act as Restful many children via parent create method
     *
     * @return true if act as Restful many children via parent create method, false otherwise
     */
    public boolean isRestfulRelatedToManyCreate() {
        return "POST".equalsIgnoreCase(httpMethod) && isRelatedToManyPath() ;
    }

    public boolean isRelationPath() {
        return isParentChildPath() || isRelatedToManyPath();
    }

    /**
     * Check if the path match format /xxx/:id/yyy/:tid
     *
     * @return true if path act as parent-child
     */
    private boolean isParentChildPath() {
        if (pathParams.size() != 2 || path == null) {
            return false;
        }
        String id = pathParams.get(0).baseName;
        String tid = pathParams.get(1).baseName;

        return path.contains("/{" + id + "}") && path.contains("/{" + tid + "}");
    }

    /**
     * Check if the path match format /xxx/:id/yyy's
     *
     * @return true if path act as parent-child
     */
    private boolean isRelatedToManyPath() {
        if (pathParams.size() != 1 || path == null) {
            return false;
        }
        String id = pathParams.get(0).baseName;

        return path.contains("/{" + id + "}/");
    }

    /**
     * Check if the path match format /xxx/:id
     *
     * @return true if path act as member
     */
    private boolean isByIdPath() {
        if (pathParams.size() != 1 || path == null)  {
            return false;
        }
        String id = pathParams.get(0).baseName;
        return path.endsWith("/{" + id + "}");
    }
}
