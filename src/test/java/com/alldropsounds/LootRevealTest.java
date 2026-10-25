package com.alldropsounds;

import org.junit.Assert;
import org.junit.Test;

public class LootRevealTest
{
	@Test
	public void revealPlaysWhenTheItemAppears()
	{
		Assert.assertTrue(LootReveal.REVEAL.playsOnReveal());
		Assert.assertFalse(LootReveal.REVEAL.playsOnInterface());
	}

	@Test
	public void interfacePlaysWhenTheLootWindowOpens()
	{
		Assert.assertFalse(LootReveal.INTERFACE.playsOnReveal());
		Assert.assertTrue(LootReveal.INTERFACE.playsOnInterface());
	}

	@Test
	public void bothPlaysAtEachMoment()
	{
		Assert.assertTrue(LootReveal.BOTH.playsOnReveal());
		Assert.assertTrue(LootReveal.BOTH.playsOnInterface());
	}

	@Test
	public void toaLootObjectIsTheRewardShownOnTheSarcophagus()
	{
		Assert.assertTrue(ToaLootReveal.isLootObject(ToaLootReveal.LOOT));
		Assert.assertTrue(ToaLootReveal.isLootObject(ToaLootReveal.LOOT_VOUCHER));
		Assert.assertFalse(ToaLootReveal.isLootObject(0));
		Assert.assertFalse(ToaLootReveal.isLootObject(44934));
	}
}
