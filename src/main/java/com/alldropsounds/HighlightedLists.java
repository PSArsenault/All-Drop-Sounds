package com.alldropsounds;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.runelite.client.util.Text;

/**
 * Highlighted item names stored by Ground Items and other plugins.
 */
public final class HighlightedLists
{
	private static final Pattern QUANTITY = Pattern.compile("^(.*?)([<>])(\\d+)$");

	private HighlightedLists()
	{
	}

	public static boolean isHighlightKey(String wholeKey)
	{
		String[] parts = groupAndKey(wholeKey);
		if (parts == null)
		{
			return false;
		}

		String leaf = parts[1].toLowerCase(Locale.ENGLISH);
		if (leaf.contains("color") || leaf.contains("volume") || leaf.contains("price"))
		{
			return false;
		}
		return "highlighteditems".equals(leaf)
			|| "highlighteditem".equals(leaf)
			|| "highlightitems".equals(leaf);
	}

	public static String[] groupAndKey(String wholeKey)
	{
		if (wholeKey == null)
		{
			return null;
		}

		int dot = wholeKey.indexOf('.');
		if (dot <= 0 || dot >= wholeKey.length() - 1)
		{
			return null;
		}

		String group = wholeKey.substring(0, dot);
		String rest = wholeKey.substring(dot + 1);
		int dot2 = rest.indexOf('.');
		String key = dot2 < 0 ? rest : rest.substring(0, dot2);
		if (key.isEmpty())
		{
			return null;
		}
		return new String[] {group, key};
	}

	public static boolean isItemList(String value)
	{
		if (value == null)
		{
			return false;
		}

		String trimmed = value.trim();
		if (trimmed.isEmpty() || trimmed.equalsIgnoreCase("true") || trimmed.equalsIgnoreCase("false"))
		{
			return false;
		}
		if (trimmed.matches("#?[0-9a-fA-F]{6,8}") || trimmed.matches("-?\\d+"))
		{
			return false;
		}
		return true;
	}

	public static boolean matches(List<String> lists, String itemName, int quantity)
	{
		if (lists == null || itemName == null)
		{
			return false;
		}

		for (String list : lists)
		{
			if (!isItemList(list))
			{
				continue;
			}
			for (String entry : entries(list))
			{
				if (entryMatches(entry, itemName, quantity))
				{
					return true;
				}
			}
		}
		return false;
	}

	private static List<String> entries(String list)
	{
		try
		{
			return Text.fromCSV(list);
		}
		catch (Exception ex)
		{
			List<String> parts = new ArrayList<String>();
			for (String part : list.split("[,;\\r\\n]+"))
			{
				parts.add(part);
			}
			return parts;
		}
	}

	static boolean entryMatches(String rawEntry, String itemName, int quantity)
	{
		if (rawEntry == null)
		{
			return false;
		}

		String entry = rawEntry.trim();
		if (entry.isEmpty())
		{
			return false;
		}

		int target = -1;
		boolean greater = false;
		Matcher quantityMatch = QUANTITY.matcher(entry);
		if (quantityMatch.matches())
		{
			entry = quantityMatch.group(1).trim();
			greater = ">".equals(quantityMatch.group(2));
			target = Integer.parseInt(quantityMatch.group(3));
		}
		if (entry.isEmpty())
		{
			return false;
		}
		if (target >= 0)
		{
			if (greater && quantity <= target)
			{
				return false;
			}
			if (!greater && quantity >= target)
			{
				return false;
			}
		}

		if (entry.indexOf('*') >= 0)
		{
			return wildcard(entry, itemName);
		}
		return itemName.equalsIgnoreCase(entry);
	}

	private static boolean wildcard(String pattern, String itemName)
	{
		StringBuilder regex = new StringBuilder("(?i)");
		for (int i = 0; i < pattern.length(); i++)
		{
			char c = pattern.charAt(i);
			if (c == '*')
			{
				regex.append(".*");
			}
			else if ("\\.^$|?+()[]{}".indexOf(c) >= 0)
			{
				regex.append('\\').append(c);
			}
			else
			{
				regex.append(c);
			}
		}
		return itemName.matches(regex.toString());
	}
}
