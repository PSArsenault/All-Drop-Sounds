package com.alldropsounds;

import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;

/**
 * Reward interfaces whose contents are chest loot rather than a normal inventory change.
 */
public final class RewardChests
{
	public static final int THEATRE_OF_BLOOD_REGION = 12867;
	public static final int THEATRE_OF_BLOOD_LOBBY = 14642;

	private RewardChests()
	{
	}

	public static boolean isRewardChest(int groupId)
	{
		return rewardContainer(groupId) != null || groupId == InterfaceID.WILDY_LOOT_CHEST;
	}

	public static Integer rewardContainer(int groupId)
	{
		if (groupId == InterfaceID.BARROWS_REWARD)
		{
			return InventoryID.TRAIL_REWARDINV;
		}
		if (groupId == InterfaceID.RAIDS_REWARDS)
		{
			return InventoryID.RAIDS_REWARDS;
		}
		if (groupId == InterfaceID.TOB_CHESTS)
		{
			return InventoryID.TOB_CHESTS;
		}
		if (groupId == InterfaceID.TOA_CHESTS)
		{
			return InventoryID.TOA_CHESTS;
		}
		if (groupId == InterfaceID.TFA_CHESTS)
		{
			return InventoryID.TFA_CHESTS;
		}
		if (groupId == InterfaceID.PMOON_REWARD)
		{
			return InventoryID.PMOON_REWARDINV;
		}
		if (groupId == InterfaceID.COLOSSEUM_REWARD_CHEST_2)
		{
			return InventoryID.COLOSSEUM_REWARDS;
		}
		return null;
	}

	public static boolean inTheatreOfBlood(int regionId)
	{
		return regionId == THEATRE_OF_BLOOD_REGION || regionId == THEATRE_OF_BLOOD_LOBBY;
	}
}
