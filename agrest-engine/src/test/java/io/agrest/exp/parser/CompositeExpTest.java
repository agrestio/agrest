package io.agrest.exp.parser;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static io.agrest.exp.parser.ExpBuilder.expFromType;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CompositeExpTest {

    static Stream<Arguments> parseExp() {
        return Stream.of(
            Arguments.of("abs(1 + 2)",
                         expFromType(ExpRoot.class)
                         .withValue("abs(1 + 2)")
                         .addChild(expFromType(ExpAbs.class)
                                   .addChild(expFromType(ExpAdd.class)
                                             .addChild(expFromType(ExpScalar.class)
                                                       .withValue(1))
                                             .addChild(expFromType(ExpScalar.class)
                                                       .withValue(2))))
                         .build()
            ),
            Arguments.of("a * b + c",
                         expFromType(ExpRoot.class)
                         .withValue("a * b + c")
                         .addChild(expFromType(ExpAdd.class)
                                   .addChild(expFromType(ExpMultiply.class)
                                             .addChild(expFromType(ExpObjPath.class)
                                                       .withValue("a"))
                                             .addChild(expFromType(ExpObjPath.class)
                                                       .withValue("b")))
                                   .addChild(expFromType(ExpObjPath.class)
                                             .withValue("c")))
                         .build()
            ),
            Arguments.of("a * (b + c)",
                         expFromType(ExpRoot.class)
                         .withValue("a * (b + c)")
                         .addChild(expFromType(ExpMultiply.class)
                                   .addChild(expFromType(ExpObjPath.class)
                                             .withValue("a"))
                                   .addChild(expFromType(ExpAdd.class)
                                             .addChild(expFromType(ExpObjPath.class)
                                                       .withValue("b"))
                                             .addChild(expFromType(ExpObjPath.class)
                                                       .withValue("c"))))
                         .build()
            ),
            Arguments.of("a / b + c",
                         expFromType(ExpRoot.class)
                         .withValue("a / b + c")
                         .addChild(expFromType(ExpAdd.class)
                                   .addChild(expFromType(ExpDivide.class)
                                             .addChild(expFromType(ExpObjPath.class)
                                                       .withValue("a"))
                                             .addChild(expFromType(ExpObjPath.class)
                                                       .withValue("b")))
                                   .addChild(expFromType(ExpObjPath.class)
                                             .withValue("c")))
                         .build()
            ),
            Arguments.of("a / (b + c)",
                         expFromType(ExpRoot.class)
                         .withValue("a / (b + c)")
                         .addChild(expFromType(ExpDivide.class)
                                   .addChild(expFromType(ExpObjPath.class)
                                             .withValue("a"))
                                   .addChild(expFromType(ExpAdd.class)
                                             .addChild(expFromType(ExpObjPath.class)
                                                       .withValue("b"))
                                             .addChild(expFromType(ExpObjPath.class)
                                                       .withValue("c"))))
                         .build()
            ),
            Arguments.of("a & b | c",
                         expFromType(ExpRoot.class)
                         .withValue("a & b | c")
                         .addChild(expFromType(ExpBitwiseOr.class)
                                   .addChild(expFromType(ExpBitwiseAnd.class)
                                             .addChild(expFromType(ExpObjPath.class)
                                                       .withValue("a"))
                                             .addChild(expFromType(ExpObjPath.class)
                                                       .withValue("b")))
                                   .addChild(expFromType(ExpObjPath.class)
                                             .withValue("c")))
                         .build()
            ),
            Arguments.of("a & (b | c)",
                         expFromType(ExpRoot.class)
                         .withValue("a & (b | c)")
                         .addChild(expFromType(ExpBitwiseAnd.class)
                                   .addChild(expFromType(ExpObjPath.class)
                                             .withValue("a"))
                                   .addChild(expFromType(ExpBitwiseOr.class)
                                             .addChild(expFromType(ExpObjPath.class)
                                                       .withValue("b"))
                                             .addChild(expFromType(ExpObjPath.class)
                                                       .withValue("c"))))
                         .build()
            ),
            Arguments.of("a & b ^ c",
                         expFromType(ExpRoot.class)
                         .withValue("a & b ^ c")
                         .addChild(expFromType(ExpBitwiseXor.class)
                                   .addChild(expFromType(ExpBitwiseAnd.class)
                                             .addChild(expFromType(ExpObjPath.class)
                                                       .withValue("a"))
                                             .addChild(expFromType(ExpObjPath.class)
                                                       .withValue("b")))
                                   .addChild(expFromType(ExpObjPath.class)
                                             .withValue("c")))
                         .build()
            ),
            Arguments.of("a & (b ^ c)",
                         expFromType(ExpRoot.class)
                         .withValue("a | (b ^ c)")
                         .addChild(expFromType(ExpBitwiseAnd.class)
                                   .addChild(expFromType(ExpObjPath.class)
                                             .withValue("a"))
                                   .addChild(expFromType(ExpBitwiseXor.class)
                                             .addChild(expFromType(ExpObjPath.class)
                                                       .withValue("b"))
                                             .addChild(expFromType(ExpObjPath.class)
                                                       .withValue("c"))))
                         .build()
            ),
            Arguments.of("a << b & c",
                         expFromType(ExpRoot.class)
                         .withValue("a << b & c")
                         .addChild(expFromType(ExpBitwiseAnd.class)
                                   .addChild(expFromType(ExpBitwiseLeftShift.class)
                                             .addChild(expFromType(ExpObjPath.class)
                                                       .withValue("a"))
                                             .addChild(expFromType(ExpObjPath.class)
                                                       .withValue("b")))
                                   .addChild(expFromType(ExpObjPath.class)
                                             .withValue("c")))
                         .build()
            ),
            Arguments.of("a << (b & c)",
                         expFromType(ExpRoot.class)
                         .withValue("a << (b & c)")
                         .addChild(expFromType(ExpBitwiseLeftShift.class)
                                   .addChild(expFromType(ExpObjPath.class)
                                             .withValue("a"))
                                   .addChild(expFromType(ExpBitwiseAnd.class)
                                             .addChild(expFromType(ExpObjPath.class)
                                                       .withValue("b"))
                                             .addChild(expFromType(ExpObjPath.class)
                                                       .withValue("c"))))
                         .build()
            ),
            Arguments.of("a and b or c",
                         expFromType(ExpRoot.class)
                         .withValue("a and b or c")
                         .addChild(expFromType(ExpOr.class)
                                   .addChild(expFromType(ExpAnd.class)
                                             .addChild(expFromType(ExpObjPath.class)
                                                       .withValue("a"))
                                             .addChild(expFromType(ExpObjPath.class)
                                                       .withValue("b")))
                                   .addChild(expFromType(ExpObjPath.class)
                                             .withValue("c")))
                         .build()
            ),
            Arguments.of("a and (b or c)",
                         expFromType(ExpRoot.class)
                         .withValue("a and (b or c)")
                         .addChild(expFromType(ExpAnd.class)
                                   .addChild(expFromType(ExpObjPath.class)
                                             .withValue("a"))
                                   .addChild(expFromType(ExpOr.class)
                                             .addChild(expFromType(ExpObjPath.class)
                                                       .withValue("b"))
                                             .addChild(expFromType(ExpObjPath.class)
                                                       .withValue("c"))))
                         .build()
            ),
            Arguments.of("t.amount > 0 and t.name like '%story'",
                         expFromType(ExpRoot.class)
                         .withValue("t.amount > 0 and t.name like '%story'")
                         .addChild(expFromType(ExpAnd.class)
                                   .addChild(expFromType(ExpGreater.class)
                                             .addChild(expFromType(ExpObjPath.class)
                                                       .withValue("t.amount"))
                                             .addChild(expFromType(ExpScalar.class)
                                                       .withValue(0)))
                                   .addChild(expFromType(ExpLike.class)
                                             .addChild(expFromType(ExpObjPath.class)
                                                       .withValue("t.name"))
                                             .addChild(expFromType(ExpScalar.class)
                                                       .withValue("%story"))))
                         .build()
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
