/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.expression.host;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.net.URI;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Inherited methods of followed (compat) classes. The verifier used to require every referenced method to be declared
 * by the followed owner itself, so an enum's {@code name()} (declared by java.lang.Enum) rejected the Carry On
 * capability. Kept apart from OptionalCapabilityAdmissionTest, which the Fabric build excludes (Forge-era API use).
 */
final class OptionalLinkageVerifierTest {
    private static final Predicate<String> EXTERNAL = name -> name.startsWith("fixture.api.");
    private static final Predicate<String> FOLLOWED = name -> name.startsWith("fixture.") && !EXTERNAL.test(name);

    @TempDir
    Path temporary;

    @Test
    void inheritedMethodsOfFollowedClassesQualify() throws Exception {
        var classes = Files.createDirectories(temporary.resolve("inherited"));
        var classpath = List.of("-classpath", classes.toString());
        compile(classes, "fixture.api.Dependency", """
            package fixture.api;
            public final class Dependency {
                public static int value() { return 7; }
            }
            """, List.of());
        compile(classes, "fixture.Kind", """
            package fixture;
            public enum Kind { FIRST }
            """, classpath);
        compile(classes, "fixture.Base", """
            package fixture;
            public class Base {
                public String base() { return "b"; }
            }
            """, classpath);
        compile(classes, "fixture.Child", """
            package fixture;
            public final class Child extends Base {
            }
            """, classpath);
        compile(classes, "fixture.Component", """
            package fixture;
            public final class Component {
                // name() comes from java.lang.Enum, base() from the followed superclass Base, toString() from Object.
                public String value() {
                    return Kind.FIRST.name() + new Child().base() + fixture.api.Dependency.value() + toString();
                }
            }
            """, classpath);
        try (var loader = loader(jar(classes, temporary.resolve("inherited.jar")))) {
            OptionalLinkageVerifier.verify(loader, Set.of("fixture.Component"), EXTERNAL, FOLLOWED);
        }
    }

    @Test
    void methodMissingFromTheWholeHierarchyStillFails() throws Exception {
        var compileTime = Files.createDirectories(temporary.resolve("compile"));
        var classpath = List.of("-classpath", compileTime.toString());
        compile(compileTime, "fixture.Base", """
            package fixture;
            public class Base {
                public String base() { return "b"; }
            }
            """, List.of());
        compile(compileTime, "fixture.Child", """
            package fixture;
            public final class Child extends Base {
            }
            """, classpath);
        compile(compileTime, "fixture.Component", """
            package fixture;
            public final class Component {
                public String value() { return new Child().base(); }
            }
            """, classpath);
        // At runtime Base lacks base(), so Child inherits nothing.
        var runtime = Files.createDirectories(temporary.resolve("runtime"));
        compile(runtime, "fixture.Base", """
            package fixture;
            public class Base {
            }
            """, List.of());
        for (var name : List.of("fixture/Child.class", "fixture/Component.class")) {
            var target = runtime.resolve(name);
            Files.createDirectories(target.getParent());
            Files.copy(compileTime.resolve(name), target);
        }
        try (var loader = loader(jar(runtime, temporary.resolve("missing.jar")))) {
            assertThrows(NoSuchMethodError.class,
                () -> OptionalLinkageVerifier.verify(loader, Set.of("fixture.Component"), EXTERNAL, FOLLOWED));
        }
    }

    private static URLClassLoader loader(Path jar) throws IOException {
        return new URLClassLoader(new java.net.URL[] {jar.toUri().toURL()}, ClassLoader.getPlatformClassLoader());
    }

    private static void compile(Path output, String binaryName, String source, List<String> extraOptions)
        throws IOException {
        var compiler = ToolProvider.getSystemJavaCompiler();
        var diagnostics = new javax.tools.DiagnosticCollector<JavaFileObject>();
        try (var manager = compiler.getStandardFileManager(diagnostics, Locale.ROOT, null)) {
            var unit = new SimpleJavaFileObject(URI.create("string:///" + binaryName.replace('.', '/') + ".java"),
                JavaFileObject.Kind.SOURCE) {
                @Override
                public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                    return source;
                }
            };
            var options = new java.util.ArrayList<String>(List.of("--release", "17", "-d", output.toString()));
            options.addAll(extraOptions);
            assertTrue(compiler.getTask(null, manager, diagnostics, options, null, List.of(unit)).call(),
                diagnostics.getDiagnostics().toString());
        }
    }

    private static Path jar(Path root, Path output) throws IOException {
        try (var stream = new JarOutputStream(Files.newOutputStream(output)); var files = Files.walk(root)) {
            for (var file : files.filter(Files::isRegularFile).toList()) {
                stream.putNextEntry(new JarEntry(root.relativize(file).toString().replace('\\', '/')));
                stream.write(Files.readAllBytes(file));
                stream.closeEntry();
            }
        }
        return output;
    }
}
