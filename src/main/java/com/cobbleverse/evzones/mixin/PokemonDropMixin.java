package com.cobbleverse.evzones.mixin;

import com.cobbleverse.evzones.EvZonesMod;
import com.cobbleverse.evzones.SpawnZone;
import com.cobbleverse.evzones.ZoneSpawnManager;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Prevents Pokemon inside EV zones from dropping items when killed.
 * Players still get EVs from the battle — just no loot drops.
 *
 * Targets Entity.dropStack so the method resolution is clean (dropStack is
 * declared in Entity, not in PokemonEntity). The instanceof check ensures
 * we only cancel drops for Cobblemon Pokemon that are inside an EV zone.
 */
@Mixin(Entity.class)
public abstract class PokemonDropMixin {

    @Inject(method = "dropStack(Lnet/minecraft/item/ItemStack;F)Lnet/minecraft/entity/ItemEntity;",
            at = @At("HEAD"),
            cancellable = true)
    private void evzones$cancelDropsInZone(ItemStack stack, float yOffset, CallbackInfoReturnable<ItemEntity> cir) {
        Entity self = (Entity) (Object) this;

        if (!(self instanceof PokemonEntity)) return;

        SpawnZone zone = ZoneSpawnManager.getZoneContaining(self);
        if (zone != null) {
            if (ZoneSpawnManager.isDebugMode()) {
                EvZonesMod.LOGGER.info("[EVZones DEBUG] Cancelled drop of {} from Pokemon in zone '{}'",
                        stack.getItem(), zone.name);
            }
            cir.setReturnValue(null);
        }
    }
}
