package com.alldropsounds;

import java.nio.file.Path;

/**
 * Picks the wav and volume for one item. A specific item file wins over the tier file.
 * A missing file or a volume of 0 or less means nothing should play.
 */
public final class DropSoundPlan
{
	public final Path file;
	public final int volume;

	private DropSoundPlan(Path file, int volume)
	{
		this.file = file;
		this.volume = volume;
	}

	public static DropSoundPlan choose(Path specificFile, Path tierFile, int itemVolume, int tierVolume)
	{
		if (specificFile != null)
		{
			return playable(specificFile, itemVolume);
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
