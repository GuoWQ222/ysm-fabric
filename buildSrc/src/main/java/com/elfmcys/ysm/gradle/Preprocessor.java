/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.gradle;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

/**
 * Line-preserving source preprocessor for the Minecraft-version and loader axes of the Fabric port's multi-version
 * build. The checked-in tree is the 26.2 Fabric variant; other targets compile a processed copy.
 *
 * <pre>
 * //#if MC &gt;= 26.3
 * code for 26.3 and later
 * //#elseif MC &lt; 26.2 &amp;&amp; LOADER == neoforge
 * //$$ code for NeoForge 26.1.x
 * //#else
 * //$$ code for everything else
 * //#endif
 * </pre>
 *
 * Inactive lines carry the marker {@code //$$ } after their indentation; active lines carry none. Directive lines stay
 * as they are, so line numbers match the checked-in file. Conditions: {@code MC <op> <version>} with == != &lt; &lt;=
 * &gt; &gt;=, {@code LOADER == | != <name>}, joined by {@code &&} (binds tighter) and {@code ||}, each term optionally
 * negated with {@code !}.
 */
public final class Preprocessor {
    public static final String MARKER = "//$$ ";

    private final int[] mc;
    private final String loader;

    public Preprocessor(String mcVersion, String loader) {
        this.mc = parseVersion(mcVersion);
        this.loader = loader.toLowerCase(Locale.ROOT);
    }

    /**
     * Applies a rename table (pairs of old and new names, e.g. moved classes or packages) to whole names only: an entry
     * ending with '.' is a package prefix; any other entry must not be followed by an identifier character.
     */
    public static String remap(String text, List<String[]> pairs) {
        for (var pair : pairs) {
            if (pair.length == 3) {
                text = java.util.regex.Pattern.compile(pair[1]).matcher(text).replaceAll(pair[2]);
                continue;
            }
            var from = pair[0];
            var to = pair[1];
            // "~name": a member (method or field) name, also renamed after a '.' (calls like super.name(...))
            boolean member = from.startsWith("~");
            if (member)
                from = from.substring(1);
            int index = text.indexOf(from);
            if (index < 0)
                continue;
            var out = new StringBuilder(text.length() + 64);
            int start = 0;
            while (index >= 0) {
                int end = index + from.length();
                char previous = index == 0 ? ' ' : text.charAt(index - 1);
                // an entry edge that is not a name character (".identifier()") needs no boundary on that side
                boolean before = !Character.isJavaIdentifierPart(from.charAt(0))
                    || (member ? !Character.isJavaIdentifierPart(previous) : !isNamePart(previous));
                boolean after = !Character.isJavaIdentifierPart(from.charAt(from.length() - 1)) || end >= text.length()
                    || !Character.isJavaIdentifierPart(text.charAt(end));
                if (before && after) {
                    out.append(text, start, index).append(to);
                    start = end;
                }
                index = text.indexOf(from, end);
            }
            text = out.append(text, start, text.length()).toString();
        }
        return text;
    }

    private static boolean isNamePart(char c) {
        return Character.isJavaIdentifierPart(c) || c == '.';
    }

    /** Reads "old new" lines (# comments and blank lines ignored). */
    public static List<String[]> parseRemap(String text) {
        var pairs = new ArrayList<String[]>();
        for (var line : text.split("\n")) {
            var trimmed = line.strip();
            if (trimmed.isEmpty() || trimmed.startsWith("#"))
                continue;
            // "regex<TAB>pattern<TAB>replacement": a Java regex over the whole file ($1.. groups)
            if (trimmed.startsWith("regex\t")) {
                var regex = trimmed.split("\t", -1);
                if (regex.length != 3)
                    throw new IllegalArgumentException("bad regex remap line '" + trimmed + "'");
                pairs.add(regex);
                continue;
            }
            var parts = trimmed.split("\\s+");
            if (parts.length != 2)
                throw new IllegalArgumentException("bad remap line '" + trimmed + "'");
            pairs.add(parts);
        }
        return pairs;
    }

