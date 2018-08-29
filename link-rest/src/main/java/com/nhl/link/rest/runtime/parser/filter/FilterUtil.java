package com.nhl.link.rest.runtime.parser.filter;

/**
 * @since 1.5
 * @deprecated since 2.13 deprecated. It is only used in Sencha extensions, so a new
 * com.nhl.link.rest.sencha.ops.FilterUtil was created.
 */
@Deprecated
public class FilterUtil {
    private static final char undescore = '_';
    private static final String undescoreStr = String.valueOf(undescore);
    private static final char percent = '%';
    private static final String percentStr = String.valueOf(percent);

    public static String escapeValueForLike(String value) {
        if (!value.contains(undescoreStr) && !value.contains(percentStr)) {
            return value;
        }

        int len = value.length();

        StringBuilder out = new StringBuilder(len * 2); // prevent array copying
        for (int i = 0; i < len; i++) {
            char c = value.charAt(i);
            if (c == undescore || c == percent) {
                out.append('\\');
            }

            out.append(c);
        }

        return out.toString();
    }
}
