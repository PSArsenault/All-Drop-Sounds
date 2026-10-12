package com.alldropsounds;

/**
 * Picks the tier with the highest enabled start price that the stack still meets.
 * A start price of 0 or less turns that tier off.
 */
public final class ValueTiers
{
	private ValueTiers()
	{
	}

	public static ValueTier match(long value, int low, int medium, int high, int insane, int insanePlus, int insanePlusPlus)
	{
		int[] thresholds = {low, medium, high, insane, insanePlus, insanePlusPlus};
		ValueTier[] tiers = ValueTier.values();
		ValueTier best = null;
		int bestThreshold = -1;

		for (int i = 0; i < tiers.length; i++)
		{
			int threshold = thresholds[i];
			if (threshold > 0 && value >= threshold && threshold >= bestThreshold)
			{
				best = tiers[i];
				bestThreshold = threshold;
			}
		}
		return best;
	}
}
