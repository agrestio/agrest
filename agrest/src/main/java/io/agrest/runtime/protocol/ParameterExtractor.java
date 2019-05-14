package io.agrest.runtime.protocol;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A utility class for safe extraction of protocol parameters.
 *
 * @since 2.13
 */
public abstract class ParameterExtractor {

    public static String string(Map<String, List<String>> parameters, String name) {

        List<String> strings = strings(parameters, name);
        return strings.isEmpty() ? null : strings.get(0);
    }

    public static List<String> strings(Map<String, List<String>> parameters, String name) {
        if (parameters == null) {
            return Collections.emptyList();
        }

        List<String> result = parameters.get(name);
        if (result == null) {
            result = Collections.emptyList();
        }

        return result;
    }

    public static int integer(Map<String, List<String>> parameters, String name) {

        List<String> strings = strings(parameters, name);
        String value = strings.isEmpty() ? null : strings.get(0);

        if (value == null) {
            return -1;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException nfex) {
            return -1;
        }
    }

    /**
     * @since 3.2
     */
    public static Integer integerObject(Map<String, List<String>> parameters, String name) {

        List<String> strings = strings(parameters, name);
        String value = strings.isEmpty() ? null : strings.get(0);

        if (value == null) {
            return null;
        }

        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException nfex) {
            return null;
        }
    }
}
