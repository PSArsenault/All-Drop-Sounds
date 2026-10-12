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
		description = "Tier wavs used when an item does not have its own file, is not highlighted, and is not an untradeable unique.",
		position = 10
	)
	String valueSounds = "valueSounds";

	@Range(min = 0, max = 100)
	@ConfigItem(
		keyName = "masterVolume",
		name = "Master volume",
		description = "Scales every drop sound. 0 is silent.",
		position = 0
	)
	default int masterVolume()
	{
		return 100;
	}

	@Range(min = 0, max = 100)
	@ConfigItem(
		keyName = "volume",
		name = "Item volume",
		description = "Volume for wav files named after a specific item. 0 is silent.",
		position = 1
	)
	default int itemVolume()
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
		keyName = "playSelfDrops",
		name = "Items you drop",
		description = "Play when you drop an item from your inventory onto the ground.",
		position = 5
	)
	default boolean playSelfDrops()
	{
		return false;
	}

	@ConfigItem(
		keyName = "ignoredItems",
		name = "Ignored items",
		description = "Comma-separated item names that never play a sound, even when a wav file exists or the stack meets a price tier.",
		position = 6
	)
	default String ignoredItems()
	{
		return "";
	}

	@Range(min = 0, max = 100)
	@ConfigItem(
		keyName = "highlightedVolume",
		name = "Highlighted volume",
		description = "Volume for highlighted.wav. Names come from the Ground Items highlighted list and any other plugin that stores one. 0 is silent.",
		position = 7
	)
	default int highlightedVolume()
	{
		return 100;
	}

	@ConfigItem(
		keyName = "playUniques",
		name = "Untradeable uniques",
		description = "Play Unique.wav for pets and other untradeable uniques that have no price.",
		position = 8
	)
	default boolean playUniques()
	{
		return true;
	}

	@Range(min = 0, max = 100)
	@ConfigItem(
		keyName = "uniqueVolume",
		name = "Unique volume",
		description = "Volume for Unique.wav. 0 is silent.",
		position = 9
	)
	default int uniqueVolume()
	{
		return 100;
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

	@Range(min = 0, max = 100)
	@ConfigItem(
		keyName = "lowValueVolume",
		name = "Low volume",
		description = "Volume for Low.wav. 0 is silent.",
		position = 8,
		section = valueSounds
	)
	default int lowValueVolume()
	{
		return 100;
	}

	@ConfigItem(
		keyName = "mediumValuePrice",
		name = "Medium price",
		description = "Start price for Medium.wav. 0 disables this tier.",
		position = 9,
		section = valueSounds
	)
	default int mediumValuePrice()
	{
		return 100_000;
	}

	@Range(min = 0, max = 100)
	@ConfigItem(
		keyName = "mediumValueVolume",
		name = "Medium volume",
		description = "Volume for Medium.wav. 0 is silent.",
		position = 10,
		section = valueSounds
	)
	default int mediumValueVolume()
	{
		return 100;
	}

	@ConfigItem(
		keyName = "highValuePrice",
		name = "High price",
		description = "Start price for High.wav. 0 disables this tier.",
		position = 11,
		section = valueSounds
	)
	default int highValuePrice()
	{
		return 1_000_000;
	}

	@Range(min = 0, max = 100)
	@ConfigItem(
		keyName = "highValueVolume",
		name = "High volume",
		description = "Volume for High.wav. 0 is silent.",
		position = 12,
		section = valueSounds
	)
	default int highValueVolume()
	{
		return 100;
	}

	@ConfigItem(
		keyName = "insaneValuePrice",
		name = "Insane price",
		description = "Start price for Insane.wav. 0 disables this tier.",
		position = 13,
		section = valueSounds
	)
	default int insaneValuePrice()
	{
		return 10_000_000;
	}

	@Range(min = 0, max = 100)
	@ConfigItem(
		keyName = "insaneValueVolume",
		name = "Insane volume",
		description = "Volume for Insane.wav. 0 is silent.",
		position = 14,
		section = valueSounds
	)
	default int insaneValueVolume()
	{
		return 100;
	}

	@ConfigItem(
		keyName = "insanePlusValuePrice",
		name = "Insane+ price",
		description = "Start price for Insane+.wav. 0 disables this tier.",
		position = 15,
		section = valueSounds
	)
	default int insanePlusValuePrice()
	{
		return 50_000_000;
	}

	@Range(min = 0, max = 100)
	@ConfigItem(
		keyName = "insanePlusValueVolume",
		name = "Insane+ volume",
		description = "Volume for Insane+.wav. 0 is silent.",
		position = 16,
		section = valueSounds
	)
	default int insanePlusValueVolume()
	{
		return 100;
	}

	@ConfigItem(
		keyName = "insanePlusPlusValuePrice",
		name = "Insane++ price",
		description = "Start price for Insane++.wav. 0 disables this tier.",
		position = 17,
		section = valueSounds
	)
	default int insanePlusPlusValuePrice()
	{
		return 250_000_000;
	}

	@Range(min = 0, max = 100)
	@ConfigItem(
		keyName = "insanePlusPlusValueVolume",
		name = "Insane++ volume",
		description = "Volume for Insane++.wav. 0 is silent.",
		position = 18,
		section = valueSounds
	)
	default int insanePlusPlusValueVolume()
	{
		return 100;
	}
}
