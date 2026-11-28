/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.gradle;

import java.io.File;
import java.io.IOException;
import java.util.TreeSet;
import java.util.zip.ZipFile;
import org.gradle.api.GradleException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Release-jar check: the shadow jar relocates bundled libraries under com/elfmcys. A call into code outside the jar
 * (loader, Minecraft) whose descriptor names a relocated type cannot link at run time (the loader's method still names
 * the library's own package) - e.g. Forge's IModInfo.getVersion() returns Maven's ArtifactVersion. Fails the build with
 * every such reference.
 */
public final class RelocationCheck {
    private RelocationCheck() {}

    /** Loader or Minecraft code; JDK methods name no library types (MethodHandle.invoke is signature-polymorphic). */
    private static boolean external(String owner) {
        return !owner.startsWith("com/elfmcys/") && !owner.startsWith("[") && !owner.startsWith("java/")
            && !owner.startsWith("javax/") && !owner.startsWith("jdk/") && !owner.startsWith("sun/");
    }

    public static void check(File jar, String relocatedPrefix) throws IOException {
        var problems = new TreeSet<String>();
        try (var zip = new ZipFile(jar)) {
            var entries = zip.entries();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                if (!entry.getName().endsWith(".class") || !entry.getName().startsWith("com/elfmcys/"))
                    continue;
                ClassReader reader;
                try (var stream = zip.getInputStream(entry)) {
                    reader = new ClassReader(stream.readAllBytes());
                } catch (IllegalArgumentException unreadable) {
                    continue;
                }
                var className = reader.getClassName();
                reader.accept(new ClassVisitor(Opcodes.ASM9) {
                    @Override
                    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
                        String[] exceptions) {
                        return new MethodVisitor(Opcodes.ASM9) {
                            @Override
                            public void visitMethodInsn(int opcode, String owner, String method, String desc,
                                boolean isInterface) {
                                if (external(owner) && desc.contains(relocatedPrefix))
                                    problems.add(className + " -> " + owner + "." + method + desc);
                            }

                            @Override
                            public void visitFieldInsn(int opcode, String owner, String field, String desc) {
                                if (external(owner) && desc.contains(relocatedPrefix))
                                    problems.add(className + " -> " + owner + "." + field + ":" + desc);
                            }
                        };
                    }
                }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            }
        }
        if (!problems.isEmpty())
            throw new GradleException("release jar: calls into loader/Minecraft code naming relocated types (cannot link): "
                + String.join("; ", problems));
    }
}
