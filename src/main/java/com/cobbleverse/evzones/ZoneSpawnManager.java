package com.cobbleverse.evzones;

import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Random;

public final class ZoneSpawnManager {

    private static final Random RANDOM = new Random();

    private ZoneSpawnManager() {}

    public static void tick(MinecraftServer server) {
        for (SpawnZone zone : ZoneConfig.get().zones) {
            zone.tickCounter++;
            if (zone.tickCounter >= zone.spawnRateTicks) {
                zone.tickCounter = 0;
                trySpawn(server, zone);
            }
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

        int current = countPokemonInZone(world, zone);
        if (current >= zone.maxCount) return;

        // Random position within radius (80% to avoid fence edges)
        double angle = RANDOM.nextDouble() * 2 * Math.PI;
        double dist = RANDOM.nextDouble() * zone.radius * 0.8;
        double spawnX = zone.x + Math.cos(angle) * dist;
        double spawnZ = zone.z + Math.sin(angle) * dist;

        try {
            var spawnSource = server.getCommandSource()
                    .withPosition(new Vec3d(spawnX, zone.y, spawnZ))
                    .withWorld(world)
                    .withLevel(4)
                    .withSilent();
            server.getCommandManager().getDispatcher().execute(
                    "pokespawn " + zone.pokemon, spawnSource);
        } catch (Exception e) {
            EvZonesMod.LOGGER.warn("EV Zone '{}': failed to spawn '{}': {}",
                    zone.name, zone.pokemon, e.getMessage());
        }
    }

    private static int countPokemonInZone(ServerWorld world, SpawnZone zone) {
        // Use a tall box so Pokémon at any Y level within the zone are counted
        Box box = new Box(
                zone.x - zone.radius, zone.y - 32, zone.z - zone.radius,
                zone.x + zone.radius, zone.y + 32, zone.z + zone.radius);

        List<Entity> entities = world.getEntitiesByClass(Entity.class, box, entity -> {
            Identifier typeId = net.minecraft.registry.Registries.ENTITY_TYPE.getId(entity.getType());
            boolean isPokemon = typeId != null
                    && "cobblemon".equals(typeId.getNamespace())
                    && "pokemon".equals(typeId.getPath());
            // XZ-only cylinder check — ignores Y so Pokémon on different levels still count
            double dx = entity.getX() - zone.x;
            double dz = entity.getZ() - zone.z;
            boolean inRadius = (dx * dx + dz * dz) <= zone.radius * zone.radius;
            return isPokemon && inRadius;
        });

        return entities.size();
    }
}
