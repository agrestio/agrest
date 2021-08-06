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

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static org.openapitools.codegen.utils.StringUtils.camelize;

public class AgServerCodegen extends AbstractJavaJAXRSServerCodegen implements AgServerFeatures {

    private final Map<String, Set<CodegenProperty>> models = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    protected boolean generateForTesting = false;

    public AgServerCodegen() {
        super();
        artifactId = "swagger-jaxrs-agrest-server";

        supportsInheritance = true;
        sourceFolder = "src/main/java";
        implFolder = "src/main/java";
        testFolder = "src/test/java";

        outputFolder = "generated-code/JavaJaxRS-Agrest";

        apiTestTemplateFiles.clear();

        modelDocTemplateFiles.remove("model_doc.mustache");
        apiDocTemplateFiles.remove("api_doc.mustache");

        typeMapping.put("date", "LocalDateTime");
        typeMapping.put("DateTime", "LocalDateTime");
        typeMapping.put("number", "Double");

        typeMapping.put("exp", "Exp");
        typeMapping.put("dir", "Dir");
        typeMapping.put("exclude", "Exclude");
        typeMapping.put("include", "Include");
        typeMapping.put("sort", "Sort");
        typeMapping.put("mapBy", "MapBy");
        typeMapping.put("start", "Start");
        typeMapping.put("limit", "Limit");

        importMapping.put("LocalDate", "java.time.LocalDateTime");
        importMapping.put("LocalDateTime", "java.time.LocalDateTime");

        embeddedTemplateDir = templateDir = AbstractJavaJAXRSServerCodegen.JAXRS_TEMPLATE_DIRECTORY_NAME + File.separator + "agrest";

        cliOptions.add(CliOption.newBoolean(GENERATE_FOR_TESTING, "Generates models and API's for testing purpose"));
    }

    @Override
    public String getName() {
        return "agrest";
    }

    @Override
    public String getHelp() {
        return "Generates Agrest jaxrs API.";
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
    public Map<String, Object> postProcessOperationsWithModels(Map<String, Object> objs, List<Object> allModels) {
        Map<String, Object> objsResult = super.postProcessOperationsWithModels(objs, allModels);

        Map<String, Object> operations = (Map<String, Object>) objsResult.get("operations");

        if (operations != null && !models.isEmpty()) {
            @SuppressWarnings("unchecked")
            List<CodegenOperation> ops = (List<CodegenOperation>) operations.get("operation");
            List<AgCodegenOperation> newOps = new ArrayList<AgCodegenOperation>();
            for (CodegenOperation operation : ops) {
                if (models.get(operation.baseName) == null) {
                    continue;
                }

                // Removes container from response
                operation.returnType = models.keySet().stream().filter(k -> k.equalsIgnoreCase(operation.baseName)).findFirst().get();
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
            for (CodegenProperty prop : models.get(operation.baseName)) {
                // Selects plain attributes only
                if ((prop.complexType == null || !models.keySet().contains(prop.complexType))
                        && !"id".equalsIgnoreCase(prop.baseName)) {
                    CodegenParameter codegenParam = new CodegenParameter();
                    codegenParam.paramName = prop.baseName;
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
                // not needed anymore as "hasMore" attribute doesn't exists anymore
                //operation.modelAttributes.get(operation.modelAttributes.size() - 1).hasMore = false;
            }
        }
    }

    private void populateRelations(AgCodegenOperation operation, AgCodegenOperation relation) {
        // if path looks like /xxx/:id/yyy/:tid or /xxx/:id/yyy's, stores corresponding relation only
        if (operation.isRelationPath()) {
            // copies queryParam's from parent operation to child relation
            relation.allParams = relation.queryParams = operation.queryParams;

            if (operation.bodyParam == null) { // In case of GET or DELETE operations
                String[] path = operation.path.split("/");
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
    public Map<String, Object> postProcessAllModels(Map<String, Object> objs) {
        // prevents to generate models for protocol parameters
        objs.entrySet()
                .removeIf(e -> typeMapping.keySet().stream()
                        .anyMatch(k -> k.equalsIgnoreCase(e.getKey())));

        return super.postProcessAllModels(objs);
    }

    @Override
    public void setGenerateForTesting(boolean generateForTesting) {
        this.generateForTesting = generateForTesting;
    }
}

