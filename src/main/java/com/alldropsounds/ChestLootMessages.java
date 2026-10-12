package com.alldropsounds;

import java.util.regex.Pattern;
import net.runelite.client.util.Text;

/**
 * Chat lines that mean a chest has put loot into the inventory.
 */
public final class ChestLootMessages
{
	private static final Pattern LARRANS_CHEST = Pattern.compile("You have opened Larran['\\u2019]s (big|small) chest\\b.*");
	private static final Pattern GRUBBY_CHEST = Pattern.compile("You find treasure(?: and supplies|, supplies, and a weirdly coloured egg sac) within the chest\\.");
	private static final Pattern ROGUES_CHEST = Pattern.compile("You find (a|some)([a-z\\s]*) inside\\.");
	private static final Pattern HAM_CHEST = Pattern.compile("Your ([a-z]+) key breaks in the lock.*");

	private ChestLootMessages()
	{
	}

	public static boolean isChestLoot(String message)
	{
		if (message == null)
		{
			return false;
		}

		message = Text.removeTags(message).trim();
		return message.equals("You find some treasure in the chest!")
			|| message.equals("You steal some loot from the chest.")
			|| message.equals("You find treasure inside!")
			|| message.equals("You take some loot from inside.")
			|| message.startsWith("You open the chest and find")
			|| LARRANS_CHEST.matcher(message).matches()
			|| GRUBBY_CHEST.matcher(message).matches()
			|| ROGUES_CHEST.matcher(message).matches()
			|| HAM_CHEST.matcher(message).matches();
	}
}
