# All Drop Sounds

RuneLite plugin that plays a custom wav when an item drops on the ground or comes from a chest.

## Sounds

Wav files go in `%USERPROFILE%\.runelite\plugin-data\all-drop-sounds`. The plugin creates that folder on startup and names the path in chat after you log in. Open sounds folder in the settings opens it. Tick the box once; it clears after the folder opens.

**Master volume** is at the top of the settings and scales every sound. 0 is silent. Sounds also follow the in-game master and sound-effects sliders, and they stop when those sliders are muted.

An item file overrides every category sound. The file name is the item's exact name, and matching ignores letter case. `Twisted bow.wav` and `Twisted bow 2.wav` take turns. `skotos.wav` and `tiny tempor.wav` play for those pets. A file is a wildcard only when its name contains `%`, so `Clue scroll%.wav` matches every clue scroll and `Clue scroll.wav` matches only that exact name.

```
Twisted bow.wav
Twisted bow 2.wav
Clue scroll%.wav
Enhanced crystal weapon seed.wav
Dharok's helm.wav
```

When an item has no file of its own, the plugin picks the first match:

1. `highlighted.wav`, for names on the highlighted list in Ground Items or any other plugin that stores a highlighted item list
2. `pets.wav`, `jars.wav`, or `champions.wav` for those untradeable uniques, then `Unique.wav` when the category file is missing
3. The highest price tier the stack still meets

A stack is the unit price times the quantity. The price source is Grand Exchange, high alchemy, or the highest of the two (the default). If `highlighted.wav` or the unique category file is missing, the price tier sound is used. An exact item file still wins over its category, so `skotos.wav` plays instead of `pets.wav`. A volume of 0 stays silent. A rarer drop stops a cheaper sound that is already playing.

```
highlighted.wav
pets.wav
jars.wav
champions.wav
Unique.wav
Low.wav
Medium.wav
High.wav
Insane.wav
Insane+.wav
Insane++.wav
```

Default start prices are 20,000, 100,000, 1,000,000, 10,000,000, 50,000,000, and 250,000,000. Each tier has its own volume. A start price of 0 disables that tier. A stack below every enabled tier plays no tier sound.

One sound plays per item name when:

- the item appears on the ground as your drop or a group drop
- the item is received from a chest, including Barrows, the Gauntlet and Corrupted Gauntlet, Chambers of Xeric, Theatre of Blood, Tombs of Amascut, the Lunar Chest, the Fortis Colosseum chest, the Wilderness loot chest, and other chests that say you found treasure inside

Items you drop from your inventory stay silent. A drop is recognized from the Drop option and from the item leaving your inventory, including when the game does not mark it as your loot.

**Ignored items** is a comma-separated list of item names. A listed item never plays a sound, including when a wav file exists for it or the stack is valuable enough for a tier. Matching ignores letter case.

Ground sounds stay quiet until the scene has finished loading, so items already on the ground at login stay silent.

## Settings

- Master volume
- Specific drop volume
- Open sounds folder
- Ground drops
- Chest loot
- Only your drops
- Ignored items
- Highlighted volume
- Untradeable uniques
- Unique volume
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