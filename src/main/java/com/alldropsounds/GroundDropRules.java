package com.alldropsounds;

import net.runelite.api.TileItem;

/**
 * Decides whether a ground item spawn should make a sound.
 */
public final class GroundDropRules
{
	private GroundDropRules()
	{
	}

	public static boolean isYourDrop(int ownership)
	{
		return ownership == TileItem.OWNERSHIP_SELF || ownership == TileItem.OWNERSHIP_GROUP;
	}

	public static boolean spawnBlocked(int tickCount, int blockUntilTick)
	{
		return tickCount < blockUntilTick;
	}

	public static boolean skipSelfDrop(boolean playItemsYouDrop, Integer droppedAtTick, int tickCount)
	{
		return !playItemsYouDrop && wasJustDropped(droppedAtTick, tickCount);
	}

	public static boolean wasJustDropped(Integer droppedAtTick, int tickCount)
	{
		if (droppedAtTick == null)
		{
			return false;
		}

		long delta = (long) tickCount - droppedAtTick;
		return delta >= 0 && delta <= 1;
	}

	public static boolean dropExpired(int droppedAtTick, int tickCount)
	{
		long delta = (long) tickCount - droppedAtTick;
		return delta > 2;
	}
}
