/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.gradle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.MethodRemapper;
import org.objectweb.asm.commons.Remapper;

/**
 * Renames a jar's references to Minecraft members from Mojang to SRG names (Forge up to 1.20.4 runs on SRG member
 * names), as ForgeGradle 6's reobfJar did. Member lookups walk the class hierarchy (the jar's own classes and the
 * libraries), so overrides of Minecraft methods in YSM's classes and lambdas implementing Minecraft interfaces are
 * renamed too. The mapping is TSRG v1 (class blocks; "name newName" fields, "name desc newName" methods).
 */
public abstract class SrgRemapTask extends DefaultTask {
    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getInput();

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getMappings();

    /** Jars providing the class hierarchy (Minecraft, Forge and their libraries). */
    @Classpath
    public abstract ConfigurableFileCollection getLibraries();

    @OutputFile
    public abstract RegularFileProperty getOutput();

    private final Map<String, Map<String, String>> fields = new HashMap<>();
    private final Map<String, Map<String, String>> methods = new HashMap<>();
    /** class -> [superclass, interfaces...] (null superclass for java/lang/Object or unknown) */
    private final Map<String, List<String>> parents = new HashMap<>();
    private final Map<String, byte[]> inputClasses = new HashMap<>();
    private final Map<String, File> libraryIndex = new HashMap<>();
    private final Map<File, ZipFile> openLibraries = new HashMap<>();

