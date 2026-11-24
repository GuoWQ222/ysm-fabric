/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.gradle;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

/** Writes a {@link Preprocessor}-processed copy of source directories for one Minecraft version and loader. */
@CacheableTask
public abstract class PreprocessTask extends DefaultTask {
    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract ConfigurableFileCollection getSources();

    @Input
    public abstract Property<String> getMinecraftVersion();

    @Input
    public abstract Property<String> getLoader();

    /** Rename tables (versions/<mc>.remap, then versions/loader-<loader>.remap), applied in order to every Java file
     *  after the directives. */
    @InputFiles
    @PathSensitive(PathSensitivity.NONE)
    public abstract ConfigurableFileCollection getRemaps();

    @OutputDirectory
    public abstract DirectoryProperty getOutput();

    @TaskAction
    public void run() throws IOException {
        var processor = new Preprocessor(getMinecraftVersion().get(), getLoader().get());
        var remap = new java.util.ArrayList<String[]>();
        for (var table : getRemaps().getFiles())
            remap.addAll(Preprocessor.parseRemap(Files.readString(table.toPath(), StandardCharsets.UTF_8)));
        var out = getOutput().get().getAsFile().toPath();
        deleteTree(out);
        Files.createDirectories(out);
        for (var root : getSources().getFiles()) {
            var base = root.toPath();
            if (!Files.isDirectory(base))
                continue;
            try (Stream<Path> files = Files.walk(base)) {
                files.filter(Files::isRegularFile).forEach(file -> {
                    var relative = base.relativize(file);
                    var target = out.resolve(relative.toString());
                    try {
                        Files.createDirectories(target.getParent());
                        if (file.toString().endsWith(".java")) {
                            var text = Files.readString(file, StandardCharsets.UTF_8);
                            if (Preprocessor.hasDirectives(text))
                                text = processor.process(text, relative.toString());
                            if (!remap.isEmpty())
                                text = Preprocessor.remap(text, remap);
                            Files.writeString(target, text, StandardCharsets.UTF_8);
                        } else {
                            Files.copy(file, target);
                        }
                    } catch (IOException failure) {
                        throw new UncheckedIOException(failure);
                    } catch (IllegalArgumentException failure) {
                        throw new GradleException("preprocess: " + failure.getMessage(), failure);
                    }
                });
            }
        }
    }

    private static void deleteTree(Path root) throws IOException {
        if (!Files.exists(root))
            return;
        try (Stream<Path> files = Files.walk(root)) {
            for (var path : files.sorted(java.util.Comparator.reverseOrder()).toList())
                Files.delete(path);
        }
    }
}
