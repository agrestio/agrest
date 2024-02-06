package io.agrest.exp.parser;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class ExpStringConverter {

    private ExpStringConverter() {
    }

    public static String convert(ExpAbs exp) {
        return exp.children != null ? "abs(" + exp.children[0] + ")" : "abs(?)";
    }

    public static String convert(ExpAdd exp) {
        return exp.children != null ? "(" + exp.children[0] + ") + (" + exp.children[1] + ")" : "? + ?";
    }

    public static String convert(ExpAnd exp) {
        return exp.children != null
                ? Arrays.stream(exp.children)
                .map(String::valueOf)
                .collect(Collectors.joining(") and (", "(", ")"))
                : "? and ?";
    }

    public static String convert(ExpBetween exp) {
        return exp.children != null
                ? exp.children[0] + " between " + exp.children[1] + " and " + exp.children[2]
                : "? between ? and ?";
    }

    public static String convert(ExpBitwiseAnd exp) {
        return exp.children != null
                ? "(" + exp.children[0] + ") & (" + exp.children[1] + ")"
                : "? & ?";
    }

    public static String convert(ExpBitwiseLeftShift exp) {
        return exp.children != null
                ? "(" + exp.children[0] + ") << (" + exp.children[1] + ")"
                : "? << ?";
    }

    public static String convert(ExpBitwiseNot exp) {
        return exp.children != null
                ? "~(" + exp.children[0] + ")"
                : "~ ?";
    }

    public static String convert(ExpBitwiseOr exp) {
        return exp.children != null
                ? "(" + exp.children[0] + ") | (" + exp.children[1] + ")"
                : "? | ?";
    }

    public static String convert(ExpBitwiseRightShift exp) {
        return exp.children != null
                ? "(" + exp.children[0] + ") >> (" + exp.children[1] + ")"
                : "? >> ?";
    }

    public static String convert(ExpBitwiseXor exp) {
        return exp.children != null
                ? "(" + exp.children[0] + ") ^ (" + exp.children[1] + ")"
                : "? ^ ?";
    }

    public static String convert(ExpConcat exp) {
        return exp.children != null
                ? "concat(" + exp.children[0] + ", " + exp.children[1] + ")"
                : "concat(?, ?)";
    }

    public static String convert(ExpCurrentDate exp) {
        return "currentDate()";
    }

    public static String convert(ExpCurrentTime exp) {
        return "currentTime()";
    }

    public static String convert(ExpCurrentTimestamp exp) {
        return "currentTimestamp()";
    }

    public static String convert(ExpDivide exp) {
        return exp.children != null
                ? "(" + exp.children[0] + ") / (" + exp.children[1] + ")"
                : "? / ?";
    }

    public static String convert(ExpEqual exp) {
        return exp.children != null
                ? "(" + exp.children[0] + ") = (" + exp.children[1] + ")"
                : "? = ?";
    }

    public static String convert(ExpExtract exp) {
        return exp.children != null
                ? exp.value + "(" + exp.children[0] + ")"
                : exp.value + "(?)";
    }

    public static String convert(ExpFalse exp) {
        return "false";
    }

    public static String convert(ExpGreater exp) {
        return exp.children != null
                ? "(" + exp.children[0] + ") > (" + exp.children[1] + ")"
                : "? > ?";
    }

    public static String convert(ExpGreaterOrEqual exp) {
        return exp.children != null
                ? "(" + exp.children[0] + ") >= (" + exp.children[1] + ")"
                : "? >= ?";
    }

    public static String convert(ExpIn exp) {
        return exp.children != null
                ? exp.children[0] + " in " + (exp.children[1] instanceof ExpNamedParameter ? exp.children[1] : "(" + exp.children[1] + ")")
                : "? in (?)";
    }

    public static String convert(ExpLength exp) {
        return "length(" + (exp.children != null ? exp.children[0] : "?") + ")";
    }

    public static String convert(ExpLess exp) {
        return exp.children != null
                ? "(" + exp.children[0] + ") < (" + exp.children[1] + ")"
                : "? < ?";
    }

    public static String convert(ExpLessOrEqual exp) {
        return exp.children != null
                ? "(" + exp.children[0] + ") <= (" + exp.children[1] + ")"
                : "? <= ?";
    }

    public static String convert(ExpLike exp) {
        return exp.children != null
                ? exp.children[0] + " like " + exp.children[1] + (exp.children.length > 2 ? " escape " + exp.children[2] : "")
                : "? like ?";
    }

    public static String convert(ExpLikeIgnoreCase exp) {
        return exp.children != null
                ? exp.children[0] + " likeIgnoreCase " + exp.children[1] + (exp.children.length > 2 ? " escape " + exp.children[2] : "")
                : "? likeIgnoreCase ?";
    }

    public static String convert(ExpLocate exp) {
        return exp.children != null
                ? "locate(" + exp.children[0] + ", " + exp.children[1] + (exp.children.length > 2 ? ", (" + exp.children[2] + ")" : "") + ")"
                : "locate(?, ?)";
    }

    public static String convert(ExpLower exp) {
        return exp.children != null
                ? "lower(" + exp.children[0] + ")"
                : "lower(?)";
    }

    public static String convert(ExpMod exp) {
        return exp.children != null
                ? "mod(" + exp.children[0] + ", " + exp.children[1] + ")"
                : "mod(?, ?)";
    }

    public static String convert(ExpMultiply exp) {
        return exp.children != null
                ? "(" + exp.children[0] + ") * (" + exp.children[1] + ")"
                : "? * ?";
    }

    public static String convert(ExpNamedParameter exp) {
        String name = exp.getName();
        return name != null ? "$" + name : "null";
    }

    public static String convert(ExpNegate exp) {
        return exp.children != null
                ? "-(" + exp.children[0] + ")"
                : "-?";
    }

    public static String convert(ExpNot exp) {
        return exp.children != null
                ? "not (" + exp.children[0] + ")"
                : "not ?";
    }

    public static String convert(ExpNotBetween exp) {
        return exp.children != null
                ? exp.children[0] + " not between " + exp.children[1] + " and " + exp.children[2]
                : "? not between ? and ?";
    }

    public static String convert(ExpNotEqual exp) {
        return exp.children != null
                ? "(" + exp.children[0] + ") != (" + exp.children[1] + ")"
                : "? != ?";
    }

    public static String convert(ExpNotIn exp) {
        return exp.children != null
                ? exp.children[0] + " not in " + (exp.children[1].getClass() == ExpNamedParameter.class ? exp.children[1] : "(" + exp.children[1] + ")")
                : "? not in (?)";
    }

    public static String convert(ExpNotLike exp) {
        return exp.children != null
                ? exp.children[0] + " not like " + exp.children[1] + (exp.children.length > 2 ? " escape " + exp.children[2] : "")
                : "? not like ?";
    }

    public static String convert(ExpNotLikeIgnoreCase exp) {
        return exp.children != null
                ? exp.children[0] + " not likeIgnoreCase " + exp.children[1] + (exp.children.length > 2 ? " escape " + exp.children[2] : "")
                : "? not likeIgnoreCase ?";
    }

    public static String convert(ExpOr exp) {
        return exp.children != null
                ? Arrays.stream(exp.children).map(String::valueOf).collect(Collectors.joining(") or (", "(", ")"))
                : "? or ?";
    }

    public static String convert(ExpPath exp) {
        return String.valueOf(exp.value);
    }

    public static String convert(ExpScalar exp) {
        return exp.value instanceof CharSequence
                ? "'" + exp.value + "'"
                : String.valueOf(exp.value);
    }

    public static String convert(ExpScalarList exp) {
        // children can be either an internal collection, or child nodes, so must use "getValue()"
        return exp.getValue()
                .stream()
                .map(v -> v instanceof CharSequence ? "'" + v + "'" : String.valueOf(v))
                .collect(Collectors.joining(", "));
    }

    public static String convert(ExpSqrt exp) {
        return exp.children != null ? "sqrt(" + exp.children[0] + ")" : "sqrt(?)";
    }

    public static String convert(ExpSubstring exp) {
        return exp.children != null
                ? "substring(" + exp.children[0] + ", " + exp.children[1] + (exp.children.length > 2 ? ", " + exp.children[2] : "") + ")"
                : "substring(?, ?)";
    }

    public static String convert(ExpSubtract exp) {
        return exp.children != null
                ? "(" + exp.children[0] + ") - (" + exp.children[1] + ")"
                : "? - ?";
    }

    public static String convert(ExpTrim exp) {
        return "trim(" + (exp.children != null ? exp.children[0] : "?") + ")";
    }

    public static String convert(ExpTrue exp) {
        return "true";
    }

    public static String convert(ExpUpper exp) {
        return "upper(" + (exp.children != null ? exp.children[0] : "?") + ")";
    }
}
