package io.swagger.codegen.languages;

import io.swagger.codegen.CliOption;
import io.swagger.codegen.CodegenModel;
import io.swagger.codegen.CodegenOperation;
import io.swagger.codegen.CodegenParameter;
import io.swagger.codegen.CodegenProperty;
import io.swagger.codegen.LinkRestCodegenOperation;
import io.swagger.codegen.languages.features.LinkRestServerFeatures;
import io.swagger.models.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class LinkRestServerCodegen extends AbstractJavaJAXRSServerCodegen implements LinkRestServerFeatures {
    private static final Logger LOGGER = LoggerFactory.getLogger(io.swagger.codegen.languages.LinkRestServerCodegen.class);

    private static final String QUERY_PARAMS_SELECT = "|start|limit|cayenneExp|mapBy|sort|dir|include|exclude|";
    private static final String QUERY_PARAMS_UPDATE = "|include|exclude|";

    private final Map<String, Set<CodegenProperty>> models = new HashMap<>();

    protected boolean generateForTesting = false;
    protected boolean hasCustomisation = false;

    public LinkRestServerCodegen() {
        super();
        artifactId = "swagger-jaxrs-linkrest-server";

        supportsInheritance = true;
        sourceFolder = "src/main/java";
        implFolder = "src/main/java";
        testFolder = "src/test/java";

        outputFolder = "generated-code/JavaJaxRS-LinkRest";

        apiTestTemplateFiles.clear();

        modelDocTemplateFiles.remove("model_doc.mustache");
        apiDocTemplateFiles.remove("api_doc.mustache");

        apiTemplateFiles.put("apiStage.mustache", ".java");

        typeMapping.put("date", "LocalDateTime");
        importMapping.put("LocalDate", "java.time.LocalDateTime");
        importMapping.put("LocalDateTime", "java.time.LocalDateTime");
        typeMapping.put("DateTime", "LocalDateTime");
        typeMapping.put("number", "Double");

        embeddedTemplateDir = templateDir = AbstractJavaJAXRSServerCodegen.JAXRS_TEMPLATE_DIRECTORY_NAME + File.separator + "linkrest";

        cliOptions.add(CliOption.newBoolean(GENERATE_FOR_TESTING, "Generates models and API's for testing purpose"));
    }

    @Override
    public String getName() {
        return "linkrest";
    }

    @Override
    public String getHelp() {
        return "Generates LinkRest jaxrs API.";
    }

    @Override
    public void processOpts() {
        super.processOpts();

        if (additionalProperties.containsKey(GENERATE_FOR_TESTING)) {
            this.setGenerateForTesting(convertPropertyToBooleanAndWriteBack(GENERATE_FOR_TESTING));
        }
    }

    @Override
    public void addOperationToGroup(String tag, String resourcePath, Operation operation, CodegenOperation co, Map<String, List<CodegenOperation>> operations) {
        super.addOperationToGroup(tag, resourcePath, operation, co, operations);
        co.subresourceOperation = !co.path.isEmpty();
    }

    @Override
    public void postProcessModelProperty(CodegenModel model, CodegenProperty property) {
        if (model.classname != null) {
            Set<CodegenProperty> props = models.get(model.classname);
            if (props == null) {
                props = new HashSet<>();
            }
            props.add(property);
            models.put(model.classname, props);
        }
    }

    @Override
    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
        Map<String, Object> objsResult = super.postProcessOperations(objs);

        Map<String, Object> operations = (Map<String, Object>) objsResult.get("operations");

        if ( operations != null ) {
            @SuppressWarnings("unchecked")
            List<CodegenOperation> ops = (List<CodegenOperation>) operations.get("operation");
            List<LinkRestCodegenOperation> newOps = new ArrayList<LinkRestCodegenOperation>();
            for ( CodegenOperation operation : ops ) {
                // Removes container from response
                operation.returnType = operation.baseName;

                LinkRestCodegenOperation lrOperation = new LinkRestCodegenOperation(operation);
                // Stores model properties to use them as constraints
                populateModelAttributes(lrOperation);
                // Check if operation has user defined parameters
                if (hasCustomParameters(lrOperation)) {
                    hasCustomisation = true;
                }
                // Stores model relations to use them as constraints
                for (final CodegenProperty prop : models.get(lrOperation.baseName)) {
                    if (prop.complexType != null && models.keySet().contains(prop.complexType)) {
                        LinkRestCodegenOperation lrRelation = new LinkRestCodegenOperation();
                        lrRelation.returnType = lrRelation.baseName = prop.complexType;
                        lrRelation.bodyParam = new CodegenParameter();
                        lrRelation.bodyParam.paramName = prop.baseName;

                        populateModelAttributes(lrRelation);
                        populateRelations(lrOperation, lrRelation);
                    }
                }
                newOps.add(lrOperation);
            }
            operations.put("operation", newOps);
        }

        // Marks if resource has custom parameters
        if (hasCustomisation) {
            objsResult.put("hasCustomisation", true);
        }

        return objsResult;
    }

    private void populateModelAttributes(LinkRestCodegenOperation lrOperation) {
        if (models.get(lrOperation.baseName) != null) {
            for (final CodegenProperty prop : models.get(lrOperation.baseName)) {
                // Selects plain attributes only
                if ((prop.complexType == null || !models.keySet().contains(prop.complexType))
                        && !"id".equalsIgnoreCase(prop.baseName)) {
                    final CodegenParameter codegenParam = new CodegenParameter();
                    codegenParam.paramName = prop.baseName;
                    codegenParam.hasMore = true;
                    // checks if there is queryParam with the same name as model attribute
                    if (lrOperation.queryParams.stream().anyMatch(p -> codegenParam.paramName.equalsIgnoreCase(p.paramName))) {
                        lrOperation.hasCompoundId = codegenParam.isQueryParam = true;
                        // removes model attribute related to queryParam from params list
                        lrOperation.queryParams
                                = lrOperation.queryParams
                                    .stream()
                                    .filter(p -> !p.paramName.equalsIgnoreCase(codegenParam.paramName))
                                    .collect(Collectors.toList());
                    }
                    lrOperation.modelAttributes.add(codegenParam);
                }
            }
            if (!lrOperation.modelAttributes.isEmpty()) {
                lrOperation.modelAttributes.get(lrOperation.modelAttributes.size() -1).hasMore = false;
            }
        }
    }

    private void populateRelations(LinkRestCodegenOperation lrOperation, LinkRestCodegenOperation lrRelation) {
        // if path looks like /xxx/:id/yyy/:tid or /xxx/:id/yyy's, stores corresponding relation only
        if (lrOperation.isRelationPath()) {
            // copies queryParam's from parent operation to child relation
            lrRelation.allParams = lrRelation.queryParams = lrOperation.queryParams;

            if (lrOperation.bodyParam == null) { // In case of GET or DELETE operations
                final String[] path = lrOperation.path.split("/");
                if (path.length > 1
                        && (lrRelation.bodyParam.paramName.equalsIgnoreCase(path[path.length - 1])
                        || lrRelation.bodyParam.paramName.equalsIgnoreCase(path[path.length - 2]))) {
                    lrOperation.modelRelations.add(lrRelation);
                }
            } else if (lrRelation.baseName.equalsIgnoreCase(lrOperation.bodyParam.baseType)) {
                lrOperation.modelRelations.add(lrRelation);
            }
        } else {
            lrOperation.modelRelations.add(lrRelation);
        }
    }

    private boolean hasCustomParameters(LinkRestCodegenOperation lrOperation) {
        for( CodegenParameter parameter : lrOperation.queryParams) {
            if (("GET".equalsIgnoreCase(lrOperation.httpMethod) && !QUERY_PARAMS_SELECT.contains('|' + parameter.paramName + '|') )
                    || (lrOperation.isBodyAllowed() && !QUERY_PARAMS_UPDATE.contains('|' + parameter.paramName + '|'))) {

                // saves user defined parameter
                final CodegenParameter codegenParam = parameter.copy();
                codegenParam.hasMore = true;
                lrOperation.hasCustomParams = true;
                lrOperation.customParameters.add(parameter);
            }
        }

        if (lrOperation.customParameters.isEmpty()) {
            return false;
        } else {
            // removes user defined parameter from query parameters list
            lrOperation.queryParams
                    = lrOperation.queryParams
                    .stream()
                    .filter(p -> !lrOperation.customParameters.stream().anyMatch(c -> c.paramName.equalsIgnoreCase(p.paramName)))
                    .collect(Collectors.toList());
            lrOperation.customParameters.get(lrOperation.customParameters.size() -1).hasMore = false;
            return true;
        }
    }

    @Override
    public String toApiName(String name) {
        if (name.length() == 0) {
            return "DefaultApi";
        }
        return camelize(name) + "Resource";
    }

    @Override
    public String apiFileFolder() {
        if (generateForTesting) {
            implFolder = testFolder;
            return super.apiTestFileFolder();
        }
        return super.apiFileFolder();
    }


    @Override
    public String apiFilename(String templateName, String tag) {
        String result = super.apiFilename(templateName, tag);

        if ( hasCustomisation && templateName.endsWith("Stage.mustache") ) {
            int ix = result.lastIndexOf('/');
            result = result.substring(0, ix) + "/stage" + result.substring(ix, result.length() - 5) + "Stage.java";
            result = result.replace(apiFileFolder(),
                    outputFolder + "/" + testFolder + "/" + apiPackage().replace('.', '/'));
            hasCustomisation = false;
        }

        return result;
    }

    @Override
    public String modelFileFolder() {
        if (generateForTesting) {
            return outputFolder + "/" + testFolder + "/" + modelPackage().replace('.', '/');
        }
        return super.modelFileFolder();
    }

    @Override
    public void setGenerateForTesting(boolean generateForTesting) {
        this.generateForTesting = generateForTesting;
    }
}

