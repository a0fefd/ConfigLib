package com.a0fefd.ConfigLib;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ConfigBuilder {
    private List<ConfigOption<?>> optionList;

    public ConfigBuilder(@Nullable List<ConfigOption<?>> optionList) {
        this.optionList = optionList == null ? new ArrayList<>() : new ArrayList<>(optionList);
    }

    public void add(ConfigOption<?> option) {
        optionList.add(option);
    }
    public void addList(List<ConfigOption<?>> _optionList) { optionList.addAll(_optionList); }
    public void set(int i, ConfigOption<?> option) {
        try {
            optionList.set(i, option);
        } catch (Exception e) {
            //
        }
    }

    public void set(String key, Object value) {
        for (int i = 0; i < optionList.size(); i++) {
            ConfigOption<?> option = optionList.get(i);
            if (!Objects.equals(option.key(), key)) continue;
            set(i, withValueUnchecked(option, value));
            break;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> ConfigOption<T> withValueUnchecked(ConfigOption<T> option, Object value) {
        return option.withCurrentValue((T) value);
    }

    public void remove(int i) {
        try {
            optionList.remove(i);
        } catch (Exception e) {
            //
        }
    }
    public void remove(ConfigOption<?> option) {
        try {
            optionList.remove(option);
        } catch (Exception e) {
            //
        }
    }

    public List<ConfigOption<?>> getOptionList() {
        return optionList;
    }
}
