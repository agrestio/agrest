package io.swagger.codegen;

import java.util.ArrayList;
import java.util.List;

public class LinkRestCodegenOperation extends CodegenOperation {

    public List<CodegenParameter> modelAttributes = new ArrayList<CodegenParameter>();
    public List<LinkRestCodegenOperation> modelRelations = new ArrayList<LinkRestCodegenOperation>();

    public LinkRestCodegenOperation() {
    }

    public LinkRestCodegenOperation(CodegenOperation codegenOperation) {
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
        this.isMapContainer = codegenOperation.isMapContainer;
        this.isListContainer = codegenOperation.isListContainer;
        this.isMultipart = codegenOperation.isMultipart;
        this.hasMore = codegenOperation.hasMore;
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
        this.queryParams = codegenOperation.queryParams;
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
}
