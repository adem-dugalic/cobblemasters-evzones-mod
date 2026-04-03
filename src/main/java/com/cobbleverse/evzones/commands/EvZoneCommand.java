package com.cobbleverse.evzones.commands;

import com.cobbleverse.evzones.SpawnZone;
import com.cobbleverse.evzones.ZoneConfig;
import com.cobbleverse.evzones.ZoneSpawnManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public final class EvZoneCommand {

    private EvZoneCommand() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("evzone")
                .requires(src -> src.hasPermissionLevel(2))

                // /evzone create <name> <radius> <maxcount> <rateticks> <pokemon...>
                .then(CommandManager.literal("create")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                                .then(CommandManager.argument("radius", DoubleArgumentType.doubleArg(1, 200))
                                        .then(CommandManager.argument("maxcount", IntegerArgumentType.integer(1, 100))
                                                .then(CommandManager.argument("rateticks", IntegerArgumentType.integer(20, 72000))
                                                        .then(CommandManager.argument("pokemon", StringArgumentType.greedyString())
                                                                .executes(ctx -> create(
                                                                        ctx.getSource(),
                                                                        StringArgumentType.getString(ctx, "name"),
                                                                        DoubleArgumentType.getDouble(ctx, "radius"),
                                                                        IntegerArgumentType.getInteger(ctx, "maxcount"),
                                                                        IntegerArgumentType.getInteger(ctx, "rateticks"),
                                                                        StringArgumentType.getString(ctx, "pokemon")))))))))

                // /evzone delete <name>
                .then(CommandManager.literal("delete")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                                .executes(ctx -> delete(
                                        ctx.getSource(),
                                        StringArgumentType.getString(ctx, "name")))))

                // /evzone list
                .then(CommandManager.literal("list")
                        .executes(ctx -> list(ctx.getSource())))

                // /evzone info <name>
                .then(CommandManager.literal("info")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                                .executes(ctx -> info(
                                        ctx.getSource(),
                                        StringArgumentType.getString(ctx, "name")))))

                // /evzone setpokemon <name> <pokemon...>
                .then(CommandManager.literal("setpokemon")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                                .then(CommandManager.argument("pokemon", StringArgumentType.greedyString())
                                        .executes(ctx -> setPokemon(
                                                ctx.getSource(),
                                                StringArgumentType.getString(ctx, "name"),
                                                StringArgumentType.getString(ctx, "pokemon"))))))

                // /evzone setmax <name> <count>
                .then(CommandManager.literal("setmax")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                                .then(CommandManager.argument("count", IntegerArgumentType.integer(1, 100))
                                        .executes(ctx -> setMax(
                                                ctx.getSource(),
                                                StringArgumentType.getString(ctx, "name"),
                                                IntegerArgumentType.getInteger(ctx, "count"))))))

                // /evzone setrate <name> <ticks>
                .then(CommandManager.literal("setrate")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                                .then(CommandManager.argument("ticks", IntegerArgumentType.integer(20, 72000))
                                        .executes(ctx -> setRate(
                                                ctx.getSource(),
                                                StringArgumentType.getString(ctx, "name"),
                                                IntegerArgumentType.getInteger(ctx, "ticks"))))))

                // /evzone tp <name>
                .then(CommandManager.literal("tp")
                        .then(CommandManager.argument("name", StringArgumentType.word())
                                .executes(ctx -> tp(
                                        ctx.getSource(),
                                        StringArgumentType.getString(ctx, "name")))))

                // /evzone reload
                .then(CommandManager.literal("reload")
                        .executes(ctx -> reload(ctx.getSource())))
        );
    }

    // -------------------------------------------------------------------------

    private static int create(ServerCommandSource source, String name, double radius,
                               int maxCount, int rateTicks, String pokemon)
            throws CommandSyntaxException {

        if (ZoneConfig.findByName(name) != null) {
            source.sendError(Text.literal("Zone '" + name + "' already exists. Use /evzone delete first.")
                    .formatted(Formatting.RED));
            return 0;
        }

        ServerPlayerEntity player = source.getPlayerOrThrow();
        String dimension = player.getWorld().getRegistryKey().getValue().toString();
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();

        SpawnZone zone = new SpawnZone(name, dimension, x, y, z, radius, pokemon, maxCount, rateTicks);
        ZoneConfig.addZone(zone);

        source.sendFeedback(() -> Text.literal(
                "§aCreated EV zone §e" + name + "§a at §f" + (int)x + " " + (int)y + " " + (int)z
                + " §a| pokemon: §f" + pokemon
                + " §a| radius: §f" + (int)radius
                + " §a| max: §f" + maxCount
                + " §a| rate: §f" + rateTicks + " ticks"), true);
        return 1;
    }

    private static int delete(ServerCommandSource source, String name) {
        if (!ZoneConfig.removeZone(name)) {
            source.sendError(Text.literal("Zone '" + name + "' not found.").formatted(Formatting.RED));
            return 0;
        }
        source.sendFeedback(() -> Text.literal("§aDeleted EV zone §e" + name), true);
        return 1;
    }

    private static int list(ServerCommandSource source) {
        List<SpawnZone> zones = ZoneConfig.get().zones;
        if (zones.isEmpty()) {
            source.sendFeedback(() -> Text.literal("§7No EV zones defined. Use /evzone create to add one."), false);
            return 1;
        }
        source.sendFeedback(() -> Text.literal("§6§l=== EV Training Zones (" + zones.size() + ") ==="), false);
        for (SpawnZone z : zones) {
            source.sendFeedback(() -> Text.literal(z.toDisplayString()), false);
        }
        return 1;
    }

    private static int info(ServerCommandSource source, String name) {
        SpawnZone zone = ZoneConfig.findByName(name);
        if (zone == null) {
            source.sendError(Text.literal("Zone '" + name + "' not found.").formatted(Formatting.RED));
            return 0;
        }
        source.sendFeedback(() -> Text.literal("§6§l=== Zone: " + zone.name + " ==="), false);
        source.sendFeedback(() -> Text.literal("§bPokemon: §f" + zone.pokemon), false);
        source.sendFeedback(() -> Text.literal("§bPosition: §f" + (int)zone.x + " " + (int)zone.y + " " + (int)zone.z), false);
        source.sendFeedback(() -> Text.literal("§bRadius: §f" + (int)zone.radius + " blocks"), false);
        source.sendFeedback(() -> Text.literal("§bMax count: §f" + zone.maxCount), false);
        source.sendFeedback(() -> Text.literal("§bSpawn rate: §f" + zone.spawnRateTicks + " ticks (§e"
                + String.format("%.1f", zone.spawnRateTicks / 20.0) + "s§f)"), false);
        source.sendFeedback(() -> Text.literal("§bDimension: §f" + zone.dimension), false);
        return 1;
    }

    private static int setPokemon(ServerCommandSource source, String name, String pokemon) {
        SpawnZone zone = ZoneConfig.findByName(name);
        if (zone == null) {
            source.sendError(Text.literal("Zone '" + name + "' not found.").formatted(Formatting.RED));
            return 0;
        }
        zone.pokemon = pokemon;
        ZoneConfig.save();
        source.sendFeedback(() -> Text.literal("§aZone §e" + name + "§a pokemon set to §f" + pokemon), true);
        return 1;
    }

    private static int setMax(ServerCommandSource source, String name, int count) {
        SpawnZone zone = ZoneConfig.findByName(name);
        if (zone == null) {
            source.sendError(Text.literal("Zone '" + name + "' not found.").formatted(Formatting.RED));
            return 0;
        }
        zone.maxCount = count;
        ZoneConfig.save();
        source.sendFeedback(() -> Text.literal("§aZone §e" + name + "§a max count set to §f" + count), true);
        return 1;
    }

    private static int setRate(ServerCommandSource source, String name, int ticks) {
        SpawnZone zone = ZoneConfig.findByName(name);
        if (zone == null) {
            source.sendError(Text.literal("Zone '" + name + "' not found.").formatted(Formatting.RED));
            return 0;
        }
        zone.spawnRateTicks = ticks;
        zone.tickCounter = 0;
        ZoneConfig.save();
        source.sendFeedback(() -> Text.literal("§aZone §e" + name + "§a rate set to §f" + ticks
                + " ticks (§e" + String.format("%.1f", ticks / 20.0) + "s§f)"), true);
        return 1;
    }

    private static int tp(ServerCommandSource source, String name) throws CommandSyntaxException {
        SpawnZone zone = ZoneConfig.findByName(name);
        if (zone == null) {
            source.sendError(Text.literal("Zone '" + name + "' not found.").formatted(Formatting.RED));
            return 0;
        }
        ServerPlayerEntity player = source.getPlayerOrThrow();

        net.minecraft.registry.RegistryKey<net.minecraft.world.World> worldKey =
                net.minecraft.registry.RegistryKey.of(
                        net.minecraft.registry.RegistryKeys.WORLD,
                        net.minecraft.util.Identifier.of(zone.dimension));
        net.minecraft.server.world.ServerWorld world = source.getServer().getWorld(worldKey);
        if (world == null) {
            source.sendError(Text.literal("Dimension '" + zone.dimension + "' not found.").formatted(Formatting.RED));
            return 0;
        }

        player.teleport(world, zone.x, zone.y, zone.z, player.getYaw(), player.getPitch());
        source.sendFeedback(() -> Text.literal("§aTeleported to EV zone §e" + name), false);
        return 1;
    }

    private static int reload(ServerCommandSource source) {
        ZoneConfig.load();
        source.sendFeedback(() -> Text.literal("§aEV zones reloaded. §f" + ZoneConfig.get().zones.size() + " zones active."), true);
        return 1;
    }
}
