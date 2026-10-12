package com.alldropsounds;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class ChestLootWatchTest
{
	@Test
	public void lootAlreadyInInventoryWhenChestMessageArrives()
	{
		ChestLootWatch watch = new ChestLootWatch();
		watch.reset(Collections.emptyMap());

		watch.onInventoryChange(counts(4151, 1));

		Assert.assertEquals(Collections.singletonMap(4151, 1), watch.onChestMessage());
		Assert.assertFalse(watch.isAwaiting());
	}

	@Test
	public void lootArrivesAfterChestMessage()
	{
		ChestLootWatch watch = new ChestLootWatch();
		watch.reset(Collections.emptyMap());
		watch.onGameTick();

		Assert.assertTrue(watch.onChestMessage().isEmpty());
		Assert.assertEquals(Collections.singletonMap(4151, 1), watch.onInventoryChange(counts(4151, 1)));
		Assert.assertFalse(watch.isAwaiting());
	}

	@Test
	public void pickupAfterChestDoesNotCount()
	{
		ChestLootWatch watch = new ChestLootWatch();
		watch.reset(Collections.emptyMap());
		watch.onChestMessage();
		watch.onInventoryChange(counts(4151, 1));
		watch.onGameTick();

		Assert.assertTrue(watch.onInventoryChange(counts(4151, 1, 995, 100)).isEmpty());
	}

	@Test
	public void droppingAnItemIsRemembered()
	{
		ChestLootWatch watch = new ChestLootWatch();
		watch.reset(counts(385, 1));

		Assert.assertTrue(watch.onInventoryChange(Collections.emptyMap()).isEmpty());
		Assert.assertEquals(Collections.singletonList(385), watch.getLastLost());
	}

	@Test
	public void watchExpiresWithoutLoot()
	{
		ChestLootWatch watch = new ChestLootWatch();
		watch.reset(Collections.emptyMap());
		watch.onChestMessage();
		watch.onGameTick();

		for (int i = 0; i < ChestLootWatch.WATCH_TICKS; i++)
		{
			watch.onGameTick();
		}

		Assert.assertFalse(watch.isAwaiting());
		Assert.assertTrue(watch.onInventoryChange(counts(995, 1)).isEmpty());
	}

	private static Map<Integer, Integer> counts(int... idThenQuantity)
	{
		Map<Integer, Integer> counts = new HashMap<>();
		for (int i = 0; i < idThenQuantity.length; i += 2)
		{
			counts.put(idThenQuantity[i], idThenQuantity[i + 1]);
		}
		return counts;
	}

	@Test
	public void ignoresEmptyAndNonPositiveIds()
	{
		Map<Integer, Integer> after = new HashMap<>();
		after.put(-1, 1);
		after.put(0, 5);
		after.put(995, 10);
		Assert.assertEquals(Collections.singletonList(995), InventoryDiff.increased(Collections.emptyMap(), after));
		Assert.assertEquals(Collections.emptyList(), InventoryDiff.decreased(Collections.emptyMap(), after));
		Assert.assertEquals(Arrays.asList(995), InventoryDiff.decreased(counts(995, 20), counts(995, 10)));
	}
}
