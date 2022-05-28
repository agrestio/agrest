package io.agrest.runtime.protocol.junit;

import io.agrest.SelectStage;
import io.agrest.runtime.AgRuntime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class ProtocolTester {

    private final Class<?> type;
    private final AgRuntime runtime;
    private final Map<String, List<String>> params;

    public static ProtocolTester test(Class<?> type, AgRuntime runtime) {
        return new ProtocolTester(type, runtime);
    }

    public ProtocolTester(Class<?> type, AgRuntime runtime) {
        this.type = type;
        this.runtime = runtime;
        this.params = new HashMap<>();
    }

    public ProtocolTester param(String key, String... values) {
        params.put(key, asList(values));
        return this;
    }

    public <T> ProtocolChecker parseRequest() {

        ProtocolChecker[] checkerHolder = new ProtocolChecker[1];
        runtime
                .select(type)
                .clientParams(params)
                .terminalStage(SelectStage.CREATE_ENTITY, c -> checkerHolder[0] = new ProtocolChecker(c))
                .get();

        return checkerHolder[0];
    }
}
