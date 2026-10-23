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
		name = "Sound volume",
		description = "Volume for each kind of drop sound. 0 is silent. Master volume scales every sound.",
		position = 7
	)
	String soundVolume = "soundVolume";

	@ConfigSection(
		name = "Value sounds",
		description = "Start prices for tier wavs used when an item has no file of its own, is not highlighted, and is not an untradeable unique.",
		position = 8
	)
	String valueSounds = "valueSounds";

	@ConfigItem(
		keyName = "playGroundDrops",
		name = "Ground drops",
		description = "Play when one of your items appears on the ground.",
		position = 1
	)
	default boolean playGroundDrops()
	{
		return true;
	}

	@ConfigItem(
		keyName = "playChestLoot",
		name = "Chest loot",
		description = "Play when an item is received from a chest, including Barrows, the Gauntlet, and raid chests.",
		position = 2
	)
	default boolean playChestLoot()
	{
		return true;
	}

	@ConfigItem(
		keyName = "onlyYourDrops",
		name = "Only your drops",
		description = "Ground sounds play for your drops and group drops.",
		position = 3
	)
	default boolean onlyYourDrops()
	{
		return true;
	}

	@ConfigItem(
		keyName = "playSelfDrops",
		name = "Items you drop",
		description = "Play a sound when you drop an item from your inventory onto the ground.",
		position = 4
	)
	default boolean playSelfDrops()
	{
		return false;
	}

	@ConfigItem(
		keyName = "ignoredItems",
		name = "Ignored items",
		description = "Comma-separated item names that never play a sound, even when a wav file exists or the stack meets a price tier.",
		position = 5
	)
	default String ignoredItems()
	{
		return "";
	}

	@ConfigItem(
		keyName = "playUniques",
		name = "Untradeable uniques",
		description = "Play a category wav for pets and other untradeable uniques that have no price. pets.wav, jars.wav, and champions.wav are used when those files exist. Unique.wav covers the rest. An item file such as skotos.wav still wins.",
		position = 6
	)
	default boolean playUniques()
	{
		return true;
	}

	@ConfigItem(
		keyName = "purpleNotification",
		name = "Purple notification",
		description = "Play purple.wav when a purple chest appears in Chambers of Xeric, Theatre of Blood, Tombs of Amascut, or The Fractured Archive, before the loot is revealed. Master volume scales this sound.",
		position = 7
	)
	default boolean purpleNotification()
	{
		return true;
	}

	@Range(min = 0, max = 100)
	@ConfigItem(
		keyName = "masterVolume",
		name = "Master volume",
		description = "Scales every drop sound. 0 is silent.",
		position = 0,
		section = soundVolume
	)
	default int masterVolume()
	{
		return 100;
	}

	@Range(min = 0, max = 100)
	@ConfigItem(
		keyName = "volume",
		name = "Specific drop volume",
		description = "Volume for wav files named after a specific item. 0 is silent.",
		position = 1,
		section = soundVolume
	)
	default int itemVolume()
	{
		return 100;
	}

	@Range(min = 0, max = 100)
	@ConfigItem(
		keyName = "highlightedVolume",
		name = "Highlighted volume",
		description = "Volume for highlighted.wav. Names come from the Ground Items highlighted list and any other plugin that stores one. 0 is silent.",
		position = 2,
		section = soundVolume
	)
	default int highlightedVolume()
	{
		return 100;
	}

	@Range(min = 0, max = 100)
	@ConfigItem(
		keyName = "uniqueVolume",
		name = "Unique volume",
		description = "Volume for category wavs such as pets.wav, jars.wav, champions.wav, and Unique.wav. 0 is silent.",
		position = 3,
		section = soundVolume
	)
	default int uniqueVolume()
	{
		return 100;
	}

	@Range(min = 0, max = 100)
	@ConfigItem(
		keyName = "lowValueVolume",
		name = "Low volume",
		description = "Volume for Low.wav. 0 is silent.",
		position = 4,
		section = soundVolume
	)
	default int lowValueVolume()
	{
		return 100;
	}

	@Range(min = 0, max = 100)
	@ConfigItem(
		keyName = "mediumValueVolume",
		name = "Medium volume",
		description = "Volume for Medium.wav. 0 is silent.",
		position = 5,
		section = soundVolume
	)
	default int mediumValueVolume()
	{
		return 100;
	}

	@Range(min = 0, max = 100)
	@ConfigItem(
		keyName = "highValueVolume",
		name = "High volume",
		description = "Volume for High.wav. 0 is silent.",
		position = 6,
		section = soundVolume
	)
	default int highValueVolume()
	{
		return 100;
	}

	@Range(min = 0, max = 100)
	@ConfigItem(
		keyName = "insaneValueVolume",
		name = "Insane volume",
		description = "Volume for Insane.wav. 0 is silent.",
		position = 7,
		section = soundVolume
	)
	default int insaneValueVolume()
	{
		return 100;
	}

	@Range(min = 0, max = 100)
	@ConfigItem(
		keyName = "insanePlusValueVolume",
		name = "Insane+ volume",
		description = "Volume for Insane+.wav. 0 is silent.",
		position = 8,
		section = soundVolume
	)
	default int insanePlusValueVolume()
	{
		return 100;
	}

	@Range(min = 0, max = 100)
	@ConfigItem(
		keyName = "insanePlusPlusValueVolume",
		name = "Insane++ volume",
		description = "Volume for Insane++.wav. 0 is silent.",
		position = 9,
		section = soundVolume
	)
	default int insanePlusPlusValueVolume()
	{
		return 100;
	}

	@ConfigItem(
		keyName = "priceSource",
		name = "Price source",
		description = "Which price is compared with the tier start prices. Stacks use price times quantity.",
		position = 0,
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
		position = 1,
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
		position = 2,
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
		position = 3,
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
		position = 4,
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
		position = 5,
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
		position = 6,
		section = valueSounds
	)
	default int insanePlusPlusValuePrice()
	{
		return 250_000_000;
	}
}
