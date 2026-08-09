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
        @Nullable Number max,
        @Nullable Class<?> elementType,
        @Nullable Runnable action
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
        this(key, category, null, null, defaultValue, defaultValue, type, optionType, null, null, null, null);
    }
    public ConfigOption(
            String key,
            String category,
            @Nullable String subCategory,
            @Nullable T defaultValue,
            Class<T> type,
            ConfigOptionType optionType
    ) {
        this(key, category, subCategory, null, defaultValue, defaultValue, type, optionType, null, null, null, null);
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
        this(key, category, subCategory, displayName, defaultValue, defaultValue, type, optionType, null, null, null, null);
    }
    public ConfigOption(
            String key,
            @Nullable T defaultValue,
            Class<T> type,
            ConfigOptionType optionType
    ) {
        this(key, DEFAULT_CATEGORY, null, null, defaultValue, defaultValue, type, optionType, null, null, null, null);
    }
    public ConfigOption(
            String key,
            @Nullable T defaultValue,
            @Nullable T currentValue,
            Class<T> type,
            ConfigOptionType optionType
    ) {
        this(key, DEFAULT_CATEGORY, null, null, defaultValue, currentValue, type, optionType, null, null, null, null);
    }
    public ConfigOption(
            String key,
            String category,
            @Nullable T defaultValue,
            @Nullable T currentValue,
            Class<T> type,
            ConfigOptionType optionType
    ) {
        this(key, category, null, null, defaultValue, currentValue, type, optionType, null, null, null, null);
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
        this(key, category, subCategory, null, defaultValue, currentValue, type, optionType, null, null, null, null);
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
        this(key, category, null, null, defaultValue, defaultValue, type, optionType, min, max, null, null);
    }
    // min/max here bound each NUMERIC ENTRY for LIST/TUPLE (e.g. clamping colour components
    // to 0-255), not the option as a whole the way they do for NUM.
    public ConfigOption(
            String key,
            String category,
            @Nullable String subCategory,
            @Nullable String displayName,
            @Nullable T defaultValue,
            Class<T> type,
            ConfigOptionType optionType,
            @Nullable Number min,
            @Nullable Number max
    ) {
        this(key, category, subCategory, displayName, defaultValue, defaultValue, type, optionType, min, max, null, null);
    }
    public ConfigOption(
            String key,
            @Nullable T defaultValue,
            Class<T> type,
            ConfigOptionType optionType,
            @Nullable Integer min,
            @Nullable Integer max
    ) {
        this(key, DEFAULT_CATEGORY, null, null, defaultValue, defaultValue, type, optionType, min, max, null, null);
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
        this(key, DEFAULT_CATEGORY, null, null, defaultValue, currentValue, type, optionType, min, max, null, null);
    }

    // elementType constrains LIST/TUPLE element parsing (see ConfigScreen.parseListElement) —
    // e.g. String.class forces every typed entry to stay text instead of being guessed as a
    // number when it looks numeric. Null (every other constructor above) keeps the existing
    // best-effort guess.
    public ConfigOption(
            String key,
            String category,
            @Nullable String subCategory,
            @Nullable String displayName,
            @Nullable T defaultValue,
            Class<T> type,
            ConfigOptionType optionType,
            @Nullable Class<?> elementType
    ) {
        this(key, category, subCategory, displayName, defaultValue, defaultValue, type, optionType, null, null, elementType, null);
    }
    public ConfigOption(
            String key,
            String category,
            @Nullable String subCategory,
            @Nullable String displayName,
            @Nullable T defaultValue,
            Class<T> type,
            ConfigOptionType optionType,
            @Nullable Class<?> elementType,
            @Nullable Number min,
            @Nullable Number max
    ) {
        this(key, category, subCategory, displayName, defaultValue, defaultValue, type, optionType, min, max, elementType, null);
    }

    // BUTTON has no value at all — key/category/subCategory/displayName place it in the
    // screen like any other row, but currentValue/defaultValue/type/min/max/elementType are
    // all meaningless for it, so this is the one constructor that doesn't thread them through.
    // Config.save()/load() skip BUTTON options entirely rather than persisting a null.
    @SuppressWarnings("unchecked")
    public ConfigOption(
            String key,
            String category,
            @Nullable String subCategory,
            String displayName,
            Runnable action
    ) {
        this(key, category, subCategory, displayName, null, null, (Class<T>) (Class<?>) Void.class, ConfigOptionType.BUTTON, null, null, null, action);
    }

    public ConfigOption<T> withCurrentValue(@Nullable T currentValue) {
        return new ConfigOption<>(key, category, subCategory, displayName, defaultValue, currentValue, type, optionType, min, max, elementType, action);
    }
}
