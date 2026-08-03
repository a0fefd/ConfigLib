package com.a0fefd.ConfigLib.client;

import com.a0fefd.ConfigLib.Config;
import com.a0fefd.ConfigLib.ConfigOption;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ConfigScreen extends Screen {
    private static final int TAB_Y = 24;
    private static final int TAB_HEIGHT = 20;
    private static final int LIST_TOP = 50;
    private static final int ENTRY_HEIGHT = 22;
    private static final int ENTRY_WIDTH = 240;
    private static final int CONTROL_WIDTH = 100;
    private static final int BOTTOM_MARGIN = 30;

    private final Config config;
    private final @Nullable Screen parent;
    private final List<String> categories = new ArrayList<>();
    private String selectedCategory;
    private int scrollOffset = 0;

    private final List<LabelEntry> visibleLabels = new ArrayList<>();

    public ConfigScreen(Config config, @Nullable Screen parent) {
        super(Component.literal(config.MOD_ID));
        this.config = config;
        this.parent = parent;
    }

    @Override
    protected void init() {
        categories.clear();
        Set<String> seen = new LinkedHashSet<>();
        for (ConfigOption<?> option : config.builder.getOptionList()) {
            seen.add(option.category());
        }
        categories.addAll(seen);
        if (selectedCategory == null || !categories.contains(selectedCategory)) {
            selectedCategory = categories.isEmpty() ? null : categories.get(0);
        }
        rebuildConfigWidgets();
    }

    private void rebuildConfigWidgets() {
        clearWidgets();
        visibleLabels.clear();

        int tabX = 8;
        for (String category : categories) {
            int tabWidth = Math.min(150, Math.max(50, font.width(category) + 16));
            boolean selected = category.equals(selectedCategory);
            Button tabButton = Button.builder(Component.literal(category), btn -> {
                selectedCategory = category;
                scrollOffset = 0;
                rebuildConfigWidgets();
            }).bounds(tabX, TAB_Y, tabWidth, TAB_HEIGHT).build();
            tabButton.active = !selected;
            addRenderableWidget(tabButton);
            tabX += tabWidth + 4;
        }

        int listBottom = height - BOTTOM_MARGIN;
        int centerX = width / 2;
        int labelX = centerX - ENTRY_WIDTH / 2;
        int controlX = centerX + ENTRY_WIDTH / 2 - CONTROL_WIDTH;

        List<ConfigOption<?>> options = new ArrayList<>();
        for (ConfigOption<?> option : config.builder.getOptionList()) {
            if (option.category().equals(selectedCategory)) options.add(option);
        }

        int maxScroll = Math.max(0, options.size() * ENTRY_HEIGHT - (listBottom - LIST_TOP));
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

        for (int i = 0; i < options.size(); i++) {
            int y = LIST_TOP + i * ENTRY_HEIGHT - scrollOffset;
            if (y + ENTRY_HEIGHT < LIST_TOP || y > listBottom) continue;

            ConfigOption<?> option = options.get(i);
            visibleLabels.add(new LabelEntry(option.key(), labelX, y + 6));
            addRenderableWidget(buildControl(option, controlX, y));
        }

        addRenderableWidget(Button.builder(Component.literal("Done"), btn -> onClose())
                .bounds(centerX - 50, height - 24, 100, 20).build());
    }

    @SuppressWarnings("unchecked")
    private AbstractWidget buildControl(ConfigOption<?> option, int x, int y) {
        return switch (option.optionType()) {
            case BOOL -> buildBoolControl((ConfigOption<Boolean>) option, x, y);
            case INT -> buildIntControl((ConfigOption<Integer>) option, x, y);
            case FLOAT -> buildFloatControl((ConfigOption<Float>) option, x, y);
            case ENUM -> buildEnumControl(option, x, y);
            case LIST -> buildListControl((ConfigOption<List<String>>) option, x, y);
            case STR -> buildStrControl((ConfigOption<String>) option, x, y);
        };
    }

    private AbstractWidget buildBoolControl(ConfigOption<Boolean> option, int x, int y) {
        boolean current = option.currentValue() != null ? option.currentValue() : Boolean.TRUE.equals(option.defaultValue());
        return CycleButton.onOffBuilder(current)
                .create(x, y, CONTROL_WIDTH, ENTRY_HEIGHT - 2, Component.literal(""),
                        (btn, value) -> config.builder.set(option.key(), value));
    }

    private AbstractWidget buildIntControl(ConfigOption<Integer> option, int x, int y) {
        int current = option.currentValue() != null ? option.currentValue() : (option.defaultValue() != null ? option.defaultValue() : 0);
        if (option.min() != null && option.max() != null) {
            int min = option.min();
            int max = option.max();
            double sliderValue = max == min ? 0 : (double) (current - min) / (max - min);
            return new AbstractSliderButton(x, y, CONTROL_WIDTH, ENTRY_HEIGHT - 2, Component.literal(String.valueOf(current)), sliderValue) {
                @Override
                protected void updateMessage() {
                    setMessage(Component.literal(String.valueOf(min + Math.round((max - min) * this.value))));
                }

                @Override
                protected void applyValue() {
                    config.builder.set(option.key(), min + (int) Math.round((max - min) * this.value));
                }
            };
        }
        EditBox field = new EditBox(font, x, y, CONTROL_WIDTH, ENTRY_HEIGHT - 2, Component.literal(option.key()));
        field.setValue(String.valueOf(current));
        field.setResponder(text -> {
            try {
                config.builder.set(option.key(), Integer.parseInt(text.trim()));
            } catch (NumberFormatException ignored) {
                //
            }
        });
        return field;
    }

    private AbstractWidget buildFloatControl(ConfigOption<Float> option, int x, int y) {
        float current = option.currentValue() != null ? option.currentValue() : (option.defaultValue() != null ? option.defaultValue() : 0f);
        if (option.min() != null && option.max() != null) {
            float min = option.min();
            float max = option.max();
            double sliderValue = max == min ? 0 : (current - min) / (max - min);
            return new AbstractSliderButton(x, y, CONTROL_WIDTH, ENTRY_HEIGHT - 2, Component.literal(String.format("%.2f", current)), sliderValue) {
                @Override
                protected void updateMessage() {
                    setMessage(Component.literal(String.format("%.2f", min + (max - min) * this.value)));
                }

                @Override
                protected void applyValue() {
                    config.builder.set(option.key(), (float) (min + (max - min) * this.value));
                }
            };
        }
        EditBox field = new EditBox(font, x, y, CONTROL_WIDTH, ENTRY_HEIGHT - 2, Component.literal(option.key()));
        field.setValue(String.valueOf(current));
        field.setResponder(text -> {
            try {
                config.builder.set(option.key(), Float.parseFloat(text.trim()));
            } catch (NumberFormatException ignored) {
                //
            }
        });
        return field;
    }

    private AbstractWidget buildStrControl(ConfigOption<String> option, int x, int y) {
        EditBox field = new EditBox(font, x, y, CONTROL_WIDTH, ENTRY_HEIGHT - 2, Component.literal(option.key()));
        field.setValue(option.currentValue() != null ? option.currentValue() : (option.defaultValue() != null ? option.defaultValue() : ""));
        field.setResponder(text -> config.builder.set(option.key(), text));
        return field;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private AbstractWidget buildEnumControl(ConfigOption<?> option, int x, int y) {
        Object[] constants = option.type().getEnumConstants();
        Object current = option.currentValue() != null ? option.currentValue() : option.defaultValue();
        if (current == null && constants != null && constants.length > 0) current = constants[0];
        CycleButton.Builder builder = CycleButton.builder(value -> Component.literal(value.toString()), current);
        if (constants != null) builder.withValues(constants);
        return builder.create(x, y, CONTROL_WIDTH, ENTRY_HEIGHT - 2, Component.literal(""),
                (btn, value) -> config.builder.set(option.key(), value));
    }

    private AbstractWidget buildListControl(ConfigOption<List<String>> option, int x, int y) {
        List<String> current = option.currentValue() != null ? option.currentValue() : option.defaultValue();
        EditBox field = new EditBox(font, x, y, CONTROL_WIDTH, ENTRY_HEIGHT - 2, Component.literal(option.key()));
        field.setMaxLength(2048);
        field.setValue(current != null ? String.join(", ", current) : "");
        field.setResponder(text -> {
            List<String> values = new ArrayList<>();
            for (String part : text.split(",")) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) values.add(trimmed);
            }
            config.builder.set(option.key(), values);
        });
        return field;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseY >= LIST_TOP) {
            scrollOffset -= (int) (verticalAmount * ENTRY_HEIGHT / 2);
            rebuildConfigWidgets();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(font, title, width / 2, 8, 0xFFFFFF);
        for (LabelEntry entry : visibleLabels) {
            graphics.text(font, entry.label, entry.x, entry.y, 0xFFFFFF);
        }
    }

    @Override
    public void onClose() {
        config.save();
        minecraft.setScreen(parent);
    }

    private record LabelEntry(String label, int x, int y) {
    }
}