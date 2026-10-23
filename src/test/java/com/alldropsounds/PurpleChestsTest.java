package com.alldropsounds;

import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.gameval.VarbitID;
import org.junit.Assert;
import org.junit.Test;

public class PurpleChestsTest
{
	@Test
	public void purpleObjectsAreTheRareRaidChests()
	{
		Assert.assertEquals(ObjectID.RAIDS_REWARD_LOOTBEAM_SPECIAL, PurpleChests.COX_PURPLE_BEAM);
		Assert.assertTrue(PurpleChests.isPurpleObject(PurpleChests.COX_PURPLE_BEAM));
		Assert.assertFalse(PurpleChests.isPurpleObject(ObjectID.RAIDS_REWARD_LOOTBEAM_BASIC));
		Assert.assertFalse(PurpleChests.isPurpleObject(ObjectID.RAIDS_REWARD_CHEST));
		Assert.assertFalse(PurpleChests.isPurpleObject(37976));
		Assert.assertFalse(PurpleChests.isPurpleObject(37977));

		Assert.assertTrue(PurpleChests.isPurpleObject(PurpleChests.TOB_PURPLE_CHEST_MINE));
		Assert.assertTrue(PurpleChests.isPurpleObject(PurpleChests.TOB_PURPLE_CHEST_OTHER));
		Assert.assertFalse(PurpleChests.isPurpleObject(32992));
		Assert.assertFalse(PurpleChests.isPurpleObject(32990));
		Assert.assertFalse(PurpleChests.isPurpleObject(41746));

		Assert.assertTrue(PurpleChests.isPurpleObject(PurpleChests.TOA_PURPLE_SARCOPHAGUS));
		Assert.assertFalse(PurpleChests.isPurpleObject(PurpleChests.TOA_WHITE_SARCOPHAGUS));
		Assert.assertFalse(PurpleChests.isPurpleObject(44934));
		Assert.assertTrue(PurpleChests.isToaSarcophagus(PurpleChests.TOA_SARCOPHAGUS_PARENT));
		Assert.assertTrue(PurpleChests.isToaSarcophagus(PurpleChests.TOA_SARCOPHAGUS_BARRIER));
	}

	@Test
	public void fracturedArchivePurpleIsTheClosedUniqueChest()
	{
		Assert.assertTrue(PurpleChests.isPurpleNpc(NpcID.TFA_UNIQUE_CHEST_VAULT_CLOSED));
		Assert.assertFalse(PurpleChests.isPurpleNpc(NpcID.TFA_UNIQUE_CHEST_VAULT_OPEN));
		Assert.assertFalse(PurpleChests.isPurpleNpc(NpcID.TFA_REWARDS_CHEST_VAULT_CLOSED_MINE));
		Assert.assertFalse(PurpleChests.isPurpleNpc(NpcID.TFA_REWARDS_CHEST_VAULT_CLOSED_NOTMINE));
	}

	@Test
	public void toaSarcophagusVarbitIsOddForPurple()
	{
		Assert.assertTrue(PurpleChests.isToaSarcophagusVarbit(VarbitID.TOA_VAULT_SARCOPHAGUS));
		Assert.assertFalse(PurpleChests.isToaSarcophagusVarbit(VarbitID.TOA_VAULT_CHEST_0));
		Assert.assertTrue(PurpleChests.toaSarcophagusIsPurple(1));
		Assert.assertTrue(PurpleChests.toaSarcophagusIsPurple(3));
		Assert.assertFalse(PurpleChests.toaSarcophagusIsPurple(0));
		Assert.assertFalse(PurpleChests.toaSarcophagusIsPurple(2));
		Assert.assertEquals(PurpleChests.TOA_KEY, PurpleChests.objectKey(PurpleChests.TOA_PURPLE_SARCOPHAGUS, 1, 2, 0));
		Assert.assertEquals(PurpleChests.COX_PURPLE_BEAM + ":1:2:0", PurpleChests.objectKey(PurpleChests.COX_PURPLE_BEAM, 1, 2, 0));
	}
}
