## ConfigLib

A lightweight config library for Fabric mods. Describe your options as plain Java objects and get a working in-game config screen, a /<modid> command to open it, and JSON persistence, all without hand-rolling a GUI.

### Why

Most mods end up writing the same boilerplate: a data class for settings, a screen to edit them, and load/save logic to keep a JSON file in sync. ConfigLib handles all three in one small dependency, so you can focus on the options themselves instead of the plumbing around them.

### Features

- In-game config screen: tabbed by category, with sub-category headings to group related options within a tab.
- Five option types: BOOL, NUM (int and float unified), STR, ENUM (any Java enum), and LIST (comma-separated, mixed types).
- Auto-registered command: /<modid> opens the screen, no extra setup needed.
- JSON persistence: options round-trip to <config dir>/<mod id>.json. A corrupted or outdated file falls back to defaults instead of crashing the game.

### How to install
Add these to each section of file `build.gradle`
```gradle
repositories {
	mavenCentral()
	maven { url = "https://raw.githubusercontent.com/a0fefd/ConfigLib/mvn-repo/" }
}
dependencies {
	implementation "com.a0fefd:configlib:<version>"
}
```
Replace `<version>` with your target version.

### Usage
```java
Config config = new Config("mymod", null);

config.builder.add(new ConfigOption<>(
        "example_toggle",
        "General",
        true,
        Boolean.class,
        ConfigOptionType.BOOL
));

config.load();
ConfigCommand.register(config);
```

That is enough to get a working /mymod command opening a config screen with one toggle in it. Add more ConfigOptions for more settings, and use the category and subCategory fields to organize a larger config into tabs and sections.

### Requirements

- Minecraft 26.1.2
- Fabric Loader 0.19.3 or newer
- Fabric API
- Java 25 or newer

