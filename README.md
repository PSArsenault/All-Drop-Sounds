# All Drop Sounds

RuneLite plugin that plays a custom wav when an item drops on the ground or comes from a chest.

## Sounds

Wav files go in `%USERPROFILE%\.runelite\plugin-data\all-drop-sounds`. The plugin creates that folder on startup and names the path in chat after you log in.

**Master volume** is the first control in Sound volume and scales every sound. 0 is silent. Sounds also follow the in-game master and sound-effects sliders, and they stop when those sliders are muted.

An item file overrides every category sound. The file name is the item's exact name, and matching ignores letter case. `Twisted bow.wav` and `Twisted bow 2.wav` take turns. `skotos.wav` and `tiny tempor.wav` play for those pets. A file is a wildcard only when its name contains `%`, so `Clue scroll%.wav` matches every clue scroll and `Clue scroll.wav` matches only that exact name. A drop whose name ends with ` (uncharged)` uses the wav for the name without that suffix, so `Obliterator.wav` plays for Obliterator (uncharged). A wav that includes ` (uncharged)` still wins when that file exists.

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

A stack is the unit price times the quantity. The price source is Grand Exchange, high alchemy, or the highest of the two (the default). If `highlighted.wav` or the unique category file is missing, the price tier sound is used. An exact item file still wins over its category, so `skotos.wav` plays instead of `pets.wav`. A volume of 0 stays silent. Sounds play through RuneLite's audio player. A new drop does not cut off a sound that is already playing.

```
highlighted.wav
pets.wav
jars.wav
champions.wav
Unique.wav
purple.wav
Low.wav
Medium.wav
High.wav
Insane.wav
Insane+.wav
Insane++.wav
```

Default start prices are 20,000, 100,000, 1,000,000, 10,000,000, 50,000,000, and 250,000,000. Each tier has its own volume in Sound volume. A start price of 0 disables that tier. A stack below every enabled tier plays no tier sound.

One sound plays per item name when:

- the item appears on the ground as your drop or a group drop
- the item is received from a chest, including Barrows, the Gauntlet and Corrupted Gauntlet, Chambers of Xeric, Theatre of Blood, Tombs of Amascut, The Fractured Archive, the Lunar Chest, the Fortis Colosseum chest, the Wilderness loot chest, and other chests that say you found treasure inside

On The Fractured Archive unique chest, the item sound plays when that item appears on the chest, after the glowing light. The glow itself is silent. Opening the chest to take the item does not play that sound again.

**Items you drop** plays a sound for items you place on the ground from your inventory. It is off by default. A drop is recognized from the Drop option and from the item leaving your inventory, including when the game does not mark it as your loot.

**Purple notification** plays `purple.wav` when a purple chest appears in the reward room, before its loot is revealed. It covers Chambers of Xeric, Theatre of Blood, Tombs of Amascut, and The Fractured Archive. That chest's item sound still plays for the loot. Master volume scales `purple.wav`. The notification stays quiet when `purple.wav` is missing.

**Ignored items** is a comma-separated list of item names. A listed item never plays a sound, including when a wav file exists for it or the stack is valuable enough for a tier. Matching ignores letter case.

Ground sounds stay quiet until the scene has finished loading, so items already on the ground at login stay silent.

## Settings

- Ground drops
- Chest loot
- Only your drops
- Items you drop
- Ignored items
- Untradeable uniques
- Purple notification

Sound volume:

- Master volume
- Specific drop volume
- Highlighted volume
- Unique volume
- Low, Medium, High, Insane, Insane+, and Insane++ volume

Value sounds:

- Price source
- Low, Medium, High, Insane, Insane+, and Insane++ start price
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