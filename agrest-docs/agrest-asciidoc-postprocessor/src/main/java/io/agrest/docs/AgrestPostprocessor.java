package io.agrest.docs;

import org.asciidoctor.Options;
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Postprocessor;
import org.jsoup.Jsoup;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Map;

public class AgrestPostprocessor extends Postprocessor {

    private static final String FRONT_MATTER = "front-matter";
    private static final String EMPTY_FRONT_MATTER = "---\n---\n\n";

    @Override
    public String process(Document document, String output) {
            output = extractTableOfContents(document, output);
            output = processHeader(document, output);
            return output;
    }

    protected String processHeader(Document document, String output) {
        String headerFile = (String) document.getAttribute("agrest-header", "");

        if(headerFile.isEmpty()) {
            return output;
        }

        String header;
        // inject empty front matter
        if(FRONT_MATTER.equals(headerFile.trim())) {
            header = EMPTY_FRONT_MATTER ;
        } else {
            // treat as a file
            header = document.readAsset(headerFile, Collections.emptyMap());
        }

        return header + output;
    }

    protected String extractTableOfContents(Document document, String output) {
        int start = output.indexOf("<div id=\"toc\" class=\"toc\">");
        if(start == -1) {
            // no toc found, exit
            return output;
        }

        String tocEndString = "</ul>\n</div>";
        int end = output.indexOf(tocEndString, start);
        if(end == -1) {
            // bad, no end..
            return output;
        }

        end += tocEndString.length() + 1;

        org.jsoup.nodes.Document tocDoc = Jsoup.parseBodyFragment(output.substring(start, end));
        tocDoc.select("ul").addClass("nav");
        tocDoc.select("a").addClass("nav-link");
        tocDoc.select("div#toc").addClass("toc-side");
        String toc = tocDoc.body().html();

        Object destDir = document.getOptions().get(Options.DESTINATION_DIR);
        Object docname = ((Map)document.getOptions().get(Options.ATTRIBUTES)).get("docname");

        Path path = FileSystems.getDefault().getPath((String) destDir, docname + ".toc.html");
        StandardOpenOption[] options = {
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE
        };
        try(BufferedWriter br = Files.newBufferedWriter(path, options)) {
            br.write(toc, 0, toc.length());
            br.flush();
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }

        if(start == 0) {
            return output.substring(end);
        }

        return output.substring(0, start) + output.substring(end);
    }
}
