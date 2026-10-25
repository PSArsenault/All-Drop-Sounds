package com.alldropsounds;

import net.runelite.client.util.Text;

/**
 * Chat line used when a reward pool puts loot straight into the inventory.
 * Tempoross, Guardians of the Rift, and the Wintertodt reward cart all use it, including big-search.
 */
public final class RewardPoolMessages
{
	static final String PREFIX = "You found some loot:";

	private RewardPoolMessages()
	{
	}

	public static boolean isRewardPool(String message)
	{
		if (message == null)
		{
			return false;
		}
		return Text.removeTags(message).trim().startsWith(PREFIX);
	}
}