    /** True when the text contains directives (otherwise it can be copied unchanged). */
    public static boolean hasDirectives(String text) {
        return text.contains("//#if");
    }

    public String process(String text, String name) {
        var lines = text.split("\n", -1);
        var out = new StringBuilder(text.length() + 64);
        // one frame per open block: whether its parent is active, whether a branch was taken, whether the current
        // branch is active
        Deque<boolean[]> stack = new ArrayDeque<>();
        for (int index = 0; index < lines.length; index++) {
            var line = lines[index];
            var trimmed = line.strip();
            try {
                if (trimmed.startsWith("//#if ")) {
                    boolean parent = stack.isEmpty() || stack.peek()[2];
                    boolean value = evaluate(trimmed.substring(6));
                    stack.push(new boolean[] {parent, value, parent && value});
                    out.append(line);
                } else if (trimmed.startsWith("//#elseif ")) {
                    var frame = top(stack);
                    boolean value = !frame[1] && evaluate(trimmed.substring(10));
                    frame[1] |= value;
                    frame[2] = frame[0] && value;
                    out.append(line);
                } else if (trimmed.equals("//#else")) {
                    var frame = top(stack);
                    frame[2] = frame[0] && !frame[1];
                    frame[1] = true;
                    out.append(line);
                } else if (trimmed.equals("//#endif")) {
                    top(stack);
                    stack.pop();
                    out.append(line);
                } else if (trimmed.startsWith("//#")) {
                    throw new IllegalArgumentException("unknown directive " + trimmed);
                } else if (stack.isEmpty() || stack.peek()[2]) {
                    out.append(uncomment(line));
                } else {
                    out.append(comment(line));
                }
            } catch (IllegalArgumentException failure) {
                throw new IllegalArgumentException(name + ":" + (index + 1) + ": " + failure.getMessage(), failure);
            }
            if (index < lines.length - 1)
                out.append('\n');
        }
        if (!stack.isEmpty())
            throw new IllegalArgumentException(name + ": unterminated //#if");
        return out.toString();
    }

    private static boolean[] top(Deque<boolean[]> stack) {
        if (stack.isEmpty())
            throw new IllegalArgumentException("directive outside //#if");
        return stack.peek();
    }

    static String uncomment(String line) {
        int indent = indentation(line);
        if (line.startsWith(MARKER, indent))
            return line.substring(0, indent) + line.substring(indent + MARKER.length());
        if (line.startsWith(MARKER.strip(), indent) && line.strip().equals(MARKER.strip()))
            return line.substring(0, indent);
        return line;
    }

    static String comment(String line) {
        if (line.isBlank())
            return line;
        int indent = indentation(line);
        if (line.startsWith(MARKER.strip(), indent))
            return line;
        return line.substring(0, indent) + MARKER + line.substring(indent);
    }

    private static int indentation(String line) {
        int indent = 0;
        while (indent < line.length() && (line.charAt(indent) == ' ' || line.charAt(indent) == '\t'))
            indent++;
        return indent;
    }

    boolean evaluate(String expression) {
        for (var alternative : expression.split("\\|\\|")) {
            boolean all = true;
            for (var term : alternative.split("&&"))
                all &= term(term.strip());
            if (all)
                return true;
        }
        return false;
    }

    private boolean term(String term) {
        if (term.startsWith("!"))
            return !term(term.substring(1).strip());
        var parts = term.split("\\s+");
        if (parts.length != 3)
            throw new IllegalArgumentException("bad condition '" + term + "'");
        switch (parts[0]) {
            case "MC" -> {
                int compare = compare(mc, parseVersion(parts[2]));
                return switch (parts[1]) {
                    case "==" -> compare == 0;
                    case "!=" -> compare != 0;
                    case "<" -> compare < 0;
                    case "<=" -> compare <= 0;
                    case ">" -> compare > 0;
                    case ">=" -> compare >= 0;
                    default -> throw new IllegalArgumentException("bad operator '" + parts[1] + "'");
                };
            }
            case "LOADER" -> {
                boolean equal = loader.equals(parts[2].toLowerCase(Locale.ROOT));
                return switch (parts[1]) {
                    case "==" -> equal;
                    case "!=" -> !equal;
                    default -> throw new IllegalArgumentException("LOADER supports == and != only");
                };
            }
            default -> throw new IllegalArgumentException("unknown variable '" + parts[0] + "'");
        }
    }

