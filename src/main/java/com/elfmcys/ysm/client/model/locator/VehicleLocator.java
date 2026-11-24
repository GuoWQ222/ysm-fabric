// Modified for the unofficial Minecraft 26.x port (Fabric, NeoForge, Forge) by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.client.model.locator;

import com.elfmcys.ysm.YesSteveModel;
import com.elfmcys.ysm.api.model.ModelKind;
import com.elfmcys.ysm.api.model.event.RegisterModelLocatorEvent;
import com.elfmcys.ysm.geckolib3.geo.render.built.GeoLocator;
import com.elfmcys.ysm.geckolib3.geo.render.built.GeoLocatorType;
import net.minecraft.resources.Identifier;

public class VehicleLocator extends GeoLocatorType {
    private static VehicleLocator INSTANCE;

    public final GeoLocator passenger = register("PassengerLocator");

    @SuppressWarnings("removal")
    private VehicleLocator() {
        super(Identifier.fromNamespaceAndPath(YesSteveModel.MOD_ID, "vehicle"));
    }

    /** Initializes and freezes declarations on the bootstrap owner before resource workers can read them. */
    public static void init() {
        if (INSTANCE == null) {
            INSTANCE = new VehicleLocator();
            YesSteveModel.postEvent(new RegisterModelLocatorEvent(ModelKind.VEHICLE, INSTANCE::register));
            INSTANCE.freeze();
        }
    }

    public static VehicleLocator get() {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        throw new IllegalStateException("VehicleLocator has not been initialized");
    }
}
