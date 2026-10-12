package com.alldropsounds;

import net.runelite.client.util.Text;

public final class ItemNames
{
	private ItemNames()
	{
	}

	public static String clean(String raw)
	{
		if (raw == null)
		{
			return null;
		}

		String name = Text.removeTags(raw).trim();
		if (name.isEmpty() || name.equalsIgnoreCase("null"))
		{
			return null;
		}
		return name;
	}
}
