package io.agrest.protocol.junit;

import io.agrest.SelectStage;
import io.agrest.runtime.AgRuntime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class ControlParamsTester {

    private final Class<?> type;
    private final AgRuntime runtime;
    private final Map<String, List<String>> params;

    public static ControlParamsTester test(Class<?> type, AgRuntime runtime) {
        return new ControlParamsTester(type, runtime);
    }

    public ControlParamsTester(Class<?> type, AgRuntime runtime) {
        this.type = type;
        this.runtime = runtime;
        this.params = new HashMap<>();
    }

    public ControlParamsTester param(String key, String... values) {
        params.put(key, asList(values));
        return this;
    }

    public <T> ControlParamsChecker parseRequest() {

        ControlParamsChecker[] checkerHolder = new ControlParamsChecker[1];
        runtime
                .select(type)
                .clientParams(params)
                .terminalStage(SelectStage.CREATE_ENTITY, c -> checkerHolder[0] = new ControlParamsChecker(c))
                .get();

        return checkerHolder[0];
    }
}
