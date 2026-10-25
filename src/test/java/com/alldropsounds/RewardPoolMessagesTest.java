package com.alldropsounds;

import org.junit.Assert;
import org.junit.Test;

public class RewardPoolMessagesTest
{
	@Test
	public void matchesPoolLootLines()
	{
		Assert.assertTrue(RewardPoolMessages.isRewardPool("You found some loot: 27 x Raw lobster"));
		Assert.assertTrue(RewardPoolMessages.isRewardPool("<col=0000ff>You found some loot: 1 x Spirit flakes</col>"));
		Assert.assertTrue(RewardPoolMessages.isRewardPool("  You found some loot: Tome of fire"));
	}

	@Test
	public void ignoresChestAndUnrelatedChat()
	{
		Assert.assertFalse(RewardPoolMessages.isRewardPool(null));
		Assert.assertFalse(RewardPoolMessages.isRewardPool(""));
		Assert.assertFalse(RewardPoolMessages.isRewardPool("You find some loot inside."));
		Assert.assertFalse(RewardPoolMessages.isRewardPool("You found some loot in the chest!"));
		Assert.assertFalse(RewardPoolMessages.isRewardPool("You find some treasure in the chest!"));
	}
}
