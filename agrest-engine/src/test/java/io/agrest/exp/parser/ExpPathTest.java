package io.agrest.exp.parser;

import io.agrest.AgException;
import io.agrest.exp.AgExpressionException;
import io.agrest.protocol.Exp;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExpPathTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "a",
            "A",
            "_",
            "a.b",
            "a.b.c",
            "a0",
            "a$",
            "a+",
            "a0$b+._c#d+",

            "year",
            "month",
            "week",
            "day_of_year",
            "day",
            "day_of_month",
            "day_of_week",
            "hour",
            "minute",
            "second",

            "current_date",
            "current_time",
            "current_timestamp",

            "concat",
            "substring",
            "trim",
            "lower",
            "upper",

            "length",
            "locate",
            "abs",
            "sqrt",
            "mod"
    })
    void parse(String expString) {
        assertEquals(ExpPath.class, Exp.parse(expString).getClass());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "a|a",
            " a  |a"
    })
    public void parsedToString(String expString, String expected) {
        assertEquals(expected, Exp.parse(expString).toString());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "a.b.c.d|a.b.c.d",
            "a.b+.c+.d|a.b+.c+.d",
            "a.b.c#p1.d#p2|a.b.p1.p2|p1-c|p2-d",
            "a.b+.c#p1+.d#p2|a.b+.p1.p2|p1-c+|p2-d",
    })
    public void pathAliases(String expString, String expectedPath, @AggregateWith(VarargsAggregator.class) String... expectedAliases) {
        ExpPath expPath = new ExpPath(expString);
        assertEquals(expectedPath, expPath.getPath());
        assertEquals(expectedAliases.length, expPath.getPathAliases().size());
        for (String expectedAlias : expectedAliases) {
            String[] aliasMapping = expectedAlias.split("-");
            assertEquals(aliasMapping[1], expPath.getPathAliases().get(aliasMapping[0]));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "0a",
            "a++",
            ".",
            ".b",
            "a..b",
            "a . b",
            "#a",
            "a#0",
            "a#a#a"
    })
    public void parseInvalidGrammar(String expString) {
        assertThrows(AgException.class, () -> Exp.parse(expString));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "a.b.c#p1.d#p1"
    })
    public void parseInvalidAliases(String expString) {
        assertThrows(AgExpressionException.class, () -> Exp.path(expString));
    }

    static class VarargsAggregator implements ArgumentsAggregator {
        @Override
        public Object aggregateArguments(ArgumentsAccessor accessor, ParameterContext context) {
            return accessor.toList().stream()
                    .skip(context.getIndex())
                    .map(String::valueOf)
                    .toArray(String[]::new);
        }
    }
}
