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

	/**
	 * The sarcophagus and the purple Archive chest have a reveal before their loot interface.
	 * Chambers of Xeric, Theatre of Blood, and the smaller Archive chests play when that interface opens.
	 */
	public static boolean suspenseLoot(int groupId)
	{
		return groupId == InterfaceID.TOA_CHESTS || groupId == InterfaceID.TFA_CHESTS;
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
