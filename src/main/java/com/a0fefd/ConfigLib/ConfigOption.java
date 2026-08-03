package com.a0fefd.ConfigLib;

import org.jetbrains.annotations.Nullable;

public record ConfigOption<T>(
        String key,
        String category,
        @Nullable String subCategory,
        String displayName,
        @Nullable T defaultValue,
        @Nullable T currentValue,
        Class<T> type,
        ConfigOptionType optionType,
        @Nullable Number min,
        @Nullable Number max
) {
    public static final String DEFAULT_CATEGORY = "General";

    // key is the stable identifier used for storage/lookup; displayName is what the GUI
    // shows. Falling back to key here means every existing constructor below keeps working
    // unchanged (they just pass null) instead of needing a displayName-less duplicate of
    // each overload.
    public ConfigOption {
        if (displayName == null) displayName = key;
    }

    public ConfigOption(
            String key,
            String category,
            @Nullable T defaultValue,
            Class<T> type,
            ConfigOptionType optionType
    ) {
        this(key, category, null, null, defaultValue, defaultValue, type, optionType, null, null);
    }
    public ConfigOption(
            String key,
            String category,
            @Nullable String subCategory,
            @Nullable T defaultValue,
            Class<T> type,
            ConfigOptionType optionType
    ) {
        this(key, category, subCategory, null, defaultValue, defaultValue, type, optionType, null, null);
    }
    public ConfigOption(
            String key,
            String category,
            @Nullable String subCategory,
            @Nullable String displayName,
            @Nullable T defaultValue,
            Class<T> type,
            ConfigOptionType optionType
    ) {
        this(key, category, subCategory, displayName, defaultValue, defaultValue, type, optionType, null, null);
    }
    public ConfigOption(
            String key,
            @Nullable T defaultValue,
            Class<T> type,
            ConfigOptionType optionType
    ) {
        this(key, DEFAULT_CATEGORY, null, null, defaultValue, defaultValue, type, optionType, null, null);
    }
    public ConfigOption(
            String key,
            @Nullable T defaultValue,
            @Nullable T currentValue,
            Class<T> type,
            ConfigOptionType optionType
    ) {
        this(key, DEFAULT_CATEGORY, null, null, defaultValue, currentValue, type, optionType, null, null);
    }
    public ConfigOption(
            String key,
            String category,
            @Nullable T defaultValue,
            @Nullable T currentValue,
            Class<T> type,
            ConfigOptionType optionType
    ) {
        this(key, category, null, null, defaultValue, currentValue, type, optionType, null, null);
    }
    public ConfigOption(
            String key,
            String category,
            @Nullable String subCategory,
            @Nullable T defaultValue,
            @Nullable T currentValue,
            Class<T> type,
            ConfigOptionType optionType
    ) {
        this(key, category, subCategory, null, defaultValue, currentValue, type, optionType, null, null);
    }
    public ConfigOption(
            String key,
            String category,
            @Nullable T defaultValue,
            Class<T> type,
            ConfigOptionType optionType,
            @Nullable Number min,
            @Nullable Number max
    ) {
        this(key, category, null, null, defaultValue, defaultValue, type, optionType, min, max);
    }
    public ConfigOption(
            String key,
            @Nullable T defaultValue,
            Class<T> type,
            ConfigOptionType optionType,
            @Nullable Integer min,
            @Nullable Integer max
    ) {
        this(key, DEFAULT_CATEGORY, null, null, defaultValue, defaultValue, type, optionType, min, max);
    }
    public ConfigOption(
            String key,
            @Nullable T defaultValue,
            @Nullable T currentValue,
            Class<T> type,
            ConfigOptionType optionType,
            @Nullable Integer min,
            @Nullable Integer max
    ) {
        this(key, DEFAULT_CATEGORY, null, null, defaultValue, currentValue, type, optionType, min, max);
    }

    public ConfigOption<T> withCurrentValue(@Nullable T currentValue) {
        return new ConfigOption<>(key, category, subCategory, displayName, defaultValue, currentValue, type, optionType, min, max);
    }
}
