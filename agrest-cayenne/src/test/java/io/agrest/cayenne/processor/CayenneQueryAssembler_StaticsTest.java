package io.agrest.cayenne.processor;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CayenneQueryAssembler_StaticsTest {

    @Test
    public void consumeRangeIterator_NoOffset_NoLimit_Negative() {

        List<String> list = asList("a", "b", "c", "d");
        List<String> consumed = new ArrayList<>();
        CayenneQueryAssembler.consumeRangeIterator(list.iterator(), -1, -1, consumed::add);
        assertEquals(asList("a", "b", "c", "d"), consumed);
    }

    @Test
    public void consumeRangeIterator_NoOffset_NoLimit() {

        List<String> list = asList("a", "b", "c", "d");
        List<String> consumed = new ArrayList<>();
        CayenneQueryAssembler.consumeRangeIterator(list.iterator(), 0, 0, consumed::add);
        assertEquals(asList("a", "b", "c", "d"), consumed);
    }

    @Test
    public void consumeRangeIterator_Offset_Limit() {

        List<String> list = asList("a", "b", "c", "d");
        List<String> consumed = new ArrayList<>();
        CayenneQueryAssembler.consumeRangeIterator(list.iterator(), 1, 2, consumed::add);
        assertEquals(asList("b", "c"), consumed);
    }

    @Test
    public void consumeRangeIterator_Offset_NoLimit() {

        List<String> list = asList("a", "b", "c", "d");
        List<String> consumed = new ArrayList<>();
        CayenneQueryAssembler.consumeRangeIterator(list.iterator(), 1, 0, consumed::add);
        assertEquals(asList("b", "c", "d"), consumed);
    }


    @Test
    public void consumeRangeList_NoOffset_NoLimit_Negative() {

        List<String> list = asList("a", "b", "c", "d");
        List<String> consumed = new ArrayList<>();
        CayenneQueryAssembler.consumeRangeList(list, -1, -1, consumed::add);
        assertEquals(asList("a", "b", "c", "d"), consumed);
    }

    @Test
    public void consumeRangeList_NoOffset_NoLimit() {

        List<String> list = asList("a", "b", "c", "d");
        List<String> consumed = new ArrayList<>();
        CayenneQueryAssembler.consumeRangeList(list, 0, 0, consumed::add);
        assertEquals(asList("a", "b", "c", "d"), consumed);
    }

    @Test
    public void consumeRangeList_Offset_Limit() {

        List<String> list = asList("a", "b", "c", "d");
        List<String> consumed = new ArrayList<>();
        CayenneQueryAssembler.consumeRangeList(list, 1, 2, consumed::add);
        assertEquals(asList("b", "c"), consumed);
    }

    @Test
    public void consumeRangeList_Offset_NoLimit() {

        List<String> list = asList("a", "b", "c", "d");
        List<String> consumed = new ArrayList<>();
        CayenneQueryAssembler.consumeRangeList(list, 1, 0, consumed::add);
        assertEquals(asList("b", "c", "d"), consumed);
    }

}
