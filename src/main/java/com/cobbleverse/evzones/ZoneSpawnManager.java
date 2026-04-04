package com.cobbleverse.evzones;

import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.Iterator;
import java.util.UUID;

public final class ZoneSpawnManager {

    private static boolean debugMode = false;

    private ZoneSpawnManager() {}

    public static void setDebugMode(boolean enabled) {
        debugMode = enabled;
    }

    public static boolean isDebugMode() {
        return debugMode;
    }

    public static void tick(MinecraftServer server) {
        if (server.getPlayerManager().getPlayerList().isEmpty()) {
            return;
        }

        for (SpawnZone zone : ZoneConfig.get().zones) {
            // Ensure transient field is initialized (e.g. after config reload)
            if (zone.trackedPokemon == null) {
                zone.trackedPokemon = new java.util.HashSet<>();
            }

            zone.tickCounter++;
            if (zone.tickCounter < zone.spawnRateTicks) {
                continue;
            }
            zone.tickCounter = 0;

            boolean playerNearby = false;
            for (net.minecraft.server.network.ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                double dx = player.getX() - zone.x;
                double dz = player.getZ() - zone.z;
                double activationRange = Math.max(zone.radius * 2.0, 64.0);
                if (dx * dx + dz * dz <= activationRange * activationRange) {
                    playerNearby = true;
                    break;
                }
            }
            if (!playerNearby) {
                continue;
            }

            trySpawn(server, zone);
        }
    }

    private static void trySpawn(MinecraftServer server, SpawnZone zone) {
        RegistryKey<net.minecraft.world.World> worldKey =
                RegistryKey.of(RegistryKeys.WORLD, Identifier.of(zone.dimension));
        ServerWorld world = server.getWorld(worldKey);
        if (world == null) {
            EvZonesMod.LOGGER.warn("EV Zone '{}': dimension '{}' not found.", zone.name, zone.dimension);
            return;
        }

        // Prune dead/removed entities from the tracked set
        pruneTrackedPokemon(world, zone);

        int alive = zone.trackedPokemon.size();

        if (debugMode) {
            EvZonesMod.LOGGER.info("[EVZones DEBUG] Zone '{}': {} tracked alive / {} max",
                    zone.name, alive, zone.maxCount);
        }

        if (alive >= zone.maxCount) {
            return;
        }

        // Spawn at exact center of the zone
        double spawnX = zone.x;
        double spawnZ = zone.z;

        try {
            var spawnSource = server.getCommandSource()
                    .withPosition(new Vec3d(spawnX, zone.y, spawnZ))
                    .withWorld(world)
                    .withLevel(4)
                    .withSilent();
            server.getCommandManager().getDispatcher().execute(
                    "pokespawn " + zone.pokemon, spawnSource);

            // After pokespawn, find newly added entities near the spawn point
            var nearbyAfter = world.getEntitiesByClass(
                    Entity.class,
                    new net.minecraft.util.math.Box(
                            spawnX - 2, zone.y - 5, spawnZ - 2,
                            spawnX + 2, zone.y + 5, spawnZ + 2),
                    e -> !zone.trackedPokemon.contains(e.getUuid()));

            // Track any new Pokemon-like entities (use string check to be safe with classloading)
            boolean tracked = false;
            for (Entity entity : nearbyAfter) {
                String className = entity.getClass().getName().toLowerCase();
                if (className.contains("pokemon")) {
                    zone.trackedPokemon.add(entity.getUuid());
                    if (debugMode) {
                        EvZonesMod.LOGGER.info("[EVZones DEBUG] Zone '{}': tracked new Pokemon UUID {} ({})",
                                zone.name, entity.getUuid(), entity.getClass().getSimpleName());
                    }
                    tracked = true;
                }
            }

            if (!tracked && debugMode) {
                EvZonesMod.LOGGER.warn("[EVZones DEBUG] Zone '{}': pokespawn ran but no new Pokemon entity found near spawn point!", zone.name);
            }

        } catch (Exception e) {
            EvZonesMod.LOGGER.warn("EV Zone '{}': failed to spawn '{}': {}",
                    zone.name, zone.pokemon, e.getMessage());
        }
    }

    /**
     * Removes UUIDs from the tracked set if the entity no longer exists in the world
     * or is dead/removed.
     */
    private static void pruneTrackedPokemon(ServerWorld world, SpawnZone zone) {
        Iterator<UUID> it = zone.trackedPokemon.iterator();
        while (it.hasNext()) {
            UUID uuid = it.next();
            Entity entity = world.getEntity(uuid);
            if (entity == null || !entity.isAlive() || entity.isRemoved()) {
                if (debugMode) {
                    EvZonesMod.LOGGER.info("[EVZones DEBUG] Zone '{}': pruned UUID {} (entity {})",
                            zone.name, uuid,
                            entity == null ? "not found" : "dead/removed");
                }
                it.remove();
            }
        }
    }

    /**
     * Check if a given entity is inside any EV zone.
     * Returns the zone if found, null otherwise.
     */
    public static SpawnZone getZoneContaining(Entity entity) {
        for (SpawnZone zone : ZoneConfig.get().zones) {
            String entityDim = entity.getWorld().getRegistryKey().getValue().toString();
            if (!entityDim.equals(zone.dimension)) continue;

            double dx = entity.getX() - zone.x;
            double dz = entity.getZ() - zone.z;
            if (dx * dx + dz * dz <= zone.radius * zone.radius) {
                return zone;
            }
        }
        return null;
    }

    /**
     * Returns the number of tracked (alive) Pokemon for a zone.
     * Call pruneTrackedPokemon first for an accurate count.
     */
    public static int getTrackedCount(ServerWorld world, SpawnZone zone) {
        if (zone.trackedPokemon == null) {
            zone.trackedPokemon = new java.util.HashSet<>();
        }
        pruneTrackedPokemon(world, zone);
        return zone.trackedPokemon.size();
    }
}
