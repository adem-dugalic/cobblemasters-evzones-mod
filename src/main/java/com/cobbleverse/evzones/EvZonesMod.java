package com.cobbleverse.evzones;

import com.cobbleverse.evzones.commands.EvZoneCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EvZonesMod implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("cobblemasters-evzones");

    @Override
    public void onInitialize() {
        LOGGER.info("CobbleMasters EV Zones loading...");

        ZoneConfig.load();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            EvZoneCommand.register(dispatcher);
            LOGGER.info("Registered /evzone command.");
        });

        ServerTickEvents.END_SERVER_TICK.register(ZoneSpawnManager::tick);

        LOGGER.info("CobbleMasters EV Zones ready. {} zones active.", ZoneConfig.get().zones.size());
    }
}
