# All Drop Sounds

RuneLite plugin that plays a custom sound when a specific item drops on the ground or is received from a chest.

Tier sounds cover drops by Grand Exchange or high alchemy value. Put these files in the sounds folder:

```text
Low.wav
Medium.wav
High.wav
Insane.wav
Insane+.wav
Insane++.wav
```

The default start prices are 20,000, 100,000, 1,000,000, 10,000,000, 50,000,000, and 250,000,000. Each one is a setting, and 0 turns that tier off. A stack is priced as unit price times quantity, and the highest tier it still meets is the one that plays.

An item file overrides the tier sound. The file name is the item's exact name:

```text
Twisted bow.wav
Enhanced crystal weapon seed.wav
Dharok's helm.wav
```

Matching ignores letter case. A sound plays once per item name when:

- the item appears on the ground as your drop or a group drop (items you drop yourself are ignored)
- the item is received from a chest, including Barrows, the Gauntlet and Corrupted Gauntlet, Chambers of Xeric, Theatre of Blood, Tombs of Amascut, the Lunar Chest, the Fortis Colosseum chest, the Wilderness loot chest, and other chests that say you found treasure inside

The default folder is `%userprofile%\.runelite\all-drop-sounds`. The plugin creates it and names the path in chat when you log in. You can choose another folder in the plugin settings.

## Run it

This machine needs a JDK. From this folder:

```powershell
.\gradlew.bat run
```

That starts RuneLite in developer mode with the plugin loaded. You can also build a sideload jar:

```powershell
.\gradlew.bat shadowJar
```

Copy `build\libs\all-drop-sounds-1.0.0-all.jar` to `%userprofile%\.runelite\sideloaded-plugins` and start RuneLite with `--developer-mode`.
