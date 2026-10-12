package com.alldropsounds;

/**
 * How rare a sound is. A higher rank cuts off a lower one.
 */
public final class SoundPriority
{
	public static final int NONE = Integer.MIN_VALUE;

	private SoundPriority()
	{
	}

	public static int rank(ValueTier tier, boolean namedFile, boolean highlighted, boolean unique)
	{
		if (unique)
		{
			return 8;
		}
		if (highlighted)
		{
			return 7;
		}

		int valueRank = tier == null ? 0 : tier.ordinal() + 1;
		if (namedFile)
		{
			return Math.max(valueRank, 1);
		}
		return valueRank;
	}
}
