// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.expression.host;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.api.annotation.YsmEntityComponent;
import java.lang.annotation.ElementType;
import java.util.*;

/**
 * Called by the client startup owner before it constructs any target factory.
 *
 * <p>
 * Requires: Forge discovery and the non-null host-availability snapshot are complete. This loader freezes the process
 * collector once; it is not a per-world reload or a repeatable service factory.
 *
 * <p>
 * Ensures: normal return exposes one frozen declaration host after every admitted class reaches its outcome. Rejected
 * classes contribute diagnostics, not placeholders. Admission does not certify provider thread safety or construct
 * installation-local components.
 *
 * <p>
 * Execution: synchronous metadata reads, checker execution, binding, and class initialization on the startup caller.
 * Discovery itself does not initialize implementation classes.
 *
 * <p>
 * Seam: implements [extension qualification and freeze](docs/architecture/integration/overview.md#扩展接纳与声明发布).
 */
public final class MolangExtensionLoader {
    private MolangExtensionLoader() {}

    /**
     * Freezes the process declaration collector and returns its non-null host.
     *
     * @throws IllegalStateException if collection has already been sealed; failure does not reopen collection
     */
    public static ExpressionHost createHost(HostAvailability availability) {
        var loader = net.fabricmc.loader.api.FabricLoader.getInstance();
        var admitted = com.elfmcys.ysm.api.internal.MolangDeclarationBootstrap.freeze(
            Objects.requireNonNull(availability, "availability"),
            discover(MolangExtensionLoader.class.getClassLoader()),
            new MolangExtensions.Qualification() {
                public boolean dependency(String id, String range) {
                    var mod = loader.getModContainer(id);
                    return mod.isPresent() && (range.isEmpty() || MolangExtensions
                        .versionMatches(mod.get().getMetadata().getVersion().getFriendlyString(), range));
                }

                public boolean checker(MolangExtensions.Source source,
                    com.elfmcys.ysm.api.molang.DeclarationSchema schema) {
                    if (schema.builtin())
                        return schema.dependencies().stream()
                            .allMatch(dependency -> java.util.Arrays.stream(OptionalCapability.values())
                                .filter(capability -> capability.modId().equals(dependency.modId()))
                                .allMatch(availability::supports));
                    if (schema.manual())
                        return true;
                    try {
                        var result = com.elfmcys.ysm.api.internal.CompatibilityQualification.check(schema.owner(),
                            source.loader());
                        result.issues().forEach(issue -> YesSteveModel.LOGGER.warn("Molang checker: {}", issue));
                        return result.compatible();
                    } catch (ReflectiveOperationException | RuntimeException | LinkageError failure) {
                        throw new IllegalArgumentException("cannot qualify generated checker", failure);
                    }
                }
            });
        admitted.diagnostics()
            .forEach(message -> YesSteveModel.LOGGER.error("Molang extension admission: {}", message));
        return admitted.host();
    }

    /**
     * Fabric port: Forge annotation scan data does not exist, so third-party {@code @YsmEntityComponent} classes are
     * not discovered. YSM's own builtin declarations are registered by the annotation processor, not by discovery.
     */
    public static List<MolangExtensions.Source> discover(ClassLoader loader) {
        return List.of();
    }
}
