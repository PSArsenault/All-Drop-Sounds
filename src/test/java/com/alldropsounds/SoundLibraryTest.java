package com.alldropsounds;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Assert;
import org.junit.Test;

public class SoundLibraryTest
{
	@Test
	public void matchesItemNameIgnoringCase() throws Exception
	{
		Path folder = Files.createTempDirectory("all-drop-sounds");
		Files.createFile(folder.resolve("Twisted bow.wav"));
		Files.createFile(folder.resolve("Enhanced crystal weapon seed.WAV"));
		Files.createFile(folder.resolve("notes.txt"));

		SoundLibrary library = new SoundLibrary();
		library.setFolder(TestFiles.of(folder));

		Assert.assertEquals(TestFiles.of(folder.resolve("Twisted bow.wav")), library.find("Twisted bow"));
		Assert.assertEquals(TestFiles.of(folder.resolve("Twisted bow.wav")), library.find("twisted bow"));
		Assert.assertEquals(TestFiles.of(folder.resolve("Enhanced crystal weapon seed.WAV")), library.find("Enhanced crystal weapon seed"));
		Assert.assertNull(library.find("Coins"));
		Assert.assertNull(library.find("null"));
		Assert.assertNull(library.find("  "));
		Assert.assertEquals(2, library.size());
	}

	@Test
	public void itemFileOverridesTierFile() throws Exception
	{
		Path folder = Files.createTempDirectory("all-drop-sounds-tiers");
		Files.createFile(folder.resolve("Low.wav"));
		Files.createFile(folder.resolve("Insane++.wav"));
		Files.createFile(folder.resolve("Twisted bow.wav"));

		SoundLibrary library = new SoundLibrary();
		library.setFolder(TestFiles.of(folder));

		Assert.assertEquals(TestFiles.of(folder.resolve("Twisted bow.wav")), library.findDropSound("Twisted bow", ValueTier.LOW));
		Assert.assertEquals(TestFiles.of(folder.resolve("Low.wav")), library.findDropSound("Rune platebody", ValueTier.LOW));
		Assert.assertEquals(TestFiles.of(folder.resolve("Insane++.wav")), library.findDropSound("Scythe of vitur", ValueTier.INSANE_PLUS_PLUS));
		Assert.assertNull(library.findDropSound("Bones", null));
	}

	@Test
	public void unchargedDropUsesTheBaseWav() throws Exception
	{
		Path folder = Files.createTempDirectory("all-drop-sounds-uncharged");
		Files.createFile(folder.resolve("Obliterator.wav"));
		Files.createFile(folder.resolve("Obliterator 2.wav"));
		Files.createFile(folder.resolve("Voidwaker (uncharged).wav"));
		Files.createFile(folder.resolve("%uncharged%.wav"));

		SoundLibrary library = new SoundLibrary();
		library.setFolder(TestFiles.of(folder));

		Assert.assertEquals(TestFiles.of(folder.resolve("Obliterator.wav")), library.find("Obliterator (uncharged)"));
		Assert.assertEquals(TestFiles.of(folder.resolve("Obliterator 2.wav")), library.find("Obliterator (Uncharged)"));
		Assert.assertEquals(TestFiles.of(folder.resolve("Obliterator.wav")), library.find("Obliterator"));
		Assert.assertEquals(TestFiles.of(folder.resolve("Voidwaker (uncharged).wav")), library.find("Voidwaker (uncharged)"));
		Assert.assertEquals(TestFiles.of(folder.resolve("%uncharged%.wav")), library.find("Something (uncharged)"));
		Assert.assertNull(library.find("Clue scroll (easy)"));
	}

	@Test
	public void volumeExtremes()
	{
		Assert.assertEquals(0f, DropSoundPlayer.decibels(100), 0.001f);
		Assert.assertEquals(-80f, DropSoundPlayer.decibels(0), 0.001f);
		Assert.assertTrue(DropSoundPlayer.decibels(50) < 0f);
	}
}
