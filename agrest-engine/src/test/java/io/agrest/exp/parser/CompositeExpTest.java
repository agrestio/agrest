package io.agrest.exp.parser;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled("Not completed")
class CompositeExpTest {

    static Stream<Arguments> parseExp() {
        return Stream.of(
            Arguments.of("abs(1 + 2)",
                         ExpBuilder.fromType(ExpRoot.class)
                             .withValue("abs(1 + 2)")
                             .addChild(
                                 ExpBuilder.fromType(ExpAbs.class)
                                     .addChild(
                                         ExpBuilder.fromType(ExpAdd.class)
                                             .addChild(
                                                 ExpBuilder.fromType(ExpScalarInt.class)
                                                     .withValue(1)
                                             ).addChild(
                                                 ExpBuilder.fromType(ExpScalarInt.class)
                                                     .withValue(2)
                                             )
                                     )
                             ).build()
            ),
            Arguments.of("a * b + c",
                         ExpBuilder.fromType(ExpRoot.class)
                             .withValue("a * b + c")
                             .addChild(
                                 ExpBuilder.fromType(ExpAdd.class)
                                     .addChild(
                                         ExpBuilder.fromType(ExpMultiply.class)
                                             .addChild(
                                                 ExpBuilder.fromType(ExpObjPath.class)
                                                     .withValue("a")
                                             )
                                             .addChild(
                                                 ExpBuilder.fromType(ExpObjPath.class)
                                                     .withValue("b"))
                                     ).addChild(
                                         ExpBuilder.fromType(ExpObjPath.class)
                                             .withValue("c")
                                     )
                             ).build()
            ),
            Arguments.of("a * (b + c)",
                         ExpBuilder.fromType(ExpRoot.class)
                             .withValue("a * (b + c)")
                             .addChild(
                                 ExpBuilder.fromType(ExpMultiply.class)
                                     .addChild(
                                         ExpBuilder.fromType(ExpObjPath.class)
                                             .withValue("a")
                                     ).addChild(
                                         ExpBuilder.fromType(ExpAdd.class)
                                             .addChild(
                                                 ExpBuilder.fromType(ExpObjPath.class)
                                                     .withValue("b")
                                             )
                                             .addChild(
                                                 ExpBuilder.fromType(ExpObjPath.class)
                                                     .withValue("c"))
                                     )
                             ).build()
            ),
            Arguments.of("a / b + c",
                         ExpBuilder.fromType(ExpRoot.class)
                             .withValue("a / b + c")
                             .addChild(
                                 ExpBuilder.fromType(ExpAdd.class)
                                     .addChild(
                                         ExpBuilder.fromType(ExpDivide.class)
                                             .addChild(
                                                 ExpBuilder.fromType(ExpObjPath.class)
                                                     .withValue("a")
                                             )
                                             .addChild(
                                                 ExpBuilder.fromType(ExpObjPath.class)
                                                     .withValue("b"))
                                     ).addChild(
                                         ExpBuilder.fromType(ExpObjPath.class)
                                             .withValue("c")
                                     )
                             ).build()
            ),
            Arguments.of("a / (b + c)",
                         ExpBuilder.fromType(ExpRoot.class)
                             .withValue("a / (b + c)")
                             .addChild(
                                 ExpBuilder.fromType(ExpDivide.class)
                                     .addChild(
                                         ExpBuilder.fromType(ExpObjPath.class)
                                             .withValue("a")
                                     ).addChild(
                                         ExpBuilder.fromType(ExpAdd.class)
                                             .addChild(
                                                 ExpBuilder.fromType(ExpObjPath.class)
                                                     .withValue("b")
                                             )
                                             .addChild(
                                                 ExpBuilder.fromType(ExpObjPath.class)
                                                     .withValue("c"))
                                     )
                             ).build()
            ),
            Arguments.of("a & b | c",
                         ExpBuilder.fromType(ExpRoot.class)
                             .withValue("a & b | c")
                             .addChild(
                                 ExpBuilder.fromType(ExpBitwiseOr.class)
                                     .addChild(
                                         ExpBuilder.fromType(ExpBitwiseAnd.class)
                                             .addChild(
                                                 ExpBuilder.fromType(ExpObjPath.class)
                                                     .withValue("a")
                                             )
                                             .addChild(
                                                 ExpBuilder.fromType(ExpObjPath.class)
                                                     .withValue("b"))
                                     ).addChild(
                                         ExpBuilder.fromType(ExpObjPath.class)
                                             .withValue("c")
                                     )
                             ).build()
            ),
            Arguments.of("a & (b | c)",
                         ExpBuilder.fromType(ExpRoot.class)
                             .withValue("a & (b | c)")
                             .addChild(
                                 ExpBuilder.fromType(ExpBitwiseAnd.class)
                                     .addChild(
                                         ExpBuilder.fromType(ExpObjPath.class)
                                             .withValue("a")
                                     ).addChild(
                                         ExpBuilder.fromType(ExpBitwiseOr.class)
                                             .addChild(
                                                 ExpBuilder.fromType(ExpObjPath.class)
                                                     .withValue("b")
                                             )
                                             .addChild(
                                                 ExpBuilder.fromType(ExpObjPath.class)
                                                     .withValue("c"))
                                     )
                             ).build()
            ),
            Arguments.of("a & b ^ c",
                         ExpBuilder.fromType(ExpRoot.class)
                             .withValue("a & b ^ c")
                             .addChild(
                                 ExpBuilder.fromType(ExpBitwiseXor.class)
                                     .addChild(
                                         ExpBuilder.fromType(ExpBitwiseAnd.class)
                                             .addChild(
                                                 ExpBuilder.fromType(ExpObjPath.class)
                                                     .withValue("a")
                                             )
                                             .addChild(
                                                 ExpBuilder.fromType(ExpObjPath.class)
                                                     .withValue("b"))
                                     ).addChild(
                                         ExpBuilder.fromType(ExpObjPath.class)
                                             .withValue("c")
                                     )
                             ).build()
            ),
            Arguments.of("a & (b ^ c)",
                         ExpBuilder.fromType(ExpRoot.class)
                             .withValue("a | (b ^ c)")
                             .addChild(
                                 ExpBuilder.fromType(ExpBitwiseAnd.class)
                                     .addChild(
                                         ExpBuilder.fromType(ExpObjPath.class)
                                             .withValue("a")
                                     ).addChild(
                                         ExpBuilder.fromType(ExpBitwiseXor.class)
                                             .addChild(
                                                 ExpBuilder.fromType(ExpObjPath.class)
                                                     .withValue("b")
                                             )
                                             .addChild(
                                                 ExpBuilder.fromType(ExpObjPath.class)
                                                     .withValue("c"))
                                     )
                             ).build()
            ),
            Arguments.of("a << b & c",
                         ExpBuilder.fromType(ExpRoot.class)
                             .withValue("a << b & c")
                             .addChild(
                                 ExpBuilder.fromType(ExpBitwiseAnd.class)
                                     .addChild(
                                         ExpBuilder.fromType(ExpBitwiseLeftShift.class)
                                             .addChild(
                                                 ExpBuilder.fromType(ExpObjPath.class)
                                                     .withValue("a")
                                             )
                                             .addChild(
                                                 ExpBuilder.fromType(ExpObjPath.class)
                                                     .withValue("b"))
                                     ).addChild(
                                         ExpBuilder.fromType(ExpObjPath.class)
                                             .withValue("c")
                                     )
                             ).build()
            ),
            Arguments.of("a << (b & c)",
                         ExpBuilder.fromType(ExpRoot.class)
                             .withValue("a << (b & c)")
                             .addChild(
                                 ExpBuilder.fromType(ExpBitwiseLeftShift.class)
                                     .addChild(
                                         ExpBuilder.fromType(ExpObjPath.class)
                                             .withValue("a")
                                     ).addChild(
                                         ExpBuilder.fromType(ExpBitwiseAnd.class)
                                             .addChild(
                                                 ExpBuilder.fromType(ExpObjPath.class)
                                                     .withValue("b")
                                             )
                                             .addChild(
                                                 ExpBuilder.fromType(ExpObjPath.class)
                                                     .withValue("c"))
                                     )
                             ).build()
            ),
            Arguments.of("a and b or c",
                         ExpBuilder.fromType(ExpRoot.class)
                             .withValue("a and b or c")
                             .addChild(
                                 ExpBuilder.fromType(ExpOr.class)
                                     .addChild(
                                         ExpBuilder.fromType(ExpAnd.class)
                                             .addChild(
                                                 ExpBuilder.fromType(ExpObjPath.class)
                                                     .withValue("a")
                                             )
                                             .addChild(
                                                 ExpBuilder.fromType(ExpObjPath.class)
                                                     .withValue("b"))
                                     ).addChild(
                                         ExpBuilder.fromType(ExpObjPath.class)
                                             .withValue("c")
                                     )
                             ).build()
            ),
            Arguments.of("a and (b or c)",
                         ExpBuilder.fromType(ExpRoot.class)
                             .withValue("a and (b or c)")
                             .addChild(
                                 ExpBuilder.fromType(ExpAnd.class)
                                     .addChild(
                                         ExpBuilder.fromType(ExpObjPath.class)
                                             .withValue("a")
                                     ).addChild(
                                         ExpBuilder.fromType(ExpOr.class)
                                             .addChild(
                                                 ExpBuilder.fromType(ExpObjPath.class)
                                                     .withValue("b")
                                             )
                                             .addChild(
                                                 ExpBuilder.fromType(ExpObjPath.class)
                                                     .withValue("c"))
                                     )
                             ).build()
            ),
            Arguments.of("t.amount > 0 and t.name like '%story'",
                         ExpBuilder.fromType(ExpRoot.class)
                             .withValue("t.amount > 0 and t.name like '%story'")
                             .addChild(
                                 ExpBuilder.fromType(ExpAnd.class)
                                     .addChild(
                                         ExpBuilder.fromType(ExpGreater.class)
                                             .addChild(
                                                 ExpBuilder.fromType(ExpObjPath.class)
                                                     .withValue("t.amount")
                                             ).addChild(
                                                 ExpBuilder.fromType(ExpScalarInt.class)
                                                     .withValue(0)
                                             )
                                     ).addChild(
                                         ExpBuilder.fromType(ExpLike.class)
                                             .addChild(
                                                 ExpBuilder.fromType(ExpObjPath.class)
                                                     .withValue("t.name")
                                             ).addChild(
                                                 ExpBuilder.fromType(ExpScalarString.class)
                                                     .withValue("%story")
                                             )
                                     )
                             ).build()
            )
        );
    }

    private static String stringify(Node exp) {
        List<String> properties = new ArrayList<>(List.of(
            "id=" + exp.getId(),
            "value=" + exp.jjtGetValue(),
            "parent=" + exp.jjtGetParent(),
            "children=" + IntStream.range(0, exp.jjtGetNumChildren())
                .mapToObj(exp::jjtGetChild)
                .map(CompositeExpTest::stringify)
                .collect(Collectors.joining(", ", "[", "]"))
        ));

        if (exp.getClass() == ExpRoot.class) {
            var expRoot = (ExpRoot) exp;
            properties.add(2, "positionalParams=" + Arrays.toString(expRoot.getPositionalParams()));
            properties.add(3, "namedParams=" + expRoot.getNamedParams().toString());
        }

        return exp + properties.stream().collect(Collectors.joining(", ", "{", "}"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void parseExp(String expString, ExpRoot expected) {
        ExpRoot exp = AgExpressionParser.parse(expString);
        assertEquals(expected, exp, () -> String.format("%nExpected: %s%nActual: %s%n",
                                                        stringify(expected), stringify(exp)));
    }
}
