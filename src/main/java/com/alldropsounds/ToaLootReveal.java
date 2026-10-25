package com.alldropsounds;

/**
 * The Tombs of Amascut sarcophagus shows a loot object when the reward appears.
 * ObjectID1 is package-private, so the ids are copied here.
 */
public final class ToaLootReveal
{
	/** ObjectID1.TOA_VAULT_LOOT01 */
	public static final int LOOT = 44935;
	/** ObjectID1.TOA_VAULT_LOOT01_VOUCHER */
	public static final int LOOT_VOUCHER = 55807;

	private ToaLootReveal()
	{
	}

	public static boolean isLootObject(int objectId)
	{
		return objectId == LOOT || objectId == LOOT_VOUCHER;
	}
}
