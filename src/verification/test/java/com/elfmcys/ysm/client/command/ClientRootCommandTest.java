// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ClientRootCommandTest {
    @Test
    void registeredTreeKeepsLocalMolangAndAcceptsClearSelectors() throws Exception {
        var dispatcher = new com.mojang.brigadier.CommandDispatcher<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource>();
        ClientRootCommand.register(dispatcher);
        var root = dispatcher.getRoot().getChild("ysmclient");
        assertNotNull(root.getChild("molang"));
        var clear = root.getChild("roaming");
        assertNotNull(clear.getChild("clear").getChild("model"));
        var subtree = new com.mojang.brigadier.CommandDispatcher<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource>();
        subtree.getRoot().addChild(clear);
        // Fabric port: client commands use FabricClientCommandSource (an interface); a null-returning stand-in.
        var source = (net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource)java.lang.reflect.Proxy
            .newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] {net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource.class},
                (proxy, method, args) -> method.getReturnType() == boolean.class ? false
                    : method.getReturnType().isPrimitive() ? 0 : null);
        for (var selector : java.util.List.of("default", "id:missing/model@1.0.0", "\"local:folder/model one.mxc\"")) {
            var parsed = subtree.parse("roaming clear " + selector, source);
            assertFalse(parsed.getReader().canRead());
            assertNotNull(parsed.getContext().getCommand());
        }
    }

    @Test
    void mapsFrozenHostSymbolsToMolangSuggestions() {
        assertEquals("q.health", ClientRootCommand.sourceName("query:health"));
        assertEquals("math.random", ClientRootCommand.sourceName("math:random"));
        assertEquals("ctrl.idle", ClientRootCommand.sourceName("ctrl:idle"));
        assertEquals("o", ClientRootCommand.sourceName("global:o"));
    }
}
