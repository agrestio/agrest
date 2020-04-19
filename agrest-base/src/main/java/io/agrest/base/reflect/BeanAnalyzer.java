package io.agrest.base.reflect;

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
    private static final Pattern SETTER = Pattern.compile("^set([A-Z].*)$");

    public static Stream<PropertyGetter> findGetters(Class<?> type) {
        return Stream.of(type.getMethods())
                .map(BeanAnalyzer::fromGetter)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private static Optional<PropertyGetter> fromGetter(Method maybeGetter) {
        Class<?> type = maybeGetter.getReturnType();
        if (Types.isVoid(type) || maybeGetter.getParameterTypes().length > 0) {
            return Optional.empty();
        }

        return propertyNameFromGetter(maybeGetter.getName())
                .map(n -> new PropertyGetter(n, type, maybeGetter));
    }

    public static Optional<String> propertyNameFromGetter(String maybeGetter) {
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

    public static Stream<PropertySetter> findSetters(Class<?> type) {
        return Stream.of(type.getMethods())
                .map(BeanAnalyzer::fromSetter)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private static Optional<PropertySetter> fromSetter(Method maybeSetter) {
        Class<?> returnType = maybeSetter.getReturnType();
        if (!Types.isVoid(returnType) || maybeSetter.getParameterTypes().length != 1) {
            return Optional.empty();
        }

        Class<?> parameterType = maybeSetter.getParameterTypes()[0];

        return propertyNameFromSetter(maybeSetter.getName())
                .map(n -> new PropertySetter(n, parameterType, maybeSetter));
    }

    public static Optional<String> propertyNameFromSetter(String maybeSetter) {
        Matcher matcher = SETTER.matcher(maybeSetter);
        if (!matcher.find()) {
            return Optional.empty();
        }

        String raw = matcher.group(1);
        return Optional.of(Character.toLowerCase(raw.charAt(0)) + raw.substring(1));
    }
}
