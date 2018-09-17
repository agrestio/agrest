package io.swagger.codegen.languages;

import io.swagger.codegen.AgCodegenOperation;
import io.swagger.codegen.languages.features.AgServerFeatures;
import io.swagger.v3.oas.models.Operation;
import org.openapitools.codegen.CliOption;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenOperation;
import org.openapitools.codegen.CodegenParameter;
import org.openapitools.codegen.CodegenProperty;
import org.openapitools.codegen.languages.AbstractJavaJAXRSServerCodegen;
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

public class AgServerCodegen extends AbstractJavaJAXRSServerCodegen implements AgServerFeatures {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgServerCodegen.class);

    private final Map<String, Set<CodegenProperty>> models = new HashMap<>();

    protected boolean generateForTesting = false;

    public AgServerCodegen() {
        super();
        artifactId = "swagger-jaxrs-agrest-server";

        supportsInheritance = true;
        sourceFolder = "src/main/java";
        implFolder = "src/main/java";
        testFolder = "src/test/java";

        outputFolder = "generated-code/JavaJaxRS-AgREST";

        apiTestTemplateFiles.clear();

        modelDocTemplateFiles.remove("model_doc.mustache");
        apiDocTemplateFiles.remove("api_doc.mustache");

        typeMapping.put("date", "LocalDateTime");
        typeMapping.put("DateTime", "LocalDateTime");
        typeMapping.put("number", "Double");

        typeMapping.put("cayenneExp", "CayenneExp");
        typeMapping.put("dir", "Dir");
        typeMapping.put("exclude", "Exclude");
        typeMapping.put("include", "Include");
        typeMapping.put("sort", "Sort");
        typeMapping.put("mapBy", "MapBy");
        typeMapping.put("start", "Start");
        typeMapping.put("limit", "Limit");

        importMapping.put("LocalDate", "java.time.LocalDateTime");
        importMapping.put("LocalDateTime", "java.time.LocalDateTime");

        importMapping.put("CayenneExp", "io.agrest.protocol.CayenneExp");
        importMapping.put("Dir",        "io.agrest.protocol.Dir");
        importMapping.put("Exclude",    "io.agrest.protocol.Exclude");
        importMapping.put("Include",    "io.agrest.protocol.Include");
        importMapping.put("Sort",       "io.agrest.protocol.Sort");
        importMapping.put("MapBy",      "io.agrest.protocol.MapBy");
        importMapping.put("Start",      "io.agrest.protocol.Start");
        importMapping.put("Limit",      "io.agrest.protocol.Limit");

        embeddedTemplateDir = templateDir = AbstractJavaJAXRSServerCodegen.JAXRS_TEMPLATE_DIRECTORY_NAME + File.separator + "agrest";

        cliOptions.add(CliOption.newBoolean(GENERATE_FOR_TESTING, "Generates models and API's for testing purpose"));
    }

    @Override
    public String getName() {
        return "agrest";
    }

    @Override
    public String getHelp() {
        return "Generates AgREST jaxrs API.";
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
        if (model.classname != null && !model.classname.isEmpty()) {
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

        if ( operations != null && !models.isEmpty()) {
            @SuppressWarnings("unchecked")
            List<CodegenOperation> ops = (List<CodegenOperation>) operations.get("operation");
            List<AgCodegenOperation> newOps = new ArrayList<AgCodegenOperation>();
            for ( CodegenOperation operation : ops ) {
                if (models.get(operation.baseName) == null) {
                    continue;
                }

                // Removes container from response
                operation.returnType = operation.baseName;
                AgCodegenOperation newOperation = new AgCodegenOperation(operation);
                // Stores model properties to use them as constraints
                populateModelAttributes(newOperation);
                // Stores model relations to use them as constraints
                for (final CodegenProperty prop : models.get(newOperation.baseName)) {
                    if (prop.complexType != null && models.keySet().contains(prop.complexType)) {
                        AgCodegenOperation relation = new AgCodegenOperation();
                        relation.returnType = relation.baseName = prop.complexType;
                        relation.bodyParam = new CodegenParameter();
                        relation.bodyParam.paramName = prop.baseName;

                        populateModelAttributes(relation);
                        populateRelations(newOperation, relation);
                    }
                }
                newOps.add(newOperation);
            }
            operations.put("operation", newOps);
        }

        return objsResult;
    }

    private void populateModelAttributes(AgCodegenOperation operation) {
        if (models.get(operation.baseName) != null) {
            for (final CodegenProperty prop : models.get(operation.baseName)) {
                // Selects plain attributes only
                if ((prop.complexType == null || !models.keySet().contains(prop.complexType))
                        && !"id".equalsIgnoreCase(prop.baseName)) {
                    final CodegenParameter codegenParam = new CodegenParameter();
                    codegenParam.paramName = prop.baseName;
                    codegenParam.hasMore = true;
                    // checks if there is queryParam with the same name as model attribute
                    if (operation.queryParams.stream().anyMatch(p -> codegenParam.paramName.equalsIgnoreCase(p.paramName))) {
                        operation.hasCompoundId = codegenParam.isQueryParam = true;
                        // removes model attribute related to queryParam from params list
                        operation.queryParams
                                = operation.queryParams
                                    .stream()
                                    .filter(p -> !p.paramName.equalsIgnoreCase(codegenParam.paramName))
                                    .collect(Collectors.toList());
                    }
                    operation.modelAttributes.add(codegenParam);
                }
            }
            if (!operation.modelAttributes.isEmpty()) {
                operation.modelAttributes.get(operation.modelAttributes.size() -1).hasMore = false;
            }
        }
    }

    private void populateRelations(AgCodegenOperation operation, AgCodegenOperation relation) {
        // if path looks like /xxx/:id/yyy/:tid or /xxx/:id/yyy's, stores corresponding relation only
        if (operation.isRelationPath()) {
            // copies queryParam's from parent operation to child relation
            relation.allParams = relation.queryParams = operation.queryParams;

            if (operation.bodyParam == null) { // In case of GET or DELETE operations
                final String[] path = operation.path.split("/");
                if (path.length > 1
                        && (relation.bodyParam.paramName.equalsIgnoreCase(path[path.length - 1])
                        || relation.bodyParam.paramName.equalsIgnoreCase(path[path.length - 2]))) {
                    operation.modelRelations.add(relation);
                }
            } else if (relation.baseName.equalsIgnoreCase(operation.bodyParam.baseType)) {
                operation.modelRelations.add(relation);
            }
        } else {
            operation.modelRelations.add(relation);
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

