package com.alldropsounds;

import org.junit.Assert;
import org.junit.Test;

public class IgnoredItemsTest
{
	@Test
	public void blankListsAndNamesMatchNothing()
	{
		Assert.assertFalse(IgnoredItems.contains(null, "Coins"));
		Assert.assertFalse(IgnoredItems.contains("", "Coins"));
		Assert.assertFalse(IgnoredItems.contains("   ", "Coins"));
		Assert.assertFalse(IgnoredItems.contains("Coins", null));
		Assert.assertFalse(IgnoredItems.contains("Coins", ""));
		Assert.assertFalse(IgnoredItems.contains("Coins", "   "));
		Assert.assertFalse(IgnoredItems.contains("Coins", "null"));
		Assert.assertFalse(IgnoredItems.contains("null", "Coins"));
	}

	@Test
	public void namesMatchIgnoringCaseSpacingTagsAndSeparators()
	{
		Assert.assertTrue(IgnoredItems.contains("Coins", "Coins"));
		Assert.assertTrue(IgnoredItems.contains("coins", "COINS"));
		Assert.assertTrue(IgnoredItems.contains(" Coins , Bones ", "bones"));
		Assert.assertTrue(IgnoredItems.contains("Coins;Bones", "Bones"));
		Assert.assertTrue(IgnoredItems.contains("Coins\nBones\r\nTwisted bow", "Twisted bow"));
		Assert.assertTrue(IgnoredItems.contains("Coins,,Bones,", "Bones"));
		Assert.assertTrue(IgnoredItems.contains("<col=ff9040>Twisted bow</col>", "twisted bow"));
		Assert.assertTrue(IgnoredItems.contains("Twisted   bow", "Twisted bow"));
		Assert.assertTrue(IgnoredItems.contains("Dharok's helm", "dharok's helm"));
	}

	@Test
	public void onlyTheListedItemIsIgnored()
	{
		Assert.assertFalse(IgnoredItems.contains("Rune", "Rune platebody"));
		Assert.assertFalse(IgnoredItems.contains("Twisted bow", "Twisted bow (uncharged)"));
		Assert.assertFalse(IgnoredItems.contains("Low", "Coins"));
		Assert.assertTrue(IgnoredItems.contains("Low", "Low"));
		Assert.assertFalse(IgnoredItems.contains("Coins, Bones", "Big bones"));
	}
}
