package com.nhl.link.rest.runtime.parser.pointer;

public class Pointers {

    static final String PATH_SEPARATOR = ".";
    static final String ID_SEPARATOR = ":";

    static String unescape(String s) {
        return s.replace(PATH_SEPARATOR + PATH_SEPARATOR, PATH_SEPARATOR)
                .replace(ID_SEPARATOR + ID_SEPARATOR, ID_SEPARATOR);
    }

    static String escape(String s) {
        return s.replace(PATH_SEPARATOR, PATH_SEPARATOR + PATH_SEPARATOR)
                .replace(ID_SEPARATOR, ID_SEPARATOR + ID_SEPARATOR);
    }

    static String concat(String s1, String s2) {
        return s1 + PATH_SEPARATOR + s2;
    }

    static String concatRelationship(String rel, Object id) {
        return rel + ID_SEPARATOR + id;
    }
}
