package com.alldropsounds;

import org.junit.Assert;
import org.junit.Test;

public class ValueTiersTest
{
	private static final int LOW = 20_000;
	private static final int MEDIUM = 100_000;
	private static final int HIGH = 1_000_000;
	private static final int INSANE = 10_000_000;
	private static final int INSANE_PLUS = 50_000_000;
	private static final int INSANE_PLUS_PLUS = 250_000_000;

	@Test
	public void usesHighestTierTheValueMeets()
	{
		Assert.assertNull(match(19_999));
		Assert.assertEquals(ValueTier.LOW, match(20_000));
		Assert.assertEquals(ValueTier.MEDIUM, match(100_000));
		Assert.assertEquals(ValueTier.HIGH, match(1_000_000));
		Assert.assertEquals(ValueTier.INSANE, match(10_000_000));
		Assert.assertEquals(ValueTier.INSANE_PLUS, match(50_000_000));
		Assert.assertEquals(ValueTier.INSANE_PLUS_PLUS, match(250_000_000));
		Assert.assertEquals(ValueTier.INSANE_PLUS_PLUS, match(2_000_000_000L));
	}

	@Test
	public void disabledTierIsSkipped()
	{
		Assert.assertEquals(ValueTier.HIGH, ValueTiers.match(5_000_000, LOW, MEDIUM, HIGH, 0, 0, 0));
	}

	@Test
	public void stackValueUsesQuantity()
	{
		Assert.assertEquals(150_000L, PriceSource.stackValue(PriceSource.unitPrice(1, 0, PriceSource.GRAND_EXCHANGE), 150_000));
		Assert.assertEquals(500L, PriceSource.unitPrice(200, 500, PriceSource.HIGHEST));
		Assert.assertEquals(200L, PriceSource.unitPrice(200, 500, PriceSource.GRAND_EXCHANGE));
		Assert.assertEquals(0L, PriceSource.stackValue(0, 10));
	}

	@Test
	public void partialStackIsTheGainedAmount()
	{
		java.util.Map<Integer, Integer> before = new java.util.HashMap<>();
		before.put(566, 10);
		java.util.Map<Integer, Integer> after = new java.util.HashMap<>();
		after.put(566, 50);
		Assert.assertEquals(java.util.Collections.singletonMap(566, 40), InventoryDiff.gainedAmounts(before, after));
	}

	private static ValueTier match(long value)
	{
		return ValueTiers.match(value, LOW, MEDIUM, HIGH, INSANE, INSANE_PLUS, INSANE_PLUS_PLUS);
	}
}
