package com.a0fefd.ConfigLib.client;

import com.a0fefd.ConfigLib.Config;
import com.a0fefd.ConfigLib.ConfigOption;
import com.a0fefd.ConfigLib.ConfigOptionType;
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
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ConfigScreen extends Screen {
    private static final int TAB_Y = 24;
    private static final int TAB_HEIGHT = 20;
    private static final int LIST_TOP = 50;
    private static final int ENTRY_HEIGHT = 22;
    private static final int HEADING_HEIGHT = 16;
    private static final int ENTRY_WIDTH = 240;
    private static final int CONTROL_WIDTH = 100;
    private static final int BOTTOM_MARGIN = 30;

    private final Config config;
    private final @Nullable Screen parent;
    private final List<String> categories = new ArrayList<>();
    private String selectedCategory;
    private int scrollOffset = 0;

    private final List<LabelEntry> visibleLabels = new ArrayList<>();
    private final List<HeadingEntry> visibleHeadings = new ArrayList<>();

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
        categories.sort(Comparator.comparingInt(category -> {
            Integer index = config.builder.getCategoryIndex(category);
            return index != null ? index : Integer.MAX_VALUE;
        }));
        if (selectedCategory == null || !categories.contains(selectedCategory)) {
            selectedCategory = categories.isEmpty() ? null : categories.getFirst();
        }
        rebuildConfigWidgets();
    }

    private void rebuildConfigWidgets() {
        clearWidgets();
        visibleLabels.clear();
        visibleHeadings.clear();

        int tabX = 8;
        for (String category : categories) {
            int tabWidth = Math.clamp(font.width(category) + 16, 50, 150);
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

        // A heading row is inserted whenever an option's sub-category differs from the
        // previous option's, grouping consecutive same-sub-category options under it.
        List<Row> rows = new ArrayList<>();
        String lastSubCategory = null;
        for (ConfigOption<?> option : config.builder.getOptionList()) {
            if (!option.category().equals(selectedCategory)) continue;
            String subCategory = option.subCategory();
            if (subCategory != null && !subCategory.equals(lastSubCategory)) {
                rows.add(new Row.Heading(subCategory));
            }
            lastSubCategory = subCategory;
            rows.add(new Row.OptionRow(option));
        }

        int totalHeight = 0;
        for (Row row : rows) totalHeight += rowHeight(row);
        int maxScroll = Math.max(0, totalHeight - (listBottom - LIST_TOP));
        scrollOffset = Math.clamp(scrollOffset, 0, maxScroll);

        int y = LIST_TOP - scrollOffset;
        for (Row row : rows) {
            int rowHeight = rowHeight(row);
            if (y + rowHeight < LIST_TOP || y > listBottom) {
                y += rowHeight;
                continue;
            }

            switch (row) {
                case Row.Heading heading -> visibleHeadings.add(new HeadingEntry(heading.text(), centerX, y + 6));
                case Row.OptionRow optionRow -> {
                    ConfigOption<?> option = optionRow.option();
                    visibleLabels.add(new LabelEntry(option.displayName(), labelX, y + 6));
                    addControl(option, controlX, y);
                }
            }
            y += rowHeight;
        }

        addRenderableWidget(Button.builder(Component.literal("Done"), btn -> onClose())
                .bounds(centerX - 50, height - 24, 100, 20).build());
    }

    private sealed interface Row {
        record Heading(String text) implements Row {
        }

        record OptionRow(ConfigOption<?> option) implements Row {
        }
    }

    // LIST renders one row per entry plus a trailing "add" row (see addListControl), so its
    // height is dynamic — every other row type is a fixed single line.
    private int rowHeight(Row row) {
        if (row instanceof Row.Heading) return HEADING_HEIGHT;
        ConfigOption<?> option = ((Row.OptionRow) row).option();
        if (option.optionType() != ConfigOptionType.LIST) return ENTRY_HEIGHT;
        return (listEntries(option).size() + 1) * ENTRY_HEIGHT;
    }

    private static List<?> listEntries(ConfigOption<?> option) {
        Object currentRaw = option.currentValue() != null ? option.currentValue() : option.defaultValue();
        return currentRaw instanceof List<?> list ? list : List.of();
    }

    // TUPLE and LIST need several widgets in one control block, so this adds directly rather
    // than returning a single AbstractWidget like the other option types.
    @SuppressWarnings("unchecked")
    private void addControl(ConfigOption<?> option, int x, int y) {
        switch (option.optionType()) {
            case BOOL -> addRenderableWidget(buildBoolControl((ConfigOption<Boolean>) option, x, y));
            case NUM -> addRenderableWidget(buildNumControl(option, x, y));
            case ENUM -> addRenderableWidget(buildEnumControl(option, x, y));
            case LIST -> addListControl(option, x, y);
            case STR -> addRenderableWidget(buildStrControl((ConfigOption<String>) option, x, y));
            case TUPLE -> addTupleControl(option, x, y);
        }
    }

    private AbstractWidget buildBoolControl(ConfigOption<Boolean> option, int x, int y) {
        boolean current = option.currentValue() != null ? option.currentValue() : Boolean.TRUE.equals(option.defaultValue());
        return CycleButton.onOffBuilder(current)
                .displayOnlyValue()
                .create(x, y, CONTROL_WIDTH, ENTRY_HEIGHT - 2, Component.literal(""),
                        (btn, value) -> config.builder.set(option.key(), value));
    }

    private AbstractWidget buildNumControl(ConfigOption<?> option, int x, int y) {
        Object currentRaw = option.currentValue() != null ? option.currentValue() : option.defaultValue();
        double current = currentRaw instanceof Number n ? n.doubleValue() : 0;

        if (option.min() != null && option.max() != null) {
            double min = option.min().doubleValue();
            double max = option.max().doubleValue();
            double sliderValue = max == min ? 0 : (current - min) / (max - min);
            return new AbstractSliderButton(x, y, CONTROL_WIDTH, ENTRY_HEIGHT - 2, Component.literal(formatNum(current)), sliderValue) {
                @Override
                protected void updateMessage() {
                    setMessage(Component.literal(formatNum(min + (max - min) * this.value)));
                }

                @Override
                protected void applyValue() {
                    config.builder.set(option.key(), min + (max - min) * this.value);
                }
            };
        }
        EditBox field = new EditBox(font, x, y, CONTROL_WIDTH, ENTRY_HEIGHT - 2, Component.literal(option.displayName()));
        field.setValue(formatNum(current));
        field.setResponder(text -> {
            try {
                config.builder.set(option.key(), Double.parseDouble(text.trim()));
            } catch (NumberFormatException ignored) {
                //
            }
        });
        return field;
    }

    // NUM is stored/parsed as double throughout (see Config.load(), which round-trips JSON
    // numbers as Double regardless of the declared Class<T>) so there's one numeric type to
    // reason about instead of Integer/Float diverging from what Gson hands back after a reload.
    private static String formatNum(double value) {
        return value == Math.rint(value) && !Double.isInfinite(value)
                ? String.valueOf((long) value)
                : String.format("%.2f", value);
    }

    private AbstractWidget buildStrControl(ConfigOption<String> option, int x, int y) {
        EditBox field = new EditBox(font, x, y, CONTROL_WIDTH, ENTRY_HEIGHT - 2, Component.literal(option.displayName()));
        field.setValue(option.currentValue() != null ? option.currentValue() : (option.defaultValue() != null ? option.defaultValue() : ""));
        field.setResponder(text -> config.builder.set(option.key(), text));
        return field;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private AbstractWidget buildEnumControl(ConfigOption<?> option, int x, int y) {
        Object[] constants = option.type().getEnumConstants();
        Object current = option.currentValue() != null ? option.currentValue() : option.defaultValue();
        if (current == null && constants != null && constants.length > 0) current = constants[0];
        CycleButton.Builder builder = CycleButton.builder(value -> Component.literal(value.toString()), current)
                .displayOnlyValue();
        if (constants != null) builder.withValues(constants);
        return builder.create(x, y, CONTROL_WIDTH, ENTRY_HEIGHT - 2, Component.literal(""),
                (btn, value) -> config.builder.set(option.key(), value));
    }

    // One row per entry (each with its own remove button) plus a trailing add row, instead
    // of TUPLE's fixed-arity single row — LIST's arity is unbounded, so entries stack
    // vertically rather than cramming into ever-narrower boxes on one line. Editing an
    // entry updates it in place with no rebuild; adding/removing changes row count, so
    // those rebuild the whole screen (see rowHeight, which this must stay consistent with).
    private static final int LIST_BUTTON_WIDTH = 16;
    private static final int LIST_GAP = 2;

    private void addListControl(ConfigOption<?> option, int x, int y) {
        List<Object> backing = new ArrayList<>(listEntries(option));
        int fieldWidth = CONTROL_WIDTH - LIST_BUTTON_WIDTH - LIST_GAP;

        for (int i = 0; i < backing.size(); i++) {
            int index = i;
            int rowY = y + i * ENTRY_HEIGHT;

            EditBox field = new EditBox(font, x, rowY, fieldWidth, ENTRY_HEIGHT - 2,
                    Component.literal(option.displayName() + " " + i));
            field.setMaxLength(256);
            field.setValue(formatTupleElement(backing.get(i)));
            field.setResponder(text -> {
                backing.set(index, clampElement(parseListElement(text.trim()), option));
                config.builder.set(option.key(), new ArrayList<>(backing));
            });
            addRenderableWidget(field);

            addRenderableWidget(Button.builder(Component.literal("x"), btn -> {
                        backing.remove(index);
                        config.builder.set(option.key(), new ArrayList<>(backing));
                        rebuildConfigWidgets();
                    }).bounds(x + fieldWidth + LIST_GAP, rowY, LIST_BUTTON_WIDTH, ENTRY_HEIGHT - 2)
                    .build());
        }

        int addY = y + backing.size() * ENTRY_HEIGHT;
        EditBox addField = new EditBox(font, x, addY, fieldWidth, ENTRY_HEIGHT - 2, Component.literal("Add entry"));
        addRenderableWidget(addField);
        addRenderableWidget(Button.builder(Component.literal("+"), btn -> {
                    String text = addField.getValue().trim();
                    if (text.isEmpty()) return;
                    backing.add(clampElement(parseListElement(text), option));
                    config.builder.set(option.key(), new ArrayList<>(backing));
                    rebuildConfigWidgets();
                }).bounds(x + fieldWidth + LIST_GAP, addY, LIST_BUTTON_WIDTH, ENTRY_HEIGHT - 2)
                .build());
    }

    // Fixed-arity sibling of LIST: one EditBox per entry instead of one comma-joined field.
    // Arity comes from whichever of currentValue/defaultValue is present — there's no
    // separate "how many entries" field on ConfigOption, so an empty default can't be
    // widened later; it just renders as a single box.
    private void addTupleControl(ConfigOption<?> option, int x, int y) {
        List<?> currentRaw = option.currentValue() instanceof List<?> l ? l
                : option.defaultValue() instanceof List<?> d ? d : List.of();
        int count = Math.max(1, currentRaw.size());

        List<Object> backing = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            backing.add(clampElement(i < currentRaw.size() ? currentRaw.get(i) : 0.0, option));
        }

        int gap = 2;
        int boxWidth = (CONTROL_WIDTH - gap * (count - 1)) / count;
        for (int i = 0; i < count; i++) {
            int index = i;
            int boxX = x + i * (boxWidth + gap);
            EditBox field = new EditBox(font, boxX, y, boxWidth, ENTRY_HEIGHT - 2,
                    Component.literal(option.displayName() + " " + i));
            field.setValue(formatTupleElement(backing.get(i)));
            field.setResponder(text -> {
                backing.set(index, clampElement(parseListElement(text.trim()), option));
                config.builder.set(option.key(), new ArrayList<>(backing));
            });
            addRenderableWidget(field);
        }
    }

    private static String formatTupleElement(Object value) {
        return value instanceof Number n ? formatNum(n.doubleValue()) : String.valueOf(value);
    }

    // List elements have no per-element type info (ConfigOption<T> only carries one Class<T> for
    // the whole list), and Gson round-trips JSON numbers as Double regardless of what was written,
    // so numeric entries are kept as Double (matching NUM) rather than guessing Integer vs Double.
    private static Object parseListElement(String text) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException ignored) {
            return text;
        }
    }

    // Applies to LIST/TUPLE only — option.min()/max() bound each entry here, unlike NUM
    // where they bound the option's single value. Non-numeric entries (STR-ish LIST use)
    // pass through untouched.
    private static Object clampElement(Object value, ConfigOption<?> option) {
        if (!(value instanceof Number n)) return value;
        double d = n.doubleValue();
        if (option.min() != null) d = Math.max(d, option.min().doubleValue());
        if (option.max() != null) d = Math.min(d, option.max().doubleValue());
        return d;
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
        graphics.centeredText(font, title, width / 2, 8, 0xFFFFFFFF);
        for (LabelEntry entry : visibleLabels) {
            graphics.text(font, entry.label, entry.x, entry.y, 0xFFFFFFFF);
        }
        for (HeadingEntry entry : visibleHeadings) {
            graphics.centeredText(font, entry.text(), entry.x(), entry.y(), 0xFFAAAAAA);
        }
    }

    @Override
    public void onClose() {
        config.save();
        minecraft.setScreen(parent);
    }

    private record LabelEntry(String label, int x, int y) {
    }

    private record HeadingEntry(String text, int x, int y) {
    }
}