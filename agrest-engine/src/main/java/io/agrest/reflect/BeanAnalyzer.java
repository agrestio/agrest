package io.agrest.reflect;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @since 2.10
 */
public class BeanAnalyzer {

    private static final Set<String> NOT_GETTERS = Set.of("getClass", "hashCode", "notify", "notifyAll", "toString");

    @Deprecated(since = "5.0")
    private static final Pattern SETTER = Pattern.compile("^set([A-Z].*)$");
    @Deprecated(since = "5.0")
    private static final Pattern GETTER = Pattern.compile("^(get|is)([A-Z].*)$");

    private enum GetterType {
        not_getter, classic, classic_bool, simple
    }

    private enum SetterType {
        not_setter, classic
    }

    public static Stream<PropertyGetter> findGetters(Class<?> type) {
        return Stream.of(type.getMethods())
                .map(BeanAnalyzer::fromGetter)
                .filter(g -> g != null);
    }

    public static Stream<PropertySetter> findSetters(Class<?> type) {
        return Stream.of(type.getMethods())
                .map(BeanAnalyzer::fromSetter)
                .filter(s -> s != null);
    }

    /**
     * @deprecated no longer in use
     */
    @Deprecated(since = "5.0")
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

    /**
     * @deprecated no longer in use
     */
    @Deprecated(since = "5.0")
    public static Optional<String> propertyNameFromSetter(String maybeSetter) {
        Matcher matcher = SETTER.matcher(maybeSetter);
        if (!matcher.find()) {
            return Optional.empty();
        }

        String raw = matcher.group(1);
        return Optional.of(Character.toLowerCase(raw.charAt(0)) + raw.substring(1));
    }

    private static PropertyGetter fromGetter(Method maybeGetter) {
        String n = maybeGetter.getName();

        switch (getterType(maybeGetter)) {
            case simple:
                return new PropertyGetter(n, maybeGetter);
            case classic:
                return new PropertyGetter(Character.toLowerCase(n.charAt(3)) + n.substring(4), maybeGetter);
            case classic_bool:
                return new PropertyGetter(Character.toLowerCase(n.charAt(2)) + n.substring(3), maybeGetter);
            case not_getter:
            default:
                return null;
        }
    }

    private static PropertySetter fromSetter(Method maybeSetter) {
        String n = maybeSetter.getName();
        switch (setterType(maybeSetter)) {
            case classic:
                Class<?> pt = maybeSetter.getParameterTypes()[0];
                return new PropertySetter(Character.toLowerCase(n.charAt(3)) + n.substring(4), pt, maybeSetter);
            case not_setter:
            default:
                return null;
        }
    }

    private static GetterType getterType(Method method) {
        Class<?> type = method.getReturnType();
        String name = method.getName();

        if (Types.isVoid(type)
                || method.getParameterTypes().length > 0
                || NOT_GETTERS.contains(name)) {

            return GetterType.not_getter;
        }

        if (name.startsWith("is") && name.length() > 2 && Character.isUpperCase(name.charAt(2)) && Boolean.TYPE == type) {
            return GetterType.classic_bool;
        }

        if (name.startsWith("get") && name.length() > 3 && Character.isUpperCase(name.charAt(3))) {
            return GetterType.classic;
        }

        return GetterType.simple;
    }

    private static SetterType setterType(Method method) {

        if (method.getParameterTypes().length != 1) {
            return SetterType.not_setter;
        }

        String name = method.getName();
        if (!name.startsWith("set") || name.length() < 4 || !Character.isUpperCase(name.charAt(3))) {
            return SetterType.not_setter;
        }

        Class<?> type = method.getReturnType();
        if (!Types.isVoid(type) && !type.isAssignableFrom(method.getDeclaringClass())) {
            return SetterType.not_setter;
        }

        return SetterType.classic;
    }
}
