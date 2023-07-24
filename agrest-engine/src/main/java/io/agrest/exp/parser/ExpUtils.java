package io.agrest.exp.parser;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public class ExpUtils {

    public static Node in(ExpObjPath path, ExpGenericScalar<?>... scalars) {
        SimpleNode exp = new ExpIn();
        exp.children = cloneAndPush(scalars, path, false);
        return exp;
    }

    public static Node likeIgnoreCase(ExpObjPath path, ExpGenericScalar<?> scalar) {
        Node exp = new ExpLikeIgnoreCase();
        composeBinary(exp, path, scalar);
        return exp;
    }

    public static Node like(ExpObjPath path, ExpGenericScalar<?> scalar) {
        Node exp = new ExpLike();
        composeBinary(exp, path, scalar);
        return exp;
    }

    public static Node greaterOrEqual(ExpObjPath path, ExpGenericScalar<?> scalar) {
        Node exp = new ExpGreaterOrEqual();
        composeBinary(exp, path, scalar);
        return exp;
    }

    public static Node lessOrEqual(ExpObjPath path, ExpGenericScalar<?> scalar) {
        Node exp = new ExpLessOrEqual();
        composeBinary(exp, path, scalar);
        return exp;
    }

    public static Node greater(ExpObjPath path, ExpGenericScalar<?> scalar) {
        Node exp = new ExpGreater();
        composeBinary(exp, path, scalar);
        return exp;
    }

    public static Node less(ExpObjPath path, ExpGenericScalar<?> scalar) {
        Node exp = new ExpLess();
        composeBinary(exp, path, scalar);
        return exp;
    }

    public static Node equal(ExpObjPath path, ExpGenericScalar<?> scalar) {
        Node exp = new ExpEqual();
        composeBinary(exp, path, scalar);
        return exp;
    }

    public static Node and(Node exp1, Node exp2, Node... moreExp) {
        if (exp1 == null) {
            return exp2;
        }
        if (exp2 == null) {
            return exp1;
        }
        Node exp = new ExpAnd();
        composeMultiOptimized(exp, exp1, exp2, moreExp);
        return exp;
    }

    public static Node or(Node exp1, Node exp2, Node... moreExp) {
        if (exp1 == null) {
            return exp2;
        }
        if (exp2 == null) {
            return exp1;
        }
        Node exp = new ExpOr();
        composeMultiOptimized(exp, exp1, exp2, moreExp);
        return exp;
    }

    private static void composeBinary(Node exp, Node arg1, Node arg2) {
        SimpleNode expNode = (SimpleNode) exp;
        expNode.children = new Node[2];
        expNode.children[0] = arg1;
        expNode.children[1] = arg2;
    }

    private static void composeMultiOptimized(Node target, Node exp1, Node exp2, Node... moreExp) {
        SimpleNode targetNode = (SimpleNode) target;
        if (moreExp != null && moreExp.length != 0) {
            targetNode.children = Stream.concat(Stream.of(exp1, exp2), Arrays.stream(moreExp))
                    .filter(Objects::nonNull)
                    .flatMap(exp -> exp.getClass() == target.getClass()
                            ? Arrays.stream(((SimpleNode) exp).children)
                            : Stream.of(exp))
                    .toArray(Node[]::new);
        } else if (exp1.getClass() == target.getClass() && exp2.getClass() == target.getClass()) {
            targetNode.children = mergeIntoNew(((SimpleNode) exp1).children, ((SimpleNode) exp2).children);
        } else if (exp1.getClass() == target.getClass()) {
            targetNode.children = cloneAndPush(((SimpleNode) exp1).children, exp2, true);
        } else if (exp2.getClass() == target.getClass()) {
            targetNode.children = cloneAndPush(((SimpleNode) exp2).children, exp1, false);
        } else {
            composeBinary(target, exp1, exp2);
        }
    }

    private static Node[] cloneAndPush(Node[] array, Node node, boolean pushBack) {
        Node[] newArray = new Node[array.length + 1];
        newArray[pushBack ? newArray.length - 1 : 0] = node;
        System.arraycopy(array, 0, newArray, pushBack ? 0 : 1, array.length);
        return newArray;
    }

    private static Node[] mergeIntoNew(Node[] array1, Node[] array2) {
        Node[] newArray = new Node[array1.length + array2.length];
        System.arraycopy(array1, 0, newArray, 0, array1.length);
        System.arraycopy(array2, 0, newArray, array1.length, array2.length);
        return newArray;
    }
}
