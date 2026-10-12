package com.alldropsounds;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Watches inventory changes that follow a chest-loot chat message.
 * Loot can be applied either just before or just after that message.
 */
public final class ChestLootWatch
{
	static final int WATCH_TICKS = 5;

	private Map<Integer, Integer> baseline = new HashMap<>();
	private Map<Integer, Integer> inventory = new HashMap<>();
	private boolean awaiting;
	private int ticksLeft;
	private boolean startedThisTick;
	private boolean capturedThisTick;
	private List<Integer> lastLost = Collections.emptyList();

	public void reset(Map<Integer, Integer> counts)
	{
		inventory = copy(counts);
		baseline = copy(counts);
		awaiting = false;
		ticksLeft = 0;
		startedThisTick = false;
		capturedThisTick = false;
		lastLost = Collections.emptyList();
	}

	public void onGameTick()
	{
		capturedThisTick = false;
		if (startedThisTick)
		{
			startedThisTick = false;
		}
		else if (ticksLeft > 0)
		{
			ticksLeft--;
			if (ticksLeft == 0)
			{
				awaiting = false;
			}
		}

		if (!awaiting)
		{
			baseline = copy(inventory);
		}
	}

	public Map<Integer, Integer> onChestMessage()
	{
		if (capturedThisTick)
		{
			return Collections.emptyMap();
		}

		awaiting = true;
		ticksLeft = WATCH_TICKS;
		startedThisTick = true;
		lastLost = Collections.emptyList();

		Map<Integer, Integer> gained = InventoryDiff.gainedAmounts(baseline, inventory);
		if (!gained.isEmpty())
		{
			awaiting = false;
			baseline = copy(inventory);
			capturedThisTick = true;
		}
		return gained;
	}

	public Map<Integer, Integer> onInventoryChange(Map<Integer, Integer> now)
	{
		List<Integer> lost = InventoryDiff.decreased(inventory, now);
		Map<Integer, Integer> gained = Collections.emptyMap();
		if (awaiting)
		{
			gained = InventoryDiff.gainedAmounts(baseline, now);
			if (!gained.isEmpty())
			{
				awaiting = false;
				baseline = copy(now);
				capturedThisTick = true;
			}
		}

		inventory = copy(now);
		lastLost = lost;
		return gained;
	}

	public List<Integer> getLastLost()
	{
		return lastLost;
	}

	public boolean isAwaiting()
	{
		return awaiting;
	}

	private static Map<Integer, Integer> copy(Map<Integer, Integer> counts)
	{
		return counts == null ? new HashMap<>() : new HashMap<>(counts);
	}
}
