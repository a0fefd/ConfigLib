package com.a0fefd.ConfigLib;

import org.jetbrains.annotations.Nullable;

public record ConfigOption<T>(
        String key,
        String category,
        @Nullable T defaultValue,
        @Nullable T currentValue,
        Class<T> type,
        ConfigOptionType optionType,
        @Nullable Integer min,
        @Nullable Integer max
) {
    public static final String DEFAULT_CATEGORY = "General";

    public ConfigOption(String key, @Nullable T defaultValue, @Nullable T currentValue, Class<T> type, ConfigOptionType optionType, @Nullable Integer min, @Nullable Integer max) {
        this(key, DEFAULT_CATEGORY, defaultValue, currentValue, type, optionType, min, max);
    }

    public ConfigOption<T> withCurrentValue(@Nullable T currentValue) {
        return new ConfigOption<>(key, category, defaultValue, currentValue, type, optionType, min, max);
    }
}
