package com.a0fefd.ConfigLib.client;

import com.a0fefd.ConfigLib.Config;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;

import java.util.List;

public class ConfigCommand {
    public static void register(Config config) {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                register(dispatcher, config, config.MOD_ID));
    }
    public static void registerWithAlternatives(Config config, List<String> alternatives) {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            register(dispatcher, config, config.MOD_ID);
            for (String alt : alternatives) {
                register(dispatcher, config, alt);
            }
        });
    }

    private static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, Config config, String command) {
        dispatcher.register(ClientCommands.literal(command)
                .executes(context -> {
                    // ChatScreen.sendMessage() closes itself with setScreen(null) right after this
                    // callback returns, which would immediately undo an open() called here.
                    Minecraft.getInstance().execute(() ->
                            Minecraft.getInstance().setScreen(new ConfigScreen(config, null)));
                    return 1;
                }));
    }
}