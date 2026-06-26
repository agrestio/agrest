package io.agrest.protocol;

import io.agrest.AgException;
import io.agrest.exp.parser.AgExpressionParserTreeConstants;
import io.agrest.exp.parser.ExpAnd;
import io.agrest.exp.parser.ExpOr;
import io.agrest.exp.parser.ExpScalarList;
import io.agrest.exp.parser.Node;
import io.agrest.exp.parser.SimpleNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

final class ExpUtils {

    static void appendAndChild(List<Node> nodes, Node exp) {

        if (exp instanceof ExpAnd expAnd) {
            for (Node n : expAnd.getChildren()) {
                appendAndChild(nodes, n);
            }
        } else {
            nodes.add(exp);
        }
    }

    static void appendOrChild(List<Node> nodes, Node exp) {

        if (exp instanceof ExpOr expOr) {
            for (Node n : expOr.getChildren()) {
                appendOrChild(nodes, n);
            }
        } else {
            nodes.add(exp);
        }
    }

    static Exp composeBinary(Exp exp, Exp child1, Exp child2) {
        Node[] children = new Node[]{(Node) child1, (Node) child2};
        ((SimpleNode) exp).setChildren(children);
        return exp;
    }

    static Exp composeTernary(Exp exp, Exp child1, Exp child2, Exp child3) {
        Node[] children = new Node[]{(Node) child1, (Node) child2, (Node) child3};
        ((SimpleNode) exp).setChildren(children);
        return exp;
    }

    static Exp scalarArray(Collection<?> values) {
        ExpScalarList exp = new ExpScalarList(AgExpressionParserTreeConstants.JJTSCALARLIST);
        // TODO: copy the values to a new array for guaranteed immutability?
        exp.jjtSetValue(values);
        return exp;
    }

    static Exp scalarArray(Exp[] values) {
        ExpScalarList exp = new ExpScalarList(AgExpressionParserTreeConstants.JJTSCALARLIST);

        int len = values != null ? values.length : 0;
        Exp[] clonedValues = new Exp[len];
        for (int i = 0; i < len; i++) {
            clonedValues[i] = ((SimpleNode) values[i]).deepCopy();
        }

        exp.jjtSetValue(clonedValues);
        return exp;
    }

    static Exp scalarArray(Object[] values) {
        ExpScalarList exp = new ExpScalarList(AgExpressionParserTreeConstants.JJTSCALARLIST);
        // TODO: copy the values to a new array for guaranteed immutability?
        exp.jjtSetValue(values != null ? Arrays.asList(values) : Collections.emptyList());
        return exp;
    }

    // TODO: maybe we can handle all these arrays directly instead of converting them to lists?
    static List<?> wrapPrimitiveArray(Object value) {
        return switch (value) {
            case byte[] array -> {
                List<Byte> result = new ArrayList<>(array.length);
                for (byte b : array) {
                    result.add(b);
                }
                yield result;
            }
            case short[] array -> {
                List<Short> result = new ArrayList<>(array.length);
                for (short b : array) {
                    result.add(b);
                }
                yield result;
            }
            case char[] array -> {
                List<Character> result = new ArrayList<>(array.length);
                for (char b : array) {
                    result.add(b);
                }
                yield result;
            }
            case int[] array -> {
                List<Integer> result = new ArrayList<>(array.length);
                for (int b : array) {
                    result.add(b);
                }
                yield result;
            }
            case long[] array -> {
                List<Long> result = new ArrayList<>(array.length);
                for (long b : array) {
                    result.add(b);
                }
                yield result;
            }
            case float[] array -> {
                List<Float> result = new ArrayList<>(array.length);
                for (float b : array) {
                    result.add(b);
                }
                yield result;
            }
            case double[] array -> {
                List<Double> result = new ArrayList<>(array.length);
                for (double b : array) {
                    result.add(b);
                }
                yield result;
            }
            case boolean[] array -> {
                List<Boolean> result = new ArrayList<>(array.length);
                for (boolean b : array) {
                    result.add(b);
                }
                yield result;
            }
            default -> throw AgException.internalServerError("Array of type '%s' is not supported as an 'in' exp parameter",
                    value.getClass().getComponentType().getSimpleName());
        };
    }
}
