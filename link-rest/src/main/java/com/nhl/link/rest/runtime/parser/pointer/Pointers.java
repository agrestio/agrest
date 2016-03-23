package com.nhl.link.rest.runtime.parser.pointer;

class Pointers {

    static final String PATH_SEPARATOR = ".";
    static final String RELATIONSHIP_SEPARATOR = ":";

    static String unescape(String s) {
        return s.replace(PATH_SEPARATOR + PATH_SEPARATOR, PATH_SEPARATOR)
                .replace(RELATIONSHIP_SEPARATOR + RELATIONSHIP_SEPARATOR, RELATIONSHIP_SEPARATOR);
    }

    static String escape(String s) {
        return s.replace(PATH_SEPARATOR, PATH_SEPARATOR + PATH_SEPARATOR)
                .replace(RELATIONSHIP_SEPARATOR, RELATIONSHIP_SEPARATOR + RELATIONSHIP_SEPARATOR);
    }

    static String buildPath(String... strings) {

        String result = "";
        for (int i = 0; i < strings.length; i++) {
            result += strings[i];
            if (i < strings.length - 1) {
                result += PATH_SEPARATOR;
            }
        }
        return result;
    }

    static String buildRelationship(String rel, Object id) {
        return rel + RELATIONSHIP_SEPARATOR + id;
    }
}
