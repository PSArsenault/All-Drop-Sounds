package com.alldropsounds;

import net.runelite.api.SoundEffectVolume;

/**
 * Scales plugin volume by the in-game sound sliders.
 * Zero on either slider that is actually in use mutes the sound.
 */
public final class GameAudio
{
	private GameAudio()
	{
	}

	public static int outputPercent(int masterSlider, int effectsSlider)
	{
		int effects = sliderPercent(effectsSlider);
		if (effects <= 0)
		{
			return 0;
		}

		int master = masterSlider <= 0 ? 0 : sliderPercent(masterSlider);
		if (master <= 0)
		{
			return 0;
		}
		return effects * master / 100;
	}

	public static int applyOutput(int volume, int outputPercent)
	{
		if (volume <= 0 || outputPercent <= 0)
		{
			return 0;
		}
		int scaled = (int) Math.round(Math.min(100, volume) * (Math.min(100, outputPercent) / 100.0));
		return Math.max(0, Math.min(100, scaled));
	}

	static int sliderPercent(int value)
	{
		if (value <= 0)
		{
			return 0;
		}
		if (value <= 4)
		{
			return value * 25;
		}
		if (value >= SoundEffectVolume.HIGH)
		{
			return 100;
		}
		return Math.min(100, (value * 100 + SoundEffectVolume.HIGH / 2) / SoundEffectVolume.HIGH);
	}
}