    @TaskAction
    public void run() throws IOException {
        readMappings();
        for (var library : getLibraries().getFiles()) {
            if (!library.getName().endsWith(".jar"))
                continue;
            try (var zip = new ZipFile(library)) {
                var entries = zip.entries();
                while (entries.hasMoreElements()) {
                    var name = entries.nextElement().getName();
                    if (name.endsWith(".class") && !name.startsWith("META-INF/"))
                        libraryIndex.putIfAbsent(name.substring(0, name.length() - 6), library);
                }
            }
        }
        var input = getInput().get().getAsFile();
        var output = getOutput().get().getAsFile();
        var renamed = 0;
        try (var in = new ZipFile(input)) {
            var entries = in.entries();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                if (entry.getName().endsWith(".class") && !entry.getName().startsWith("META-INF/versions/")) {
                    try (var stream = in.getInputStream(entry)) {
                        inputClasses.put(entry.getName().substring(0, entry.getName().length() - 6), stream.readAllBytes());
                    }
                }
            }
            var remapper = new SrgRemapper();
            Files.createDirectories(output.toPath().getParent());
            try (var out = new ZipOutputStream(Files.newOutputStream(output.toPath()))) {
                entries = in.entries();
                while (entries.hasMoreElements()) {
                    var entry = entries.nextElement();
                    var copy = new ZipEntry(entry.getName());
                    copy.setTime(entry.getTime());
                    out.putNextEntry(copy);
                    var className = entry.getName().endsWith(".class")
                        ? entry.getName().substring(0, entry.getName().length() - 6) : null;
                    if (className != null && inputClasses.containsKey(className)) {
                        var before = inputClasses.get(className);
                        var after = remap(before, remapper);
                        if (!java.util.Arrays.equals(before, after))
                            renamed++;
                        out.write(after);
                    } else if (!entry.isDirectory()) {
                        try (var stream = in.getInputStream(entry)) {
                            stream.transferTo(out);
                        }
                    }
                    out.closeEntry();
                }
            }
        } finally {
            for (var zip : openLibraries.values())
                zip.close();
        }
        getLogger().lifecycle("SRG names: {} of {} classes changed ({} library classes indexed)", renamed,
            inputClasses.size(), libraryIndex.size());
        // the hierarchy must reach Minecraft: a missing Minecraft class would leave inherited members in Mojang names
        for (var probe : List.of("net/minecraft/server/level/ServerPlayer", "net/minecraft/world/entity/Entity"))
            if (!libraryIndex.containsKey(probe))
                throw new org.gradle.api.GradleException("SRG renaming: " + probe + " not on the library classpath");
    }

    private void readMappings() throws IOException {
        String owner = null;
        for (var line : Files.readAllLines(getMappings().get().getAsFile().toPath(), StandardCharsets.UTF_8)) {
            if (line.isBlank() || line.startsWith("#"))
                continue;
            if (!line.startsWith("\t")) {
                owner = line.trim().split(" ")[0];
                continue;
            }
            if (line.startsWith("\t\t") || owner == null)
                continue;
            var parts = line.trim().split(" ");
            if (parts.length == 2)
                fields.computeIfAbsent(owner, k -> new HashMap<>()).put(parts[0], parts[1]);
            else if (parts.length == 3)
                methods.computeIfAbsent(owner, k -> new HashMap<>()).put(parts[0] + parts[1], parts[2]);
        }
    }

    private byte[] remap(byte[] bytes, SrgRemapper srg) {
        var reader = new ClassReader(bytes);
        var writer = new ClassWriter(0);
        ClassVisitor visitor = new ClassRemapper(writer, srg) {
            @Override
            protected MethodVisitor createMethodRemapper(MethodVisitor methodVisitor) {
                return new MethodRemapper(methodVisitor, srg) {
                    @Override
                    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrap, Object... args) {
                        // lambdas / method references: the interface method's name, looked up on the interface with
                        // the erased method type the metafactory receives
                        if ("java/lang/invoke/LambdaMetafactory".equals(bootstrap.getOwner()) && args.length > 0
                            && args[0] instanceof Type samType) {
                            var owner = Type.getReturnType(descriptor).getInternalName();
                            var mapped = srg.lookupMethod(owner, name, samType.getDescriptor());
                            if (mapped != null)
                                name = mapped;
                        }
                        super.visitInvokeDynamicInsn(name, descriptor, bootstrap, args);
                    }
                };
            }
        };
        reader.accept(visitor, 0);
        return writer.toByteArray();
    }

    private List<String> parentsOf(String className) {
        return parents.computeIfAbsent(className, name -> {
            var bytes = inputClasses.get(name);
            if (bytes == null) {
                var library = libraryIndex.get(name);
                if (library == null)
                    return List.of();
                try {
                    var zip = openLibraries.get(library);
                    if (zip == null) {
                        zip = new ZipFile(library);
                        openLibraries.put(library, zip);
                    }
                    try (InputStream stream = zip.getInputStream(zip.getEntry(name + ".class"))) {
                        bytes = stream.readAllBytes();
                    }
                } catch (IOException failure) {
                    throw new java.io.UncheckedIOException(failure);
                }
            }
            ClassReader reader;
            try {
                reader = new ClassReader(bytes);
            } catch (IllegalArgumentException unreadable) {
                // a class file newer than this ASM reads (integration APIs built for newer Java): no Minecraft parents
                return List.of();
            }
            var result = new ArrayList<String>();
            if (reader.getSuperName() != null)
                result.add(reader.getSuperName());
            result.addAll(List.of(reader.getInterfaces()));
            return result;
        });
    }

    private final class SrgRemapper extends Remapper {
        SrgRemapper() {
            super();
        }

        /** The SRG name of name+desc on owner or its supertypes, or null. */
        String lookupMethod(String owner, String name, String descriptor) {
            if (name.startsWith("<"))
                return null;
            var key = name + descriptor;
            var seen = new HashSet<String>();
            var queue = new ArrayDeque<String>();
            queue.add(owner);
            while (!queue.isEmpty()) {
                var current = queue.poll();
                if (!seen.add(current))
                    continue;
                var mapped = methods.get(current);
                if (mapped != null && mapped.containsKey(key))
                    return mapped.get(key);
                queue.addAll(parentsOf(current));
            }
            return null;
        }

        @Override
        public String mapMethodName(String owner, String name, String descriptor) {
            if (owner.startsWith("["))
                return name;
            var mapped = lookupMethod(owner, name, descriptor);
            return mapped != null ? mapped : name;
        }

        @Override
        public String mapFieldName(String owner, String name, String descriptor) {
            var seen = new HashSet<String>();
            var queue = new ArrayDeque<String>();
            queue.add(owner);
            while (!queue.isEmpty()) {
                var current = queue.poll();
                if (!seen.add(current))
                    continue;
                var mapped = fields.get(current);
                if (mapped != null && mapped.containsKey(name))
                    return mapped.get(name);
                queue.addAll(parentsOf(current));
            }
            return name;
        }
    }
}
