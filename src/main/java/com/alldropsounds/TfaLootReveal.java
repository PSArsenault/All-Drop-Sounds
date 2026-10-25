package com.alldropsounds;

import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.SpotanimID;

/**
 * The Fractured Archive unique chest plays a glow, then a spotanim of the reward itself.
 * The glow is not the item. Each reward has two spotanims for the same item.
 */
public final class TfaLootReveal
{
	private TfaLootReveal()
	{
	}

	public static int itemId(int spotanimId)
	{
		if (spotanimId == SpotanimID.VFX_TFA_LOOT_RONDACHE_01
			|| spotanimId == SpotanimID.VFX_TFA_LOOT_RONDACHE_02)
		{
			return ItemID.RONDACHE;
		}
		if (spotanimId == SpotanimID.VFX_TFA_LOOT_TOME_01
			|| spotanimId == SpotanimID.VFX_TFA_LOOT_TOME_02)
		{
			return ItemID.ZORYAS_TOME_UNCHARGED;
		}
		if (spotanimId == SpotanimID.VFX_TFA_LOOT_OBLIGATOR_01
			|| spotanimId == SpotanimID.VFX_TFA_LOOT_OBLIGATOR_02)
		{
			return ItemID.OBLIGATOR_UNCHARGED;
		}
		if (spotanimId == SpotanimID.VFX_TFA_LOOT_CROSSBOW_01
			|| spotanimId == SpotanimID.VFX_TFA_LOOT_CROSSBOW_02)
		{
			return ItemID.ASCENSION_CROSSBOWS;
		}
		if (spotanimId == SpotanimID.VFX_TFA_LOOT_ZEAL_01
			|| spotanimId == SpotanimID.VFX_TFA_LOOT_ZEAL_02)
		{
			return ItemID.TFA_ZEAL_REWARD;
		}
		return 0;
	}
}
