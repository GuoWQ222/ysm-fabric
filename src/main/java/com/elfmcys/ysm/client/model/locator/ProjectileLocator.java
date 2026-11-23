// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.model.locator;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.api.model.ModelKind;
import com.elfmcys.ysm.api.model.event.RegisterModelLocatorEvent;
import com.elfmcys.ysm.geckolib3.geo.render.built.GeoLocatorType;
import net.minecraft.resources.Identifier;

public class ProjectileLocator extends GeoLocatorType {
    private static ProjectileLocator INSTANCE;

    @SuppressWarnings("removal")
    private ProjectileLocator() {
        super(Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "projectile"));
    }

    /** Initializes and freezes declarations on the bootstrap owner before resource workers can read them. */
    public static void init() {
        if (INSTANCE == null) {
            INSTANCE = new ProjectileLocator();
            YesSteveModel.postEvent(new RegisterModelLocatorEvent(ModelKind.PROJECTILE, INSTANCE::register));
            INSTANCE.freeze();
        }
    }

    public static ProjectileLocator get() {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        throw new IllegalStateException("ProjectileLocator has not been initialized");
    }
}