    static int[] parseVersion(String version) {
        var parts = version.strip().split("\\.");
        var result = new int[3];
        for (int index = 0; index < parts.length && index < 3; index++)
            result[index] = Integer.parseInt(parts[index]);
        return result;
    }

    private static int compare(int[] a, int[] b) {
        for (int index = 0; index < 3; index++)
            if (a[index] != b[index])
                return Integer.compare(a[index], b[index]);
        return 0;
    }

    /** Self-check: java Preprocessor.java (no Gradle needed). */
    public static void main(String[] args) {
        var source = String.join("\n",
            "class A {",
            "    //#if MC >= 26.3",
            "    //$$ int v = 263;",
            "    //#elseif MC >= 26.2",
            "    int v = 262;",
            "    //#else",
            "    //$$ int v = 261;",
            "    //#endif",
            "    //#if LOADER == neoforge || MC < 26.2 && LOADER == fabric",
            "    //$$ int x = 1;",
            "    //#endif",
            "}");
        List<String> failures = new ArrayList<>();
        check(failures, new Preprocessor("26.2", "fabric").process(source, "A"), source);
        var v263 = new Preprocessor("26.3", "fabric").process(source, "A");
        expect(failures, v263, "    int v = 263;", "    //$$ int v = 262;", "    //$$ int v = 261;", "    //$$ int x = 1;");
        var v2612 = new Preprocessor("26.1.2", "fabric").process(source, "A");
        expect(failures, v2612, "    //$$ int v = 263;", "    //$$ int v = 262;", "    int v = 261;", "    int x = 1;");
        var neo = new Preprocessor("26.2", "neoforge").process(source, "A");
        expect(failures, neo, "    int v = 262;", "    int x = 1;");
        // round trip: processing the 26.3 output for 26.2 gives the checked-in text back
        check(failures, new Preprocessor("26.2", "fabric").process(v263, "A"), source);
        if (v263.split("\n", -1).length != source.split("\n", -1).length)
            failures.add("line count changed");
        try {
            new Preprocessor("26.2", "fabric").process("//#if MC > 26.1\nx", "B");
            failures.add("unterminated block accepted");
        } catch (IllegalArgumentException expected) {
            // ok
        }
        var pairs = parseRemap("# moved\na.b.VertexFormat x.y.VertexFormat\na.b.textures. x.y.textures.\n");
        var remapped = remap("import a.b.VertexFormat;\nimport a.b.VertexFormatElement;\nimport a.b.textures.GpuTexture;\n"
            + "q.a.b.VertexFormat v; (a.b.VertexFormat)z;", pairs);
        var expectedRemap = "import x.y.VertexFormat;\nimport a.b.VertexFormatElement;\nimport x.y.textures.GpuTexture;\n"
            + "q.a.b.VertexFormat v; (x.y.VertexFormat)z;";
        if (!remapped.equals(expectedRemap))
            failures.add("remap:\n" + remapped);
        System.out.println(failures.isEmpty() ? "PREPROCESSOR OK" : "PREPROCESSOR FAIL " + failures);
        if (!failures.isEmpty())
            System.exit(1);
    }

    private static void check(List<String> failures, String actual, String expected) {
        if (!actual.equals(expected))
            failures.add("round trip:\n" + actual);
    }

    private static void expect(List<String> failures, String actual, String... lines) {
        for (var line : lines)
            if (!actual.contains(line + "\n") && !actual.endsWith(line))
                failures.add("missing [" + line + "] in\n" + actual);
    }
}
