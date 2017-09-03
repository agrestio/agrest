package com.nhl.link.rest.meta.compiler;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @since 2.10
 */
public class BeanAnalyzer {

    private static final Pattern GETTER = Pattern.compile("^(get|is)([A-Z].*)$");

    public static Stream<PropertyGetter> findGetters(Class<?> type) {
        return Stream.of(type.getMethods())
                .map(BeanAnalyzer::fromGetter)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    static Optional<PropertyGetter> fromGetter(Method maybeGetter) {
        Class<?> type = maybeGetter.getReturnType();
        if (type.equals(Void.class) || maybeGetter.getParameterTypes().length > 0) {
            return Optional.empty();
        }

        return propertyNameFromGetter(maybeGetter.getName())
                .map(n -> new PropertyGetter(n, type, maybeGetter));
    }

    static Optional<String> propertyNameFromGetter(String maybeGetter) {
        Matcher matcher = GETTER.matcher(maybeGetter);
        if (!matcher.find()) {
            return Optional.empty();
        }

        String raw = matcher.group(2);
        if (raw.equals("Class")) {
            // 'getClass' is not a property we care about
            return Optional.empty();
        }

        return Optional.of(Character.toLowerCase(raw.charAt(0)) + raw.substring(1));
    }
}
