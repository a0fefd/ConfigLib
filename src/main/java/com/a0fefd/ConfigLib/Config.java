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
            if (option.optionType() == ConfigOptionType.BUTTON) continue;
            data.put(option.key(), option.currentValue());
        }
        Path file = CONFIG_DIR.resolve(MOD_ID + ".json");
        try {
            Files.writeString(file, gson.toJson(data));
        } catch (IOException | RuntimeException e) {
            // A broken file (partial write, bad permissions, whatever left it in a state
            // that keeps failing) must not linger forever; drop it and try once more clean.
            try {
                Files.deleteIfExists(file);
                Files.writeString(file, gson.toJson(data));
            } catch (IOException | RuntimeException ignored) {
                //
            }
        }
    }

    public void load() {
        Path file = CONFIG_DIR.resolve(MOD_ID + ".json");
        if (!Files.exists(file)) return;
        try {
            applyFrom(file);
        } catch (IOException | RuntimeException e) {
            // Malformed JSON — bad syntax, wrong shape after an option's type changed,
            // whatever — must not crash the game at startup. Drop the file and fall back
            // to whatever defaults ConfigManager already set; save() writes a clean file
            // again next time an option changes.
            try {
                Files.deleteIfExists(file);
            } catch (IOException ignored) {
                //
            }
        }
    }

    private void applyFrom(Path file) throws IOException {
        JsonElement json = JsonParser.parseString(Files.readString(file));
        JsonObject object = json.getAsJsonObject();
        for (ConfigOption<?> option : builder.getOptionList()) {
            if (option.optionType() == ConfigOptionType.BUTTON) continue;
            if (object.has(option.key())) {
                Object value = gson.fromJson(object.get(option.key()), option.type());
                builder.set(option.key(), value);
            }
        }
    }
}
