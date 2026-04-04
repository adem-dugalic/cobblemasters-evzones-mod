package com.cobbleverse.evzones;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SpawnZone {

    public String name;
    public String dimension;
    public double x;
    public double y;
    public double z;
    public double radius;
    public String pokemon;
    public int maxCount;
    public int spawnRateTicks;

    // Runtime only — not saved to JSON
    public transient int tickCounter = 0;
    public transient int pendingSpawns = 0;
    /** UUIDs of Pokemon this zone has spawned that are still alive in the world. */
    public transient Set<UUID> trackedPokemon = new HashSet<>();

    public SpawnZone() {}

    public SpawnZone(String name, String dimension,
                     double x, double y, double z,
                     double radius, String pokemon,
                     int maxCount, int spawnRateTicks) {
        this.name = name;
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.pokemon = pokemon;
        this.maxCount = maxCount;
        this.spawnRateTicks = spawnRateTicks;
    }

    public String toDisplayString() {
        return String.format(
            "§e%s §7| §bpokemon:§f%s §7| §bpos:§f%.0f %.0f %.0f §7| §bradius:§f%.0f §7| §bmax:§f%d §7| §brate:§f%d ticks §7| §bdim:§f%s",
            name, pokemon, x, y, z, radius, maxCount, spawnRateTicks, dimension
        );
    }
}
