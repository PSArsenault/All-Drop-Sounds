package com.alldropsounds;

import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.SpotanimID;
import org.junit.Assert;
import org.junit.Test;

public class TfaLootRevealTest
{
	@Test
	public void itemSpotanimIsTheRewardThatAppearsAfterTheGlow()
	{
		Assert.assertEquals(ItemID.RONDACHE, TfaLootReveal.itemId(SpotanimID.VFX_TFA_LOOT_RONDACHE_01));
		Assert.assertEquals(ItemID.RONDACHE, TfaLootReveal.itemId(SpotanimID.VFX_TFA_LOOT_RONDACHE_02));
		Assert.assertEquals(ItemID.ZORYAS_TOME_UNCHARGED, TfaLootReveal.itemId(SpotanimID.VFX_TFA_LOOT_TOME_01));
		Assert.assertEquals(ItemID.ZORYAS_TOME_UNCHARGED, TfaLootReveal.itemId(SpotanimID.VFX_TFA_LOOT_TOME_02));
		Assert.assertEquals(ItemID.OBLIGATOR_UNCHARGED, TfaLootReveal.itemId(SpotanimID.VFX_TFA_LOOT_OBLIGATOR_01));
		Assert.assertEquals(ItemID.OBLIGATOR_UNCHARGED, TfaLootReveal.itemId(SpotanimID.VFX_TFA_LOOT_OBLIGATOR_02));
		Assert.assertEquals(ItemID.ASCENSION_CROSSBOWS, TfaLootReveal.itemId(SpotanimID.VFX_TFA_LOOT_CROSSBOW_01));
		Assert.assertEquals(ItemID.ASCENSION_CROSSBOWS, TfaLootReveal.itemId(SpotanimID.VFX_TFA_LOOT_CROSSBOW_02));
		Assert.assertEquals(ItemID.TFA_ZEAL_REWARD, TfaLootReveal.itemId(SpotanimID.VFX_TFA_LOOT_ZEAL_01));
		Assert.assertEquals(ItemID.TFA_ZEAL_REWARD, TfaLootReveal.itemId(SpotanimID.VFX_TFA_LOOT_ZEAL_02));
	}

	@Test
	public void glowAndStandardChestSparklesAreNotLoot()
	{
		Assert.assertEquals(0, TfaLootReveal.itemId(SpotanimID.VFX_TFA_LOOT_LIGHT));
		Assert.assertEquals(0, TfaLootReveal.itemId(SpotanimID.VFX_TFA_SMALL_CHEST_SPARKLES));
		Assert.assertEquals(0, TfaLootReveal.itemId(0));
	}
}
