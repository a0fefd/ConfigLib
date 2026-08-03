package com.a0fefd.ConfigLib.client;

import com.a0fefd.ConfigLib.Config;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;

public class ConfigCommand {
    public static void register(Config config) {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                register(dispatcher, config));
    }

    private static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, Config config) {
        dispatcher.register(ClientCommands.literal(config.MOD_ID)
                .executes(context -> {
                    Minecraft.getInstance().setScreen(new ConfigScreen(config, null));
                    return 1;
                }));
    }
}