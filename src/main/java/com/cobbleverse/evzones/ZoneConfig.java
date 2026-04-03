package com.cobbleverse.evzones;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ZoneConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("cobblemasters-evzones.json");

    private static ConfigData data = new ConfigData();

    public static ConfigData get() {
        return data;
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                data = GSON.fromJson(json, ConfigData.class);
                if (data == null) data = new ConfigData();
                if (data.zones == null) data.zones = new ArrayList<>();
                EvZonesMod.LOGGER.info("Loaded {} EV zones.", data.zones.size());
            } catch (IOException e) {
                EvZonesMod.LOGGER.warn("Failed to read EV zones config, using empty list.", e);
                data = new ConfigData();
            }
        } else {
            data = new ConfigData();
            save();
            EvZonesMod.LOGGER.info("Created empty EV zones config at {}", CONFIG_PATH);
        }
    }

    public static void save() {
        try {
            Files.writeString(CONFIG_PATH, GSON.toJson(data));
        } catch (IOException e) {
            EvZonesMod.LOGGER.warn("Failed to save EV zones config.", e);
        }
    }

    public static SpawnZone findByName(String name) {
        return data.zones.stream()
                .filter(z -> z.name.equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public static boolean addZone(SpawnZone zone) {
        if (findByName(zone.name) != null) return false;
        data.zones.add(zone);
        save();
        return true;
    }

    public static boolean removeZone(String name) {
        boolean removed = data.zones.removeIf(z -> z.name.equalsIgnoreCase(name));
        if (removed) save();
        return removed;
    }

    public static class ConfigData {
        public List<SpawnZone> zones = new ArrayList<>();
    }
}
