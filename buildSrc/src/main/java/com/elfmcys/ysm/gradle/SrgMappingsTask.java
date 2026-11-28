/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.gradle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

/**
 * Forge up to 1.20.4 runs on SRG member names (Mojang class names); YSM is written against Mojang's names. This writes
 * the Mojang-to-SRG member mapping (TSRG v1) for one Minecraft version by chaining Mojang's ProGuard mappings
 * (Mojang -> obfuscated) with MCPConfig's joined.tsrg (obfuscated -> SRG). The Mixin annotation processor (refmap) and
 * the jar renaming step read it.
 */
@CacheableTask
public abstract class SrgMappingsTask extends DefaultTask {
    /** MCPConfig zip (config/joined.tsrg inside). */
    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getMcpConfig();

    /** Mojang's client mappings (ProGuard format). */
    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getClientMappings();

    @OutputFile
    public abstract RegularFileProperty getOutput();

    private record Field(String name, String obf) {}

    private record Method(String name, String obf, List<String> types) {}

    private static final class ProguardClass {
        final String official;
        final String obf;
        final List<Field> fields = new ArrayList<>();
        final List<Method> methods = new ArrayList<>();

        ProguardClass(String official, String obf) {
            this.official = official;
            this.obf = obf;
        }
    }

    @TaskAction
    public void run() throws IOException {
        // obfuscated class -> (obfuscated field -> SRG), (obfuscated method name + obfuscated descriptor -> SRG)
        Map<String, Map<String, String>> srgFields = new HashMap<>();
        Map<String, Map<String, String>> srgMethods = new HashMap<>();
        try (var zip = new ZipFile(getMcpConfig().get().getAsFile())) {
            var entry = zip.getEntry("config/joined.tsrg");
            if (entry == null)
                throw new GradleException("no config/joined.tsrg in " + getMcpConfig().get().getAsFile());
            try (var reader = new BufferedReader(new InputStreamReader(zip.getInputStream(entry), StandardCharsets.UTF_8))) {
                String line;
                String currentClass = null;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank() || line.startsWith("tsrg2 "))
                        continue;
                    if (line.startsWith("\t\t"))
                        continue; // parameters and "static" markers
                    if (!line.startsWith("\t")) {
                        currentClass = line.trim().split(" ")[0];
                        continue;
                    }
                    var parts = line.trim().split(" ");
                    if (currentClass == null || parts.length < 2)
                        continue;
                    if (parts[1].startsWith("(")) {
                        if (parts.length >= 3)
                            srgMethods.computeIfAbsent(currentClass, k -> new HashMap<>()).put(parts[0] + parts[1], parts[2]);
                    } else {
                        srgFields.computeIfAbsent(currentClass, k -> new HashMap<>()).put(parts[0], parts[1]);
                    }
                }
            }
        }
        // Mojang ProGuard mappings: official names with their obfuscated names
        Map<String, ProguardClass> classes = new LinkedHashMap<>();
        Map<String, String> officialToObf = new HashMap<>();
        ProguardClass current = null;
        for (var line : Files.readAllLines(getClientMappings().get().getAsFile().toPath(), StandardCharsets.UTF_8)) {
            if (line.isEmpty() || line.startsWith("#"))
                continue;
            if (!line.startsWith(" ")) {
                var arrow = line.indexOf(" -> ");
                var official = line.substring(0, arrow).replace('.', '/');
                var obf = line.substring(arrow + 4, line.length() - 1).replace('.', '/');
                current = new ProguardClass(official, obf);
                classes.put(official, current);
                officialToObf.put(official, obf);
                continue;
            }
            if (current == null)
                continue;
            var text = line.trim();
            var arrow = text.indexOf(" -> ");
            var obf = text.substring(arrow + 4);
            var left = text.substring(0, arrow);
            var paren = left.indexOf('(');
            if (paren < 0) {
                var space = left.indexOf(' ');
                current.fields.add(new Field(left.substring(space + 1), obf));
                continue;
            }
            // [line:line:]returnType name(args)[:line[:line]]
            var close = left.indexOf(')');
            var head = left.substring(0, paren);
            var colon = head.lastIndexOf(':');
            if (colon >= 0)
                head = head.substring(colon + 1);
            var space = head.indexOf(' ');
            var returnType = head.substring(0, space);
            var name = head.substring(space + 1);
            var args = left.substring(paren + 1, close);
            var types = new ArrayList<String>();
            if (!args.isEmpty())
                for (var arg : args.split(","))
                    types.add(arg);
            types.add(returnType);
            current.methods.add(new Method(name, obf, types));
        }
        var out = new StringBuilder();
        int fields = 0;
        int methods = 0;
        for (var proguardClass : classes.values()) {
            var fieldMap = srgFields.getOrDefault(proguardClass.obf, Map.of());
            var methodMap = srgMethods.getOrDefault(proguardClass.obf, Map.of());
            var body = new StringBuilder();
            for (var field : proguardClass.fields) {
                var srg = fieldMap.get(field.obf());
                if (srg != null && !srg.equals(field.name())) {
                    body.append('\t').append(field.name()).append(' ').append(srg).append('\n');
                    fields++;
                }
            }
            for (var method : proguardClass.methods) {
                if (method.name().startsWith("<"))
                    continue;
                var officialDesc = descriptor(method.types(), null);
                var obfDesc = descriptor(method.types(), officialToObf);
                var srg = methodMap.get(method.obf() + obfDesc);
                if (srg != null && !srg.equals(method.name())) {
                    body.append('\t').append(method.name()).append(' ').append(officialDesc).append(' ').append(srg)
                        .append('\n');
                    methods++;
                }
            }
            if (body.length() > 0)
                out.append(proguardClass.official).append(' ').append(proguardClass.official).append('\n').append(body);
        }
        var target = getOutput().get().getAsFile().toPath();
        Files.createDirectories(target.getParent());
        Files.writeString(target, out.toString(), StandardCharsets.UTF_8);
        getLogger().lifecycle("Mojang -> SRG: {} fields, {} methods in {} classes", fields, methods, classes.size());
    }

    /** JVM descriptor of (args..., return); class names mapped through {@code rename} when given. */
    private static String descriptor(List<String> types, Map<String, String> rename) {
        var builder = new StringBuilder("(");
        for (int i = 0; i < types.size(); i++) {
            if (i == types.size() - 1)
                builder.append(')');
            builder.append(type(types.get(i), rename));
        }
        return builder.toString();
    }

    private static String type(String type, Map<String, String> rename) {
        var dims = 0;
        while (type.endsWith("[]")) {
            dims++;
            type = type.substring(0, type.length() - 2);
        }
        var base = switch (type) {
            case "boolean" -> "Z";
            case "byte" -> "B";
            case "char" -> "C";
            case "short" -> "S";
            case "int" -> "I";
            case "long" -> "J";
            case "float" -> "F";
            case "double" -> "D";
            case "void" -> "V";
            default -> {
                var internal = type.replace('.', '/');
                if (rename != null)
                    internal = rename.getOrDefault(internal, internal);
                yield "L" + internal + ";";
            }
        };
        return "[".repeat(dims) + base;
    }
}
