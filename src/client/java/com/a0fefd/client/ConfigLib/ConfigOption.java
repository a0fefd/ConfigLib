package com.a0fefd.client.ConfigLib;

import org.jetbrains.annotations.Nullable;

public record ConfigOption<T>(
        String key,
        @Nullable T defaultValue,
        @Nullable T currentValue,
        Class<T> type,
        ConfigOptionType optionType,
        @Nullable Integer min,
        @Nullable Integer max
) {
    public ConfigOption<T> withCurrentValue(@Nullable T currentValue) {
        return new ConfigOption<>(key, defaultValue, currentValue, type, optionType, min, max);
    }
}
