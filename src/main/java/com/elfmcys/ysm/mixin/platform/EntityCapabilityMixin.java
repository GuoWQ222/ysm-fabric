/*
 * Copyright (c) 2026 GuoWQ222. All rights reserved.
 *
 * This file was written for the unofficial Fabric 26.2 port (ysm-fabric). Unlike the upstream Yes Steve Model code in
 * this repository, it is NOT licensed under the Apache License 2.0. See LICENSE-PORT.md.
 */
package com.elfmcys.ysm.mixin.platform;

import com.elfmcys.ysm.platform.capability.AttachCapabilitiesEvent;
import com.elfmcys.ysm.platform.capability.Capability;
import com.elfmcys.ysm.platform.capability.CapabilityDispatcher;
import com.elfmcys.ysm.platform.capability.ICapabilityHolder;
import com.elfmcys.ysm.platform.capability.LazyOptional;
import com.elfmcys.ysm.platform.event.MinecraftForge;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
//#if MC >= 1.21.6
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//#endif
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/** Fabric port: Forge-style capabilities on every entity, attached lazily and saved under "ForgeCaps". */
@Mixin(Entity.class)
public abstract class EntityCapabilityMixin implements ICapabilityHolder {
    @Unique
    private static final String YSM$CAPS_KEY = "ForgeCaps";
    @Unique
    private CapabilityDispatcher ysm$caps;
    @Unique
    private boolean ysm$attaching;
    @Unique
    private boolean ysm$valid = true;

    @Unique
    private CapabilityDispatcher ysm$dispatcher() {
        if (ysm$caps == null && !ysm$attaching) {
            ysm$attaching = true;
            try {
                var event = new AttachCapabilitiesEvent<>(Entity.class, (Entity)(Object)this);
                MinecraftForge.EVENT_BUS.post(event);
                ysm$caps = new CapabilityDispatcher(event.getCapabilities());
            } finally {
                ysm$attaching = false;
            }
        }
        return ysm$caps;
    }

    @Override
    @NotNull
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (!ysm$valid)
            return LazyOptional.empty();
        var caps = ysm$dispatcher();
        return caps == null ? LazyOptional.empty() : caps.getCapability(cap, side);
    }

    @Override
    public void invalidateYsmCaps() {
        ysm$valid = false;
    }

    @Override
    public void reviveYsmCaps() {
        ysm$valid = true;
    }

    //#if MC >= 1.21.6
    @Inject(method = "saveWithoutId", at = @At("TAIL"))
    private void ysm$saveCaps(ValueOutput output, CallbackInfo ci) {
        if (ysm$caps == null)
            return;
        var tag = ysm$caps.serialize();
        if (!tag.isEmpty())
            output.store(YSM$CAPS_KEY, CompoundTag.CODEC, tag);
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void ysm$loadCaps(ValueInput input, CallbackInfo ci) {
        input.read(YSM$CAPS_KEY, CompoundTag.CODEC).ifPresent(tag -> {
            var caps = ysm$dispatcher();
            if (caps != null)
                caps.deserialize(tag);
        });
    }
    //#else
    //$$ // Minecraft before 1.21.6 saves entities into a CompoundTag
    //$$ @Inject(method = "saveWithoutId", at = @At("TAIL"))
    //$$ private void ysm$saveCaps(net.minecraft.nbt.CompoundTag output,
    //$$     org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<net.minecraft.nbt.CompoundTag> cir) {
    //$$     if (ysm$caps == null)
    //$$         return;
    //$$     var tag = ysm$caps.serialize();
    //$$     if (!tag.isEmpty())
    //$$         output.put(YSM$CAPS_KEY, tag);
    //$$ }
    //$$ 
    //$$ @Inject(method = "load", at = @At("TAIL"))
    //$$ private void ysm$loadCaps(net.minecraft.nbt.CompoundTag input, CallbackInfo ci) {
    //#if MC >= 1.21.5
    //$$     input.getCompound(YSM$CAPS_KEY).ifPresent(tag -> {
    //$$         var caps = ysm$dispatcher();
    //$$         if (caps != null)
    //$$             caps.deserialize(tag);
    //$$     });
    //#else
    //$$     if (input.contains(YSM$CAPS_KEY, net.minecraft.nbt.Tag.TAG_COMPOUND)) {
    //$$         var caps = ysm$dispatcher();
    //$$         if (caps != null)
    //$$             caps.deserialize(input.getCompound(YSM$CAPS_KEY));
    //$$     }
    //#endif
    //$$ }
    //#endif
}
