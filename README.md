# All Drop Sounds

RuneLite plugin that plays a custom wav when an item drops on the ground or comes from a chest.

## Sounds

Wav files go in the sounds folder. The default is `%USERPROFILE%\.runelite\all-drop-sounds`. The plugin creates that folder on startup and names the path in chat after you log in. Another folder can be set in the plugin settings.

A highlighted item with no file of its own plays `highlighted.wav` at the highlighted volume. If that file is missing, the price tier sound is used. A highlighted volume of 0 stays silent. Every other item with no file of its own plays the highest price tier the stack still meets. A stack is the unit price times the quantity. The price source is Grand Exchange, high alchemy, or the highest of the two (the default).

```
Low.wav
Medium.wav
High.wav
Insane.wav
Insane+.wav
Insane++.wav
```

Default start prices are 20,000, 100,000, 1,000,000, 10,000,000, 50,000,000, and 250,000,000. Each tier has its own volume. A start price of 0 disables that tier. A stack below every enabled tier plays no tier sound.

An item file overrides the tier sound. The file name is the item's exact name, and matching ignores letter case:

```
Twisted bow.wav
Enhanced crystal weapon seed.wav
Dharok's helm.wav
```

The main **Item volume** setting applies only to those item files. Tier files use the volume next to that tier. A volume of 0 is silent.

One sound plays per item name when:

- the item appears on the ground as your drop or a group drop
- the item is received from a chest, including Barrows, the Gauntlet and Corrupted Gauntlet, Chambers of Xeric, Theatre of Blood, Tombs of Amascut, the Lunar Chest, the Fortis Colosseum chest, the Wilderness loot chest, and other chests that say you found treasure inside

**Items you drop** controls items you place on the ground from your inventory. It is off by default, so those drops stay silent until you turn it on.

**Ignored items** is a comma-separated list of item names. A listed item never plays a sound, including when a wav file exists for it or the stack is valuable enough for a tier. Matching ignores letter case.

**Highlighted items** is a comma-separated list of item names that play `highlighted.wav`. An item's own wav file still wins, and ignored items stay silent. Matching ignores letter case.

Ground sounds stay quiet until the scene has finished loading, so items already on the ground at login stay silent.

## Settings

- Sounds folder
- Item volume
- Ground drops
- Chest loot
- Only your drops
- Items you drop
- Ignored items
- Highlighted items
- Highlighted volume
- Price source
- Low, Medium, High, Insane, Insane+, and Insane++ start price and volume

## Build and run

Java 11 or newer, and Gradle 8. This repo has no Gradle wrapper.

```powershell
gradle test
gradle run
```

`gradle run` starts RuneLite in developer mode with this plugin loaded. Sign in with a Jagex account through the Jagex launcher once so credentials are saved. The launcher itself cannot load this plugin, because it does not pass `--developer-mode`.

A sideload jar:

```powershell
gradle shadowJar
```

Copy `build\libs\all-drop-sounds-1.0.0-all.jar` to `%USERPROFILE%\.runelite\sideloaded-plugins` and start RuneLite with `--developer-mode`.

## Contributing

Pull requests target `master`. Topic branches use `feat/`, `fix/`, or `docs/`. See [CONTRIBUTING.md](CONTRIBUTING.md).
