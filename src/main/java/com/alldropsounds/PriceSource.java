package com.alldropsounds;

public enum PriceSource
{
	GRAND_EXCHANGE("Grand Exchange"),
	HIGH_ALCHEMY("High alchemy"),
	HIGHEST("Highest");

	private final String label;

	PriceSource(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}

	public static long unitPrice(long gePrice, long haPrice, PriceSource source)
	{
		long ge = Math.max(0L, gePrice);
		long ha = Math.max(0L, haPrice);
		if (source == GRAND_EXCHANGE)
		{
			return ge;
		}
		if (source == HIGH_ALCHEMY)
		{
			return ha;
		}
		return Math.max(ge, ha);
	}

	public static long stackValue(long unitPrice, int quantity)
	{
		if (unitPrice <= 0 || quantity <= 0)
		{
			return 0L;
		}
		try
		{
			return Math.multiplyExact(unitPrice, (long) quantity);
		}
		catch (ArithmeticException ex)
		{
			return Long.MAX_VALUE;
		}
	}
}
