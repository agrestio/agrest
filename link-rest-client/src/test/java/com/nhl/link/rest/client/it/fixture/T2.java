package com.nhl.link.rest.client.it.fixture;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class T2 {
    public static final String P_BOOLEANS = "booleans";
    public static final String P_INTEGERS = "integers";
    public static final String P_STRINGS = "strings";

    private Collection<Boolean> booleans;
    private List<Integer> integers;
    private Set<String> strings;

    public Collection<Boolean> getBooleans() {
        return booleans;
    }

    public void setBooleans(Collection<Boolean> booleans) {
        this.booleans = booleans;
    }

    public List<Integer> getIntegers() {
        return integers;
    }

    public void setIntegers(List<Integer> integers) {
        this.integers = integers;
    }

    public Set<String> getStrings() {
        return strings;
    }

    public void setStrings(Set<String> strings) {
        this.strings = strings;
    }
}
