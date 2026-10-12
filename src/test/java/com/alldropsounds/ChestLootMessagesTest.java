package com.alldropsounds;

import org.junit.Assert;
import org.junit.Test;

public class ChestLootMessagesTest
{
	@Test
	public void matchesChestLootLines()
	{
		Assert.assertTrue(ChestLootMessages.isChestLoot("You find some treasure in the chest!"));
		Assert.assertTrue(ChestLootMessages.isChestLoot("You steal some loot from the chest."));
		Assert.assertTrue(ChestLootMessages.isChestLoot("You find treasure inside!"));
		Assert.assertTrue(ChestLootMessages.isChestLoot("You take some loot from inside."));
		Assert.assertTrue(ChestLootMessages.isChestLoot("You open the chest and find a dragon med helm."));
		Assert.assertTrue(ChestLootMessages.isChestLoot("You have opened Larran's big chest and find some loot."));
		Assert.assertTrue(ChestLootMessages.isChestLoot("You have opened Larran's small chest once."));
		Assert.assertTrue(ChestLootMessages.isChestLoot("You find treasure and supplies within the chest."));
		Assert.assertTrue(ChestLootMessages.isChestLoot("You find treasure, supplies, and a weirdly coloured egg sac within the chest."));
		Assert.assertTrue(ChestLootMessages.isChestLoot("You find some loot inside."));
		Assert.assertTrue(ChestLootMessages.isChestLoot("Your bronze key breaks in the lock."));
	}

	@Test
	public void ignoresUnrelatedChat()
	{
		Assert.assertFalse(ChestLootMessages.isChestLoot(null));
		Assert.assertFalse(ChestLootMessages.isChestLoot(""));
		Assert.assertFalse(ChestLootMessages.isChestLoot("You open the chest."));
		Assert.assertFalse(ChestLootMessages.isChestLoot("Valuable drop: Twisted bow (1,234,567 coins)"));
	}
}
