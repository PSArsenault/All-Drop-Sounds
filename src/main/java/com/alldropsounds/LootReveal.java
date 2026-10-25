package com.alldropsounds;

/**
 * When the item sound plays for Tombs of Amascut and The Fractured Archive.
 */
public enum LootReveal
{
	REVEAL("When the item appears"),
	INTERFACE("When the loot interface opens"),
	BOTH("Both");

	private final String label;

	LootReveal(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}

	public boolean playsOnReveal()
	{
		return this == REVEAL || this == BOTH;
	}

	public boolean playsOnInterface()
	{
		return this == INTERFACE || this == BOTH;
	}
}
