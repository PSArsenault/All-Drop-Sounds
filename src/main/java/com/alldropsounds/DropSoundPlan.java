package com.alldropsounds;

import java.nio.file.Path;

/**
 * Picks the wav and volume for one item.
 * A specific item file wins, then highlighted.wav, then the price-tier file.
 * A volume of 0 or less on the chosen file means nothing should play.
 */
public final class DropSoundPlan
{
	public static final String HIGHLIGHTED_FILE = "highlighted";

	public final Path file;
	public final int volume;

	private DropSoundPlan(Path file, int volume)
	{
		this.file = file;
		this.volume = volume;
	}

	public static DropSoundPlan choose(Path specificFile, Path tierFile, int itemVolume, int tierVolume)
	{
		return choose(specificFile, null, tierFile, itemVolume, 0, tierVolume);
	}

	public static DropSoundPlan choose(Path specificFile, Path highlightedFile, Path tierFile, int itemVolume, int highlightedVolume, int tierVolume)
	{
		if (specificFile != null)
		{
			return playable(specificFile, itemVolume);
		}
		if (highlightedFile != null)
		{
			return playable(highlightedFile, highlightedVolume);
		}
		return playable(tierFile, tierVolume);
	}

	private static DropSoundPlan playable(Path file, int volume)
	{
		if (file == null || volume <= 0)
		{
			return null;
		}
		return new DropSoundPlan(file, Math.min(100, volume));
	}
}
