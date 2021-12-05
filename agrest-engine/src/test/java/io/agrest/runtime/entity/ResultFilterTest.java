package io.agrest.runtime.entity;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class ResultFilterTest {

    @Test
    public void testFilterList_AllMatches() {
        List<Object> unfiltered = asList("a", "b", "c");
        List<Object> filtered = ResultFilter.filterList(unfiltered, o -> true);
        assertSame(unfiltered, filtered, "No list copy should've been created");
    }

    @Test
    public void testFilterList_MatchStart() {
        List<String> unfiltered = asList("a", "b", "ac");
        List<String> filtered = ResultFilter.filterList(unfiltered, o -> o.startsWith("a"));
        assertEquals(asList("a", "ac"), filtered);
    }

    @Test
    public void testFilterList_MatchMiddle() {
        List<String> unfiltered = asList("a", "b", "ac", "bb");
        List<String> filtered = ResultFilter.filterList(unfiltered, o -> o.startsWith("b"));
        assertEquals(asList("b", "bb"), filtered);
    }

    @Test
    public void testFilterList_MatchEnd() {
        List<String> unfiltered = asList("a", "b", "ac", "bb");
        List<String> filtered = ResultFilter.filterList(unfiltered, o -> o.equals("bb"));
        assertEquals(Collections.singletonList("bb"), filtered);
    }

    @Test
    public void testFilterList_NoMatches() {
        List<String> unfiltered = asList("a", "b", "ac", "bb");
        List<String> filtered = ResultFilter.filterList(unfiltered, o -> false);
        assertEquals(new ArrayList<>(), filtered);
    }
}
