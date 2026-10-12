package com.alldropsounds;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import net.runelite.api.TileItem;
import org.junit.Assert;
import org.junit.Test;

public class SoundFeaturesTest
{
	@Test
	public void variantsTakeTurnsAndPrefixesMatchQualifiers() throws Exception
	{
		Path folder = Files.createTempDirectory("all-drop-sounds-variants");
		Path first = Files.createFile(folder.resolve("Twisted bow.wav"));
		Path second = Files.createFile(folder.resolve("Twisted bow 2.wav"));
		Path clues = Files.createFile(folder.resolve("Clue scroll.wav"));

		SoundLibrary library = new SoundLibrary();
		library.setFolder(folder);
		Assert.assertEquals(first, library.find("Twisted bow"));
		Assert.assertEquals(second, library.find("Twisted bow"));
		Assert.assertEquals(first, library.find("Twisted bow"));
		Assert.assertEquals(clues, library.find("Clue scroll (easy)"));
		Assert.assertEquals(clues, library.find("Clue scroll (master)"));
		Assert.assertTrue(SoundLibrary.wildcard("*dragon*", "Dragon claws"));
		Assert.assertFalse(SoundLibrary.wildcard("*dragon*", "Rune platebody"));
		Assert.assertNull(library.find("Rune platebody"));
	}

	@Test
	public void rarerSoundsPassTheGateAndCheaperOnesWait()
	{
		Assert.assertTrue(SoundGate.allow(SoundPriority.rank(ValueTier.INSANE, false, false, false), SoundPriority.rank(ValueTier.LOW, true, false, false), 10, 400));
		Assert.assertFalse(SoundGate.allow(1, 1, 10, 400));
		Assert.assertTrue(SoundGate.allow(1, 1, 400, 400));
		Assert.assertTrue(SoundGate.allow(1, 1, 0, 0));
		Assert.assertTrue(SoundPriority.rank(null, false, false, true) > SoundPriority.rank(ValueTier.INSANE_PLUS_PLUS, false, true, false));
		Assert.assertTrue(SoundPriority.rank(ValueTier.LOW, true, false, false) < SoundPriority.rank(null, false, true, false));
	}

	@Test
	public void gameSlidersMuteAndScale()
	{
		Assert.assertEquals(0, GameAudio.outputPercent(0, 127));
		Assert.assertEquals(0, GameAudio.outputPercent(127, 0));
		Assert.assertEquals(100, GameAudio.outputPercent(127, 127));
		Assert.assertEquals(100, GameAudio.outputPercent(4, 4));
		Assert.assertEquals(50, GameAudio.applyOutput(100, 50));
		Assert.assertEquals(0, GameAudio.applyOutput(80, 0));
	}

	@Test
	public void highlightedListsComeFromOtherPlugins()
	{
		Assert.assertTrue(HighlightedLists.isHighlightKey("grounditems.highlightedItems"));
		Assert.assertTrue(HighlightedLists.isHighlightKey("someplugin.highlightedItem"));
		Assert.assertFalse(HighlightedLists.isHighlightKey("grounditems.highlightedColor"));
		Assert.assertFalse(HighlightedLists.isHighlightKey("grounditems.highlightedVolume"));
		Assert.assertFalse(HighlightedLists.isItemList("#ff9040"));
		Assert.assertFalse(HighlightedLists.isItemList("true"));
		Assert.assertFalse(HighlightedLists.isItemList("12"));

		Assert.assertTrue(HighlightedLists.matches(Collections.singletonList("Dragon bones, *clue*"), "Clue scroll (easy)", 1));
		Assert.assertTrue(HighlightedLists.matches(Collections.singletonList("Coins>100"), "Coins", 200));
		Assert.assertFalse(HighlightedLists.matches(Collections.singletonList("Coins>100"), "Coins", 50));
		Assert.assertFalse(HighlightedLists.matches(Collections.singletonList("Coins<10"), "Coins", 50));
		Assert.assertTrue(HighlightedLists.matches(Arrays.asList("ff0000", "Twisted bow"), "Twisted bow", 1));
	}

	@Test
	public void uniquesArePetsJarsAndNamedUntradeables()
	{
		Assert.assertTrue(UniqueItems.isUnique("Pet chaos elemental", false, 0, 0));
		Assert.assertTrue(UniqueItems.isUnique("Baby mole", false, 0, 0));
		Assert.assertTrue(UniqueItems.isUnique("Jar of souls", false, 0, 0));
		Assert.assertTrue(UniqueItems.isUnique("Champion's scroll", false, 0, 0));
		Assert.assertFalse(UniqueItems.isUnique("Twisted bow", true, 1_000_000_000L, 1));
		Assert.assertFalse(UniqueItems.isUnique("Bones", true, 100, 1));
		Assert.assertFalse(UniqueItems.isUnique("Burnt shrimp", false, 0, 0));
		Assert.assertTrue(UniqueItems.isUnique("Some unique token", false, 0, 0));
		Assert.assertFalse(UniqueItems.isUnique("Some unique token", true, 50, 1));
	}

	@Test
	public void selfDropsWaitForTheInventoryEvent()
	{
		GroundSpawnQueue queue = new GroundSpawnQueue();
		queue.add(995, 1, TileItem.OWNERSHIP_NONE, 10);
		Assert.assertTrue(queue.drainReady(11).isEmpty());
		Assert.assertEquals(1, queue.drainReady(12).size());
		Assert.assertTrue(queue.drainReady(13).isEmpty());
	}
}
