package com.alldropsounds;

import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.gameval.VarbitID;

/**
 * Purple raid chests that are visible before the reward items are known.
 * Ids above 31999 live in RuneLite's package-private ObjectID1, so they are copied here.
 */
public final class PurpleChests
{
	public static final String FILE = "purple";
	public static final String TOA_KEY = "toa";

	/** ObjectID.RAIDS_REWARD_LOOTBEAM_SPECIAL. The white, kit, and dust beams are separate objects. */
	public static final int COX_PURPLE_BEAM = ObjectID.RAIDS_REWARD_LOOTBEAM_SPECIAL;
	/** ObjectID.RAIDS_REWARD_LOOTBEAM. Multiloc parent of the white, purple, kit, and dust beams. */
	public static final int COX_LOOTBEAM_PARENT = ObjectID.RAIDS_REWARD_LOOTBEAM;
	/** ObjectID1.TOB_TREASUREROOM_CHEST_MINE_RARE */
	public static final int TOB_PURPLE_CHEST_MINE = 32993;
	/** ObjectID1.TOB_TREASUREROOM_CHEST_NOTMINE_RARE */
	public static final int TOB_PURPLE_CHEST_OTHER = 32991;
	/** ObjectID1.TOA_VAULT_SARCOPHAGUS_CLOSED_RARE */
	public static final int TOA_PURPLE_SARCOPHAGUS = 44826;
	/** ObjectID1.TOA_VAULT_SARCOPHAGUS_PARENT */
	public static final int TOA_SARCOPHAGUS_PARENT = 46220;
	/** ObjectID1.TOA_VAULT_BARRIER_PARENT */
	public static final int TOA_SARCOPHAGUS_BARRIER = 46221;
	/** ObjectID1.TOA_VAULT_SARCOPHAGUS_CLOSED */
	public static final int TOA_WHITE_SARCOPHAGUS = 44825;

	private PurpleChests()
	{
	}

	public static boolean isPurpleObject(int objectId)
	{
		return objectId == COX_PURPLE_BEAM
			|| objectId == TOB_PURPLE_CHEST_MINE
			|| objectId == TOB_PURPLE_CHEST_OTHER
			|| objectId == TOA_PURPLE_SARCOPHAGUS;
	}

	/**
	 * Multiloc parents that can transform into a purple chest.
	 * Direct purple ids are already the rare object, so they do not need an impostor lookup.
	 */
	public static boolean isPurpleParent(int objectId)
	{
		return objectId == COX_LOOTBEAM_PARENT
			|| objectId == TOA_SARCOPHAGUS_PARENT
			|| objectId == TOA_SARCOPHAGUS_BARRIER;
	}

	public static boolean isToaSarcophagus(int objectId)
	{
		return objectId == TOA_SARCOPHAGUS_PARENT
			|| objectId == TOA_SARCOPHAGUS_BARRIER
			|| objectId == TOA_WHITE_SARCOPHAGUS
			|| objectId == TOA_PURPLE_SARCOPHAGUS;
	}

	public static boolean isPurpleNpc(int npcId)
	{
		return npcId == NpcID.TFA_UNIQUE_CHEST_VAULT_CLOSED;
	}

	public static boolean isToaSarcophagusVarbit(int varbitId)
	{
		return varbitId == VarbitID.TOA_VAULT_SARCOPHAGUS;
	}

	/**
	 * The Tombs of Amascut sarcophagus varbit is odd when the chest is purple.
	 */
	public static boolean toaSarcophagusIsPurple(int varbitValue)
	{
		return varbitValue % 2 != 0;
	}

	public static String objectKey(int objectId, int x, int y, int plane)
	{
		if (objectId == TOA_PURPLE_SARCOPHAGUS)
		{
			return TOA_KEY;
		}
		return objectId + ":" + x + ":" + y + ":" + plane;
	}
}
