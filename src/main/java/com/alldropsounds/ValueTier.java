package com.alldropsounds;

/**
 * Price bands, from cheapest to rarest. The file name is the wav to play.
 */
public enum ValueTier
{
	LOW("Low"),
	MEDIUM("Medium"),
	HIGH("High"),
	INSANE("Insane"),
	INSANE_PLUS("Insane+"),
	INSANE_PLUS_PLUS("Insane++");

	private final String fileName;

	ValueTier(String fileName)
	{
		this.fileName = fileName;
	}

	public String getFileName()
	{
		return fileName;
	}
}
