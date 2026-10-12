package com.alldropsounds;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
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
		Path cluePattern = Files.createFile(folder.resolve("Clue scroll%.wav"));
		Path skotos = Files.createFile(folder.resolve("skotos.wav"));
		Path tiny = Files.createFile(folder.resolve("tiny tempor.wav"));
		Path pets = Files.createFile(folder.resolve("pets.wav"));

		SoundLibrary library = new SoundLibrary();
		library.setFolder(TestFiles.of(folder));
		Assert.assertEquals(TestFiles.of(first), library.find("Twisted bow"));
		Assert.assertEquals(TestFiles.of(second), library.find("Twisted bow"));
		Assert.assertEquals(TestFiles.of(first), library.find("Twisted bow"));
		Assert.assertEquals(TestFiles.of(cluePattern), library.find("Clue scroll (easy)"));
		Assert.assertEquals(TestFiles.of(clues), library.find("Clue scroll"));
		Assert.assertEquals(TestFiles.of(cluePattern), library.find("Clue scroll (master)"));
		Assert.assertEquals(TestFiles.of(skotos), library.find("Skotos"));
		Assert.assertEquals(TestFiles.of(tiny), library.find("Tiny tempor"));
		Assert.assertEquals(TestFiles.of(pets), library.findExact("pets"));
		Assert.assertNull(library.find("Pet chaos elemental"));
		Assert.assertTrue(SoundLibrary.wildcard("%dragon%", "Dragon claws"));
		Assert.assertFalse(SoundLibrary.wildcard("%dragon%", "Rune platebody"));
		Assert.assertNull(library.find("Rune platebody"));
	}

	@Test
	public void rarerSoundsOutrankCheaperOnes()
	{
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
		Assert.assertEquals("pets", UniqueItems.categoryStem("Skotos"));
		Assert.assertEquals("pets", UniqueItems.categoryStem("Tiny tempor"));
		Assert.assertEquals("pets", UniqueItems.categoryStem("Pet chaos elemental"));
		Assert.assertEquals("jars", UniqueItems.categoryStem("Jar of souls"));
		Assert.assertEquals("champions", UniqueItems.categoryStem("Champion's scroll"));
		Assert.assertNull(UniqueItems.categoryStem("Some unique token"));
		Assert.assertTrue(UniqueItems.isUnique("Baby mole", false, 0, 0));
		Assert.assertTrue(UniqueItems.isUnique("Jar of souls", false, 0, 0));
		Assert.assertTrue(UniqueItems.isUnique("Champion's scroll", false, 0, 0));
		Assert.assertFalse(UniqueItems.isUnique("Twisted bow", true, 1_000_000_000L, 1));
		Assert.assertFalse(UniqueItems.isUnique("Bones", true, 100, 1));
		Assert.assertFalse(UniqueItems.isUnique("Burnt shrimp", false, 0, 0));
		Assert.assertTrue(UniqueItems.isUnique("Some unique token", false, 0, 0));
		Assert.assertFalse(UniqueItems.isUnique("Some unique token", true, 50, 1));
	}
}
