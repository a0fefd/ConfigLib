package com.a0fefd.ConfigLib;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class Config {
    public final String MOD_ID;
    public ConfigBuilder builder;

    public Config(String MOD_ID, @Nullable ConfigBuilder builder) {
        this.MOD_ID = MOD_ID;
        if (builder == null) this.builder = new ConfigBuilder(null);
        else this.builder = builder;
    }

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();

    public void save() {
        Map<String, Object> data = new LinkedHashMap<>();
        for (ConfigOption<?> option : builder.getOptionList()) {
            data.put(option.key(), option.currentValue());
        }
        try {
            Path file = CONFIG_DIR.resolve(MOD_ID + ".json");
            Files.writeString(file, gson.toJson(data));
        } catch (IOException e) {
            //
        }
    }

    public void load() {
        Path file = CONFIG_DIR.resolve(MOD_ID + ".json");
        if (!Files.exists(file)) return;
        try {
            JsonElement json = JsonParser.parseString(Files.readString(file));
            JsonObject object = json.getAsJsonObject();
            for (ConfigOption<?> option : builder.getOptionList()) {
                if (object.has(option.key())) {
                    Object value = gson.fromJson(object.get(option.key()), option.type());
                    builder.set(option.key(), value);
                }
            }
        } catch (IOException e) {
            //
        }
    }
}
