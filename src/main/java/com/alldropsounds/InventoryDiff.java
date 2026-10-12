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

			int previous = quantity(before, id);
			if (quantity(entry.getValue()) > previous)
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

			int previous = quantity(before, id);
			long added = (long) quantity(entry.getValue()) - previous;
			if (added > Integer.MAX_VALUE)
			{
				gained.put(id, Integer.MAX_VALUE);
			}
			else if (added > 0)
			{
				gained.put(id, (int) added);
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

			long next = quantity(after, id);
			if (quantity(entry.getValue()) > next)
			{
				lost.add(id);
			}
		}
		return lost;
	}

	private static int quantity(Map<Integer, Integer> counts, int id)
	{
		if (counts == null)
		{
			return 0;
		}
		return quantity(counts.get(id));
	}

	/**
	 * Adds two stack counts without throwing when the sum does not fit in an int.
	 */
	public static int addQuantity(int left, int right)
	{
		try
		{
			return Math.addExact(left, right);
		}
		catch (ArithmeticException ex)
		{
			return Integer.MAX_VALUE;
		}
	}

	private static int quantity(Integer value)
	{
		return value == null ? 0 : value;
	}
}
