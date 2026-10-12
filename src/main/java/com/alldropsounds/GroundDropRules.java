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

	public static final int SELF_DROP_WINDOW = 4;

	public static boolean shouldPlay(boolean playItemsYouDrop, boolean onlyYourDrops, int ownership, boolean selfDrop)
	{
		if (selfDrop)
		{
			return playItemsYouDrop;
		}
		if (onlyYourDrops && !isYourDrop(ownership))
		{
			return false;
		}
		return true;
	}

	public static boolean isManualDrop(String menuOption, boolean itemOp)
	{
		return itemOp && menuOption != null && menuOption.equalsIgnoreCase("Drop");
	}

	public static boolean isRememberedDrop(Integer droppedAtTick, int spawnTick)
	{
		if (droppedAtTick == null)
		{
			return false;
		}

		long delta = (long) spawnTick - droppedAtTick;
		return delta >= -SELF_DROP_WINDOW && delta <= SELF_DROP_WINDOW;
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
		return delta > SELF_DROP_WINDOW;
	}
}
