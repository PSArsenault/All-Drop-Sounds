package com.alldropsounds;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup(AllDropSoundsConfig.GROUP)
public interface AllDropSoundsConfig extends Config
{
	String GROUP = "alldropsounds";

	@ConfigSection(
		name = "Value sounds",
		description = "Tier wavs used when an item does not have its own file.",
		position = 5
	)
	String valueSounds = "valueSounds";

	@ConfigItem(
		keyName = "soundsFolder",
		name = "Sounds folder",
		description = "Folder of wav files. An item file such as \"Twisted bow.wav\" overrides the tier files Low.wav, Medium.wav, High.wav, Insane.wav, Insane+.wav, and Insane++.wav. Leave blank to use .runelite/all-drop-sounds.",
		position = 0
	)
	default String soundsFolder()
	{
		return "";
	}

	@Range(min = 0, max = 100)
	@ConfigItem(
		keyName = "volume",
		name = "Volume",
		description = "Playback volume. 0 is silent.",
		position = 1
	)
	default int volume()
	{
		return 100;
	}

	@ConfigItem(
		keyName = "playGroundDrops",
		name = "Ground drops",
		description = "Play when one of your items appears on the ground.",
		position = 2
	)
	default boolean playGroundDrops()
	{
		return true;
	}

	@ConfigItem(
		keyName = "playChestLoot",
		name = "Chest loot",
		description = "Play when an item is received from a chest, including Barrows, the Gauntlet, and raid chests.",
		position = 3
	)
	default boolean playChestLoot()
	{
		return true;
	}

	@ConfigItem(
		keyName = "onlyYourDrops",
		name = "Only your drops",
		description = "Ground sounds play for your drops and group drops.",
		position = 4
	)
	default boolean onlyYourDrops()
	{
		return true;
	}

	@ConfigItem(
		keyName = "priceSource",
		name = "Price source",
		description = "Which price is compared with the tier start prices. Stacks use price times quantity.",
		position = 6,
		section = valueSounds
	)
	default PriceSource priceSource()
	{
		return PriceSource.HIGHEST;
	}

	@ConfigItem(
		keyName = "lowValuePrice",
		name = "Low price",
		description = "Start price for Low.wav. Items below every tier play no tier sound. 0 disables this tier.",
		position = 7,
		section = valueSounds
	)
	default int lowValuePrice()
	{
		return 20_000;
	}

	@ConfigItem(
		keyName = "mediumValuePrice",
		name = "Medium price",
		description = "Start price for Medium.wav. 0 disables this tier.",
		position = 8,
		section = valueSounds
	)
	default int mediumValuePrice()
	{
		return 100_000;
	}

	@ConfigItem(
		keyName = "highValuePrice",
		name = "High price",
		description = "Start price for High.wav. 0 disables this tier.",
		position = 9,
		section = valueSounds
	)
	default int highValuePrice()
	{
		return 1_000_000;
	}

	@ConfigItem(
		keyName = "insaneValuePrice",
		name = "Insane price",
		description = "Start price for Insane.wav. 0 disables this tier.",
		position = 10,
		section = valueSounds
	)
	default int insaneValuePrice()
	{
		return 10_000_000;
	}

	@ConfigItem(
		keyName = "insanePlusValuePrice",
		name = "Insane+ price",
		description = "Start price for Insane+.wav. 0 disables this tier.",
		position = 11,
		section = valueSounds
	)
	default int insanePlusValuePrice()
	{
		return 50_000_000;
	}

	@ConfigItem(
		keyName = "insanePlusPlusValuePrice",
		name = "Insane++ price",
		description = "Start price for Insane++.wav. 0 disables this tier.",
		position = 12,
		section = valueSounds
	)
	default int insanePlusPlusValuePrice()
	{
		return 250_000_000;
	}
}
