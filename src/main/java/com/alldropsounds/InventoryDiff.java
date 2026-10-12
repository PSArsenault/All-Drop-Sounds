package com.alldropsounds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Differences between two item-id to quantity maps.
 */
public final class InventoryDiff
{
	private InventoryDiff()
	{
	}

	public static List<Integer> increased(Map<Integer, Integer> before, Map<Integer, Integer> after)
	{
		if (after == null || after.isEmpty())
		{
			return Collections.emptyList();
		}

		List<Integer> gained = new ArrayList<>();
		for (Map.Entry<Integer, Integer> entry : after.entrySet())
		{
			int id = entry.getKey();
			if (id <= 0)
			{
				continue;
			}

			int previous = before == null ? 0 : before.getOrDefault(id, 0);
			if (entry.getValue() > previous)
			{
				gained.add(id);
			}
		}
		return gained;
	}

	public static Map<Integer, Integer> gainedAmounts(Map<Integer, Integer> before, Map<Integer, Integer> after)
	{
		if (after == null || after.isEmpty())
		{
			return Collections.emptyMap();
		}

		Map<Integer, Integer> gained = new LinkedHashMap<>();
		for (Map.Entry<Integer, Integer> entry : after.entrySet())
		{
			int id = entry.getKey();
			if (id <= 0)
			{
				continue;
			}

			int previous = before == null ? 0 : before.getOrDefault(id, 0);
			int added = entry.getValue() - previous;
			if (added > 0)
			{
				gained.put(id, added);
			}
		}
		return gained;
	}

	public static List<Integer> decreased(Map<Integer, Integer> before, Map<Integer, Integer> after)
	{
		if (before == null || before.isEmpty())
		{
			return Collections.emptyList();
		}

		List<Integer> lost = new ArrayList<>();
		for (Map.Entry<Integer, Integer> entry : before.entrySet())
		{
			int id = entry.getKey();
			if (id <= 0)
			{
				continue;
			}

			int next = after == null ? 0 : after.getOrDefault(id, 0);
			if (entry.getValue() > next)
			{
				lost.add(id);
			}
		}
		return lost;
	}
}
