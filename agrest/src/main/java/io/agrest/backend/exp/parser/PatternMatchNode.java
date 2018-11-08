package io.agrest.backend.exp.parser;


import io.agrest.AgException;
import io.agrest.backend.util.RegexUtil;

import java.util.regex.Pattern;

public abstract class PatternMatchNode extends SimpleNode {

    protected transient Pattern pattern;
    protected transient boolean patternCompiled;
    protected boolean ignoringCase;
    protected char escapeChar;

    PatternMatchNode(int i, boolean ignoringCase) {
        super(i);
        this.ignoringCase = ignoringCase;
    }

    PatternMatchNode(int i, boolean ignoringCase, char escapeChar) {
        super(i);
        this.ignoringCase = ignoringCase;
        setEscapeChar(escapeChar);
    }

    /**
     * <p>This method will return an escape character for the like
     * clause.  The escape character will eventually end up in the
     * query as <code>...(t0.foo LIKE ? &#123;escape '|'&#125;)</code> where the
     * pipe symbol is the escape character.</p>
     * <p>Note that having no escape character is represented as
     * the character 0.</p>
     */

    public char getEscapeChar() {
        return escapeChar;
    }

    /**
     * <p>This method allows the setting of the escape character.
     * The escape character can be used in a LIKE clause.  The
     * character 0 signifies no escape character.  The escape
     * character '?' is disallowed.</p>
     */

    public void setEscapeChar(char value) {

        if ('?' == value)
            throw new AgException("the use of the '?' as an escape character in LIKE clauses is disallowed.");

        escapeChar = value;
    }

    protected boolean matchPattern(String string) {
        return (string != null) ? getPattern().matcher(string).find() : false;
    }

    protected Pattern getPattern() {
        // compile pattern on demand
        if (!patternCompiled) {

            synchronized (this) {

                if (!patternCompiled) {
                    pattern = null;

                    if (jjtGetNumChildren() < 2) {
                        patternCompiled = true;
                        return null;
                    }

                    // precompile pattern
                    ASTScalar patternNode = (ASTScalar) jjtGetChild(1);
                    if (patternNode == null) {
                        patternCompiled = true;
                        return null;
                    }

                    String srcPattern = (String) patternNode.getValue();
                    if (srcPattern == null) {
                        patternCompiled = true;
                        return null;
                    }

                    String preprocessed = RegexUtil.sqlPatternToRegex(srcPattern);

                    pattern = Pattern.compile(preprocessed,
                                                (ignoringCase) ? Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE : 0);

                    patternCompiled = true;
                }
            }
        }

        return pattern;
    }

    @Override
    public void jjtAddChild(Node n, int i) {
        // reset pattern if the node is modified
        if (i == 1) {
            patternCompiled = false;
        }

        super.jjtAddChild(n, i);
    }

}
