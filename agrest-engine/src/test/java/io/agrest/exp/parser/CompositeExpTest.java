package io.agrest.exp.parser;

import io.agrest.exp.AgExpression;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
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
                         expFromType(ExpAbs.class)
                                   .addChild(expFromType(ExpAdd.class)
                                             .addChild(expFromType(ExpScalar.class)
                                                       .withValue(1))
                                             .addChild(expFromType(ExpScalar.class)
                                                       .withValue(2)))
                         .build()
            ),
            Arguments.of("a * b + c",
                         expFromType(ExpAdd.class)
                                   .addChild(expFromType(ExpMultiply.class)
                                             .addChild(expFromType(ExpPath.class)
                                                       .withValue("a"))
                                             .addChild(expFromType(ExpPath.class)
                                                       .withValue("b")))
                                   .addChild(expFromType(ExpPath.class)
                                             .withValue("c"))
                         .build()
            ),
            Arguments.of("a * (b + c)",
                         expFromType(ExpMultiply.class)
                                   .addChild(expFromType(ExpPath.class)
                                             .withValue("a"))
                                   .addChild(expFromType(ExpAdd.class)
                                             .addChild(expFromType(ExpPath.class)
                                                       .withValue("b"))
                                             .addChild(expFromType(ExpPath.class)
                                                       .withValue("c")))
                         .build()
            ),
            Arguments.of("a / b + c",
                         expFromType(ExpAdd.class)
                                   .addChild(expFromType(ExpDivide.class)
                                             .addChild(expFromType(ExpPath.class)
                                                       .withValue("a"))
                                             .addChild(expFromType(ExpPath.class)
                                                       .withValue("b")))
                                   .addChild(expFromType(ExpPath.class)
                                             .withValue("c"))
                         .build()
            ),
            Arguments.of("a / (b + c)",
                         expFromType(ExpDivide.class)
                                   .addChild(expFromType(ExpPath.class)
                                             .withValue("a"))
                                   .addChild(expFromType(ExpAdd.class)
                                             .addChild(expFromType(ExpPath.class)
                                                       .withValue("b"))
                                             .addChild(expFromType(ExpPath.class)
                                                       .withValue("c")))
                         .build()
            ),
            Arguments.of("a & b | c",
                         expFromType(ExpBitwiseOr.class)
                                   .addChild(expFromType(ExpBitwiseAnd.class)
                                             .addChild(expFromType(ExpPath.class)
                                                       .withValue("a"))
                                             .addChild(expFromType(ExpPath.class)
                                                       .withValue("b")))
                                   .addChild(expFromType(ExpPath.class)
                                             .withValue("c"))
                         .build()
            ),
            Arguments.of("a & (b | c)",
                         expFromType(ExpBitwiseAnd.class)
                                   .addChild(expFromType(ExpPath.class)
                                             .withValue("a"))
                                   .addChild(expFromType(ExpBitwiseOr.class)
                                             .addChild(expFromType(ExpPath.class)
                                                       .withValue("b"))
                                             .addChild(expFromType(ExpPath.class)
                                                       .withValue("c")))
                         .build()
            ),
            Arguments.of("a & b ^ c",
                         expFromType(ExpBitwiseXor.class)
                                   .addChild(expFromType(ExpBitwiseAnd.class)
                                             .addChild(expFromType(ExpPath.class)
                                                       .withValue("a"))
                                             .addChild(expFromType(ExpPath.class)
                                                       .withValue("b")))
                                   .addChild(expFromType(ExpPath.class)
                                             .withValue("c"))
                         .build()
            ),
            Arguments.of("a & (b ^ c)",
                         expFromType(ExpBitwiseAnd.class)
                                   .addChild(expFromType(ExpPath.class)
                                             .withValue("a"))
                                   .addChild(expFromType(ExpBitwiseXor.class)
                                             .addChild(expFromType(ExpPath.class)
                                                       .withValue("b"))
                                             .addChild(expFromType(ExpPath.class)
                                                       .withValue("c")))
                         .build()
            ),
            Arguments.of("a << b & c",
                         expFromType(ExpBitwiseAnd.class)
                                   .addChild(expFromType(ExpBitwiseLeftShift.class)
                                             .addChild(expFromType(ExpPath.class)
                                                       .withValue("a"))
                                             .addChild(expFromType(ExpPath.class)
                                                       .withValue("b")))
                                   .addChild(expFromType(ExpPath.class)
                                             .withValue("c"))
                         .build()
            ),
            Arguments.of("a << (b & c)",
                         expFromType(ExpBitwiseLeftShift.class)
                                   .addChild(expFromType(ExpPath.class)
                                             .withValue("a"))
                                   .addChild(expFromType(ExpBitwiseAnd.class)
                                             .addChild(expFromType(ExpPath.class)
                                                       .withValue("b"))
                                             .addChild(expFromType(ExpPath.class)
                                                       .withValue("c")))
                         .build()
            ),
            Arguments.of("a and b or c",
                         expFromType(ExpOr.class)
                                   .addChild(expFromType(ExpAnd.class)
                                             .addChild(expFromType(ExpPath.class)
                                                       .withValue("a"))
                                             .addChild(expFromType(ExpPath.class)
                                                       .withValue("b")))
                                   .addChild(expFromType(ExpPath.class)
                                             .withValue("c"))
                         .build()
            ),
            Arguments.of("a and (b or c)",
                         expFromType(ExpAnd.class)
                                   .addChild(expFromType(ExpPath.class)
                                             .withValue("a"))
                                   .addChild(expFromType(ExpOr.class)
                                             .addChild(expFromType(ExpPath.class)
                                                       .withValue("b"))
                                             .addChild(expFromType(ExpPath.class)
                                                       .withValue("c")))
                         .build()
            ),
            Arguments.of("t.amount > 0 and t.name like '%story'",
                         expFromType(ExpAnd.class)
                                   .addChild(expFromType(ExpGreater.class)
                                             .addChild(expFromType(ExpPath.class)
                                                       .withValue("t.amount"))
                                             .addChild(expFromType(ExpScalar.class)
                                                       .withValue(0)))
                                   .addChild(expFromType(ExpLike.class)
                                             .addChild(expFromType(ExpPath.class)
                                                       .withValue("t.name"))
                                             .addChild(expFromType(ExpScalar.class)
                                                       .withValue("%story")))
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

        return exp + properties.stream().collect(Collectors.joining(", ", "{", "}"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void parseExp(String expString, AgExpression expected) {
        AgExpression exp = AgExpressionParser.parse(expString);
        assertEquals(expected, exp, () -> String.format("%nExpected: %s%nActual: %s%n",
                                                        stringify(expected), stringify(exp)));
    }
}
