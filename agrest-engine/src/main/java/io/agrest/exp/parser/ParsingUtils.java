package io.agrest.exp.parser;

import java.util.HashMap;
import java.util.Map;

public final class ParsingUtils {

    private ParsingUtils() {
    }

    static void processPathAliases(ExpPath pathExp) throws ParseException {
        String path = pathExp.getPath();
        if (!path.contains("#")) {
            return;
        }

        String[] pathSegments = path.split("\\.");
        Map<String, String> aliasMap = new HashMap<>();
        for (int i = 0; i < pathSegments.length; i++) {
            if (pathSegments[i].contains("#")) {
                String[] splitSegment = pathSegments[i].split("#");
                if (splitSegment[1].endsWith("+")) {
                    splitSegment[0] += '+';
                    splitSegment[1] = splitSegment[1].substring(0, splitSegment[1].length() - 1);
                }
                String previousAlias = aliasMap.putIfAbsent(splitSegment[1], splitSegment[0]);
                if (previousAlias != null && !previousAlias.equals(splitSegment[0])) {
                    throw new ParseException("Can't add the same alias to different path segments.");
                }
                pathSegments[i] = splitSegment[1];
            }
        }
        pathExp.setPath(String.join(".", pathSegments));
        pathExp.setPathAliases(aliasMap);
    }
}
