// Modified for the unofficial Fabric 26.2 port by GuoWQ222, 2026 (see PORTING.md).
package com.elfmcys.ysm.capability;

import com.elfmcys.ysm.model.domain.PublishedModelId;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.nbt.CompoundTag;

public class VehicleModelInfoCapability {
    private PublishedModelId modelHash;
    private boolean initialized = false;
    private Object2FloatOpenHashMap<String> molangVarsServerBound = new Object2FloatOpenHashMap<>();

    public void update(PublishedModelId modelHash, Object2FloatOpenHashMap<String> molangVarsServerBound) {
        this.modelHash = modelHash;
        this.initialized = true;
        this.molangVarsServerBound = molangVarsServerBound;
    }

    public void copyFrom(VehicleModelInfoCapability source) {
        this.modelHash = source.modelHash;
        this.initialized = source.initialized;
        this.molangVarsServerBound = source.molangVarsServerBound;
    }

    public PublishedModelId getOwnerModelId() {
        return modelHash;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public Object2FloatOpenHashMap<String> getMolangVarsServerBound() {
        return molangVarsServerBound;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("owner_model_id", modelHash == null ? "" : modelHash.toString());
        tag.putBoolean("initialized", initialized);

        CompoundTag varsTag = new CompoundTag();
        molangVarsServerBound.object2FloatEntrySet().fastForEach(varsEntry -> {
            varsTag.putFloat(varsEntry.getKey(), varsEntry.getFloatValue());
        });
        tag.put("molang_vars_server_bound", varsTag);

        return tag;
    }

    public void deserializeNBT(CompoundTag nbt) {
        var stored = nbt.getStringOr("owner_model_id", "");
        try {
            this.modelHash = stored.isEmpty() ? null : PublishedModelId.parse(stored);
        } catch (IllegalArgumentException ignored) {
            this.modelHash = null;
        }
        this.initialized = nbt.getBooleanOr("initialized", false);

        this.molangVarsServerBound.clear();
        var varsTag = nbt.getCompoundOrEmpty("molang_vars_server_bound");
        for (var name : varsTag.keySet()) {
            var value = varsTag.getFloatOr(name, 0f);
            this.molangVarsServerBound.put(name, value);
        }
    }
}
