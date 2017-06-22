package com.nhl.link.rest.it.fixture.pojo.model;

import com.nhl.link.rest.annotation.LrAttribute;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class P8 {
    public static final String STRING_SET = "stringSet";
    public static final String NUMBER_LIST = "numberList";
    public static final String NUMBER_SUBTYPE_COLLECTION = "numberSubtypeCollection";
    public static final String BOOLEANS = "booleans";
    public static final String DOUBLES = "doubles";
    public static final String CHARACTERS = "characters";


    private Set<String> stringSet;
    private List<Number> numberList;
    private Collection<? extends Number> numberSubtypeCollection;
    private Collection<Boolean> booleans;
    private Collection<Double> doubles;
    private Collection<Character> characters;

    @LrAttribute
    public Set<String> getStringSet() {
        return stringSet;
    }

    public void setStringSet(Set<String> stringSet) {
        this.stringSet = stringSet;
    }

    @LrAttribute
    public List<Number> getNumberList() {
        return numberList;
    }

    public void setNumberList(List<Number> numberList) {
        this.numberList = numberList;
    }

    @LrAttribute
    public Collection<? extends Number> getNumberSubtypeCollection() {
        return numberSubtypeCollection;
    }

    public void setNumberSubtypeCollection(Collection<? extends Number> numberSubtypeCollection) {
        this.numberSubtypeCollection = numberSubtypeCollection;
    }

    @LrAttribute
    public Collection<Boolean> getBooleans() {
        return booleans;
    }

    public void setBooleans(Collection<Boolean> booleans) {
        this.booleans = booleans;
    }

    @LrAttribute
    public Collection<Double> getDoubles() {
        return doubles;
    }

    public void setDoubles(Collection<Double> doubles) {
        this.doubles = doubles;
    }

    @LrAttribute
    public Collection<Character> getCharacters() {
        return characters;
    }

    public void setCharacters(Collection<Character> characters) {
        this.characters = characters;
    }
}
