package com.alldropsounds;

import java.util.Locale;

/**
 * Item names that must stay silent even when a wav exists or a price tier matches.
 */
public final class IgnoredItems
{
	private IgnoredItems()
	{
	}

	public static boolean contains(String list, String itemName)
	{
		String key = normalize(itemName);
		if (key == null || list == null || list.isEmpty())
		{
			return false;
		}

		String[] parts = list.split("[,;\\r\\n]+");
		for (String part : parts)
		{
			if (key.equals(normalize(part)))
			{
				return true;
			}
		}
		return false;
	}

	private static String normalize(String raw)
	{
		String cleaned = ItemNames.clean(raw);
		if (cleaned == null)
		{
			return null;
		}
		return cleaned.replaceAll("\\s+", " ").toLowerCase(Locale.ENGLISH);
	}
}
