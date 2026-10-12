package com.alldropsounds;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import net.runelite.api.TileItem;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import org.junit.Assert;
import org.junit.Test;

public class EdgeCasesTest
{
	private static final int LOW = 20_000;
	private static final int MEDIUM = 100_000;
	private static final int HIGH = 1_000_000;
	private static final int INSANE = 10_000_000;
	private static final int INSANE_PLUS = 50_000_000;
	private static final int INSANE_PLUS_PLUS = 250_000_000;

	@Test
	public void priceEdgesDoNotCrashOrGoNegative()
	{
		Assert.assertEquals(0L, PriceSource.unitPrice(-50, -1, PriceSource.HIGHEST));
		Assert.assertEquals(0L, PriceSource.unitPrice(-50, 10, PriceSource.GRAND_EXCHANGE));
		Assert.assertEquals(10L, PriceSource.unitPrice(-50, 10, PriceSource.HIGH_ALCHEMY));
		Assert.assertEquals(10L, PriceSource.unitPrice(-50, 10, null));
		Assert.assertEquals(0L, PriceSource.stackValue(100, 0));
		Assert.assertEquals(0L, PriceSource.stackValue(100, -5));
		Assert.assertEquals(0L, PriceSource.stackValue(0, Integer.MAX_VALUE));
		Assert.assertEquals(Long.MAX_VALUE, PriceSource.stackValue(Long.MAX_VALUE, 2));
		Assert.assertTrue(PriceSource.stackValue(Integer.MAX_VALUE, Integer.MAX_VALUE) > 0);
	}

	@Test
	public void tierBoundariesAndDisabledOrTiedThresholds()
	{
		Assert.assertNull(ValueTiers.match(0, LOW, MEDIUM, HIGH, INSANE, INSANE_PLUS, INSANE_PLUS_PLUS));
		Assert.assertNull(ValueTiers.match(-1, LOW, MEDIUM, HIGH, INSANE, INSANE_PLUS, INSANE_PLUS_PLUS));
		Assert.assertNull(ValueTiers.match(Long.MAX_VALUE, 0, 0, 0, 0, 0, 0));
		Assert.assertNull(ValueTiers.match(1_000, -1, -5, 0, -10, 0, 0));
		Assert.assertEquals(ValueTier.LOW, ValueTiers.match(99_999, LOW, MEDIUM, HIGH, INSANE, INSANE_PLUS, INSANE_PLUS_PLUS));
		Assert.assertEquals(ValueTier.MEDIUM, ValueTiers.match(999_999, LOW, MEDIUM, HIGH, INSANE, INSANE_PLUS, INSANE_PLUS_PLUS));
		Assert.assertEquals(ValueTier.INSANE, ValueTiers.match(49_999_999, LOW, MEDIUM, HIGH, INSANE, INSANE_PLUS, INSANE_PLUS_PLUS));
		Assert.assertEquals(ValueTier.INSANE_PLUS, ValueTiers.match(249_999_999, LOW, MEDIUM, HIGH, INSANE, INSANE_PLUS, INSANE_PLUS_PLUS));
		Assert.assertEquals(ValueTier.MEDIUM, ValueTiers.match(100, 100, 100, 0, 0, 0, 0));
		Assert.assertNull(ValueTiers.match(50, 100, 20_000, HIGH, INSANE, INSANE_PLUS, INSANE_PLUS_PLUS));
		Assert.assertEquals(ValueTier.LOW, tier(PriceSource.stackValue(1, 20_000)));
		Assert.assertNull(tier(PriceSource.stackValue(1, 19_999)));
		Assert.assertEquals(ValueTier.INSANE_PLUS_PLUS, tier(PriceSource.stackValue(50_000_000, 5)));
	}

	@Test
	public void inventoryDiffSurvivesNullsAndMixedChanges()
	{
		Assert.assertTrue(InventoryDiff.increased(null, null).isEmpty());
		Assert.assertTrue(InventoryDiff.gainedAmounts(null, null).isEmpty());
		Assert.assertTrue(InventoryDiff.decreased(null, null).isEmpty());
		Assert.assertTrue(InventoryDiff.gainedAmounts(counts(1, 1), counts(1, 1)).isEmpty());

		Map<Integer, Integer> before = new HashMap<Integer, Integer>();
		before.put(1, null);
		before.put(2, 5);
		before.put(-3, 9);
		Map<Integer, Integer> after = new HashMap<Integer, Integer>();
		after.put(1, 2);
		after.put(2, null);
		after.put(4, 7);
		after.put(0, 3);

		Assert.assertEquals(counts(1, 2, 4, 7), InventoryDiff.gainedAmounts(before, after));
		Assert.assertEquals(Collections.singletonList(2), InventoryDiff.decreased(before, after));
		Assert.assertTrue(InventoryDiff.decreased(counts(-1, 5, 0, 5), Collections.emptyMap()).isEmpty());
	}

	@Test
	public void chestWatchCoversTimingLossesAndDuplicateMessages()
	{
		ChestLootWatch watch = new ChestLootWatch();
		watch.reset(null);
		Assert.assertFalse(watch.isAwaiting());
		Assert.assertTrue(watch.onInventoryChange(null).isEmpty());
		Assert.assertTrue(watch.getLastLost().isEmpty());

		watch.reset(counts(385, 2, 995, 100));
		watch.onChestMessage();
		Assert.assertTrue(watch.onInventoryChange(counts(995, 50)).isEmpty());
		Assert.assertEquals(new HashSet<Integer>(Arrays.asList(385, 995)), new HashSet<Integer>(watch.getLastLost()));
		Assert.assertTrue(watch.isAwaiting());
		Assert.assertEquals(counts(4151, 1), watch.onInventoryChange(counts(995, 50, 4151, 1)));
		Assert.assertFalse(watch.isAwaiting());

		Assert.assertTrue(watch.onChestMessage().isEmpty());
		Assert.assertFalse(watch.isAwaiting());

		watch.onGameTick();
		watch.onChestMessage();
		Assert.assertEquals(counts(566, 40, 560, 1), watch.onInventoryChange(counts(995, 50, 4151, 1, 566, 40, 560, 1)));

		ChestLootWatch partial = new ChestLootWatch();
		partial.reset(counts(566, 10));
		partial.onChestMessage();
		Assert.assertEquals(counts(566, 15), partial.onInventoryChange(counts(566, 25)));

		ChestLootWatch sameTick = new ChestLootWatch();
		sameTick.reset(Collections.emptyMap());
		sameTick.onGameTick();
		Assert.assertTrue(sameTick.onChestMessage().isEmpty());
		sameTick.onGameTick();
		Assert.assertTrue(sameTick.isAwaiting());
		for (int i = 0; i < ChestLootWatch.WATCH_TICKS - 1; i++)
		{
			sameTick.onGameTick();
		}
		Assert.assertTrue(sameTick.isAwaiting());
		sameTick.onGameTick();
		Assert.assertFalse(sameTick.isAwaiting());
		Assert.assertTrue(sameTick.onInventoryChange(counts(995, 1)).isEmpty());
	}

	@Test
	public void chestMessagesRejectNearMissesAndAcceptTaggedLines()
	{
		Assert.assertTrue(ChestLootMessages.isChestLoot("<col=0000ff>You find some treasure in the chest!</col>"));
		Assert.assertTrue(ChestLootMessages.isChestLoot("  You steal some loot from the chest.  "));
		Assert.assertTrue(ChestLootMessages.isChestLoot("You have opened Larran's big chest."));
		Assert.assertTrue(ChestLootMessages.isChestLoot("You have opened Larran's small chest."));
		Assert.assertTrue(ChestLootMessages.isChestLoot("You find a rusty sword inside."));
		Assert.assertTrue(ChestLootMessages.isChestLoot("You open the chest and find"));
		Assert.assertTrue(ChestLootMessages.isChestLoot("Your black key breaks in the lock and something else."));

		Assert.assertFalse(ChestLootMessages.isChestLoot("you find some treasure in the chest!"));
		Assert.assertFalse(ChestLootMessages.isChestLoot("You find some treasure in the chest"));
		Assert.assertTrue(ChestLootMessages.isChestLoot("You find some treasure in the chest! "));
		Assert.assertFalse(ChestLootMessages.isChestLoot("You have opened Larran's medium chest."));
		Assert.assertFalse(ChestLootMessages.isChestLoot("You have opened Larran's big chesty thing."));
		Assert.assertFalse(ChestLootMessages.isChestLoot("Your Bronze key breaks in the lock."));
		Assert.assertFalse(ChestLootMessages.isChestLoot("You find A coin inside."));
		Assert.assertFalse(ChestLootMessages.isChestLoot("You open the chest."));
		Assert.assertFalse(ChestLootMessages.isChestLoot("<col=ff0000>Valuable drop: Twisted bow</col>"));
	}

	@Test
	public void soundFilesHandleNamesTiersAndAMissingFolder() throws Exception
	{
		Path root = Files.createTempDirectory("all-drop-sounds-edge");
		Path sounds = root.resolve("sounds");
		Files.createDirectories(sounds);
		Files.createFile(sounds.resolve("Insane+.wav"));
		Files.createFile(sounds.resolve("Insane++.wav"));
		Files.createFile(sounds.resolve(" .wav"));
		Files.createFile(sounds.resolve(".wav"));
		Files.createFile(sounds.resolve("Dharok's helm.wav"));
		Files.createDirectories(sounds.resolve("nested.wav"));
		Files.write(sounds.resolve("notes.txt"), new byte[] {1});

		SoundLibrary library = new SoundLibrary();
		library.setFolder(null);
		Assert.assertEquals(0, library.size());
		Assert.assertNull(library.find("Insane+"));
		Assert.assertNull(library.find(null));

		library.setFolder(TestFiles.of(sounds.resolve("missing")));
		Assert.assertNull(library.find("Insane+"));

		library.setFolder(TestFiles.of(sounds.resolve("notes.txt")));
		Assert.assertEquals(0, library.size());

		library.setFolder(TestFiles.of(sounds));
		Assert.assertEquals(TestFiles.of(sounds.resolve("Insane+.wav")), library.find("insane+"));
		Assert.assertEquals(TestFiles.of(sounds.resolve("Insane++.wav")), library.find("INSANE++"));
		Assert.assertEquals(TestFiles.of(sounds.resolve("Dharok's helm.wav")), library.find("Dharok's helm"));
		Assert.assertNull(library.find(""));
		Assert.assertNull(library.find("null"));
		Assert.assertNull(library.findDropSound("Bones", ValueTier.HIGH));
		Assert.assertEquals(TestFiles.of(sounds.resolve("Insane+.wav")), library.findDropSound("Low", ValueTier.INSANE_PLUS));

		Path added = sounds.resolve("Twisted bow.wav");
		Files.createFile(added);
		Assert.assertTrue(sounds.toFile().setLastModified(System.currentTimeMillis() + 10_000L));
		Assert.assertEquals(TestFiles.of(added), library.find("Twisted bow"));

		Files.delete(added);
		Assert.assertTrue(sounds.toFile().setLastModified(System.currentTimeMillis() + 20_000L));
		Assert.assertNull(library.find("Twisted bow"));

		Path other = root.resolve("other");
		Files.createDirectories(other);
		Files.createFile(other.resolve("Coins.wav"));
		library.setFolder(TestFiles.of(other));
		Assert.assertNull(library.find("Insane+"));
		Assert.assertEquals(TestFiles.of(other.resolve("Coins.wav")), library.find("Coins"));
	}

	@Test
	public void itemWavUsesItemVolumeAndTierWavUsesTierVolume() throws Exception
	{
		Path folder = Files.createTempDirectory("all-drop-sounds-plan");
		Path item = Files.createFile(folder.resolve("Twisted bow.wav"));
		Path low = Files.createFile(folder.resolve("Low.wav"));

		DropSoundPlan specific = DropSoundPlan.choose(TestFiles.of(item), TestFiles.of(low), 40, 90);
		Assert.assertEquals(TestFiles.of(item), specific.file);
		Assert.assertEquals(40, specific.volume);

		DropSoundPlan tier = DropSoundPlan.choose(null, TestFiles.of(low), 40, 90);
		Assert.assertEquals(TestFiles.of(low), tier.file);
		Assert.assertEquals(90, tier.volume);

		Assert.assertNull(DropSoundPlan.choose(TestFiles.of(item), TestFiles.of(low), 0, 90));
		Assert.assertNull(DropSoundPlan.choose(null, TestFiles.of(low), 100, -1));
		Assert.assertNull(DropSoundPlan.choose(null, null, 100, 100));
		Assert.assertEquals(100, DropSoundPlan.choose(null, TestFiles.of(low), 1, 250).volume);

		Path highlighted = Files.createFile(folder.resolve("highlighted.wav"));
		DropSoundPlan highlightedPlan = DropSoundPlan.choose(null, TestFiles.of(highlighted), TestFiles.of(low), 40, 70, 90);
		Assert.assertEquals(TestFiles.of(highlighted), highlightedPlan.file);
		Assert.assertEquals(70, highlightedPlan.volume);

		DropSoundPlan itemWins = DropSoundPlan.choose(TestFiles.of(item), TestFiles.of(highlighted), TestFiles.of(low), 40, 70, 90);
		Assert.assertEquals(TestFiles.of(item), itemWins.file);
		Assert.assertEquals(40, itemWins.volume);

		Assert.assertNull(DropSoundPlan.choose(null, TestFiles.of(highlighted), TestFiles.of(low), 40, 0, 90));
		Assert.assertEquals(TestFiles.of(low), DropSoundPlan.choose(null, null, TestFiles.of(low), 40, 70, 90).file);
		Assert.assertEquals(100, DropSoundPlan.choose(null, TestFiles.of(highlighted), null, 1, 250, 90).volume);
		Assert.assertEquals(50, DropSoundPlan.applyMaster(100, 50));
		Assert.assertEquals(0, DropSoundPlan.applyMaster(80, 0));
		Assert.assertEquals(25, DropSoundPlan.applyMaster(50, 50));
	}

	@Test
	public void groundSpawnFiltersAndDropMemory()
	{
		Assert.assertTrue(GroundDropRules.isYourDrop(TileItem.OWNERSHIP_SELF));
		Assert.assertTrue(GroundDropRules.isYourDrop(TileItem.OWNERSHIP_GROUP));
		Assert.assertFalse(GroundDropRules.isYourDrop(TileItem.OWNERSHIP_OTHER));
		Assert.assertFalse(GroundDropRules.isYourDrop(TileItem.OWNERSHIP_NONE));
		Assert.assertFalse(GroundDropRules.isYourDrop(99));
		Assert.assertFalse(GroundDropRules.isYourDrop(-1));

		Assert.assertTrue(GroundDropRules.spawnBlocked(5, 6));
		Assert.assertFalse(GroundDropRules.spawnBlocked(6, 6));
		Assert.assertTrue(GroundDropRules.spawnBlocked(0, Integer.MAX_VALUE));

		Assert.assertTrue(GroundDropRules.shouldPlay(true, true, TileItem.OWNERSHIP_NONE, true));
		Assert.assertFalse(GroundDropRules.shouldPlay(false, false, TileItem.OWNERSHIP_SELF, true));
		Assert.assertFalse(GroundDropRules.shouldPlay(true, true, TileItem.OWNERSHIP_NONE, false));
		Assert.assertTrue(GroundDropRules.shouldPlay(true, true, TileItem.OWNERSHIP_SELF, false));
		Assert.assertTrue(GroundDropRules.shouldPlay(false, false, TileItem.OWNERSHIP_OTHER, false));
		Assert.assertTrue(GroundDropRules.isManualDrop("Drop", true));
		Assert.assertTrue(GroundDropRules.isManualDrop("drop", true));
		Assert.assertFalse(GroundDropRules.isManualDrop("Drop", false));
		Assert.assertFalse(GroundDropRules.isManualDrop("Take", true));
		Assert.assertFalse(GroundDropRules.isManualDrop(null, true));
		Assert.assertTrue(GroundDropRules.isRememberedDrop(11, 10));

		Assert.assertFalse(GroundDropRules.wasJustDropped(null, 10));
		Assert.assertTrue(GroundDropRules.wasJustDropped(10, 10));
		Assert.assertTrue(GroundDropRules.wasJustDropped(10, 11));
		Assert.assertFalse(GroundDropRules.wasJustDropped(10, 12));
		Assert.assertFalse(GroundDropRules.dropExpired(10, 12));
		Assert.assertFalse(GroundDropRules.dropExpired(10, 13));
		Assert.assertTrue(GroundDropRules.dropExpired(10, 15));
	}

	@Test
	public void itemNamesIgnoreTagsBlankAndNullPlaceholder()
	{
		Assert.assertNull(ItemNames.clean(null));
		Assert.assertNull(ItemNames.clean(""));
		Assert.assertNull(ItemNames.clean("   "));
		Assert.assertNull(ItemNames.clean("null"));
		Assert.assertNull(ItemNames.clean("<col=ff0000>NULL</col>"));
		Assert.assertEquals("Twisted bow", ItemNames.clean("  <col=ff9040>Twisted bow</col>  "));
		Assert.assertEquals("Dharok's helm", ItemNames.clean("Dharok's helm"));
	}

	@Test
	public void rewardInterfacesMapToTheirContainers()
	{
		Assert.assertEquals(Integer.valueOf(InventoryID.TRAIL_REWARDINV), RewardChests.rewardContainer(InterfaceID.BARROWS_REWARD));
		Assert.assertEquals(Integer.valueOf(InventoryID.RAIDS_REWARDS), RewardChests.rewardContainer(InterfaceID.RAIDS_REWARDS));
		Assert.assertEquals(Integer.valueOf(InventoryID.TOB_CHESTS), RewardChests.rewardContainer(InterfaceID.TOB_CHESTS));
		Assert.assertEquals(Integer.valueOf(InventoryID.TOA_CHESTS), RewardChests.rewardContainer(InterfaceID.TOA_CHESTS));
		Assert.assertEquals(Integer.valueOf(InventoryID.PMOON_REWARDINV), RewardChests.rewardContainer(InterfaceID.PMOON_REWARD));
		Assert.assertEquals(Integer.valueOf(InventoryID.COLOSSEUM_REWARDS), RewardChests.rewardContainer(InterfaceID.COLOSSEUM_REWARD_CHEST_2));
		Assert.assertNull(RewardChests.rewardContainer(InterfaceID.WILDY_LOOT_CHEST));
		Assert.assertTrue(RewardChests.isRewardChest(InterfaceID.WILDY_LOOT_CHEST));
		Assert.assertTrue(RewardChests.isRewardChest(InterfaceID.BARROWS_REWARD));
		Assert.assertFalse(RewardChests.isRewardChest(-1));
		Assert.assertFalse(RewardChests.isRewardChest(0));
		Assert.assertTrue(RewardChests.inTheatreOfBlood(RewardChests.THEATRE_OF_BLOOD_REGION));
		Assert.assertTrue(RewardChests.inTheatreOfBlood(RewardChests.THEATRE_OF_BLOOD_LOBBY));
		Assert.assertFalse(RewardChests.inTheatreOfBlood(RewardChests.THEATRE_OF_BLOOD_REGION + 1));
	}

	@Test
	public void playerRejectsBadInputAndSurvivesABadFile() throws Exception
	{
		Path folder = Files.createTempDirectory("all-drop-sounds-player");
		Path garbage = folder.resolve("bad.wav");
		Files.write(garbage, new byte[] {0, 1, 2, 3, 4});

		DropSoundPlayer player = new DropSoundPlayer();
		player.play(null, 100);
		player.play(TestFiles.of(garbage), 0);
		player.play(TestFiles.of(garbage), -10);
		player.play(TestFiles.of(folder.resolve("missing.wav")), 100);
		player.play(TestFiles.of(garbage), 50);
		player.shutdown();
		player.shutdown();
		player.play(TestFiles.of(garbage), 100);

		Assert.assertEquals(0f, DropSoundPlayer.decibels(100), 0.001f);
		Assert.assertEquals(0f, DropSoundPlayer.decibels(101), 0.001f);
		Assert.assertEquals(0f, DropSoundPlayer.decibels(Integer.MAX_VALUE), 0.001f);
		Assert.assertEquals(-80f, DropSoundPlayer.decibels(0), 0.001f);
		Assert.assertEquals(-80f, DropSoundPlayer.decibels(-1), 0.001f);
		Assert.assertEquals(-80f, DropSoundPlayer.decibels(Integer.MIN_VALUE), 0.001f);
		Assert.assertEquals((float) (20.0 * Math.log10(0.5)), DropSoundPlayer.decibels(50), 0.001f);
		Assert.assertTrue(Float.isFinite(DropSoundPlayer.decibels(1)));
	}

	@Test
	public void quantitiesAndTickMathDoNotOverflow()
	{
		Assert.assertEquals(3, InventoryDiff.addQuantity(1, 2));
		Assert.assertEquals(Integer.MAX_VALUE, InventoryDiff.addQuantity(Integer.MAX_VALUE, 1));
		Assert.assertEquals(Integer.MAX_VALUE, InventoryDiff.addQuantity(Integer.MAX_VALUE, Integer.MAX_VALUE));

		Map<Integer, Integer> before = new HashMap<Integer, Integer>();
		before.put(1, Integer.MIN_VALUE);
		Assert.assertEquals(Integer.valueOf(Integer.MAX_VALUE), InventoryDiff.gainedAmounts(before, counts(1, 1)).get(1));
		Assert.assertEquals(Collections.singletonList(1), InventoryDiff.decreased(counts(1, Integer.MAX_VALUE), counts(1, Integer.MIN_VALUE)));

		Assert.assertEquals(0L, PriceSource.unitPrice(Long.MIN_VALUE, Long.MIN_VALUE, PriceSource.HIGHEST));
		Assert.assertEquals(0L, PriceSource.stackValue(1, Integer.MIN_VALUE));
		Assert.assertEquals(Long.MAX_VALUE, PriceSource.stackValue(Long.MAX_VALUE, 1));

		Assert.assertEquals(ValueTier.LOW, ValueTiers.match(1_000_000, 1_000_000, 100, 0, 0, 0, 0));
		Assert.assertEquals(ValueTier.MEDIUM, ValueTiers.match(500, 1_000_000, 100, 0, 0, 0, 0));
		Assert.assertEquals(ValueTier.INSANE_PLUS_PLUS, ValueTiers.match(Long.MAX_VALUE, 1, 1, 1, 1, 1, 1));

		Assert.assertFalse(GroundDropRules.wasJustDropped(10, 9));
		Assert.assertFalse(GroundDropRules.dropExpired(10, 9));
		Assert.assertTrue(GroundDropRules.wasJustDropped(Integer.MAX_VALUE, Integer.MAX_VALUE));
		Assert.assertFalse(GroundDropRules.dropExpired(Integer.MAX_VALUE, Integer.MAX_VALUE));
		Assert.assertFalse(GroundDropRules.wasJustDropped(Integer.MIN_VALUE, Integer.MAX_VALUE));
		Assert.assertTrue(GroundDropRules.dropExpired(Integer.MIN_VALUE, Integer.MAX_VALUE));
		Assert.assertFalse(GroundDropRules.spawnBlocked(Integer.MAX_VALUE, Integer.MAX_VALUE));
		Assert.assertTrue(GroundDropRules.spawnBlocked(Integer.MIN_VALUE, Integer.MAX_VALUE));
	}

	@Test
	public void chestWatchKeepsSameTickLootAndDropsItAfterALaterTick()
	{
		ChestLootWatch sameTick = new ChestLootWatch();
		sameTick.reset(Collections.emptyMap());
		sameTick.onInventoryChange(counts(4151, 1, 995, 50));
		Assert.assertEquals(counts(4151, 1, 995, 50), sameTick.onChestMessage());
		Assert.assertTrue(sameTick.onChestMessage().isEmpty());
		Assert.assertFalse(sameTick.isAwaiting());

		ChestLootWatch afterTick = new ChestLootWatch();
		afterTick.reset(Collections.emptyMap());
		afterTick.onGameTick();
		afterTick.onInventoryChange(counts(4151, 1));
		afterTick.onGameTick();
		Assert.assertTrue(afterTick.onChestMessage().isEmpty());
		Assert.assertTrue(afterTick.isAwaiting());
		Assert.assertEquals(counts(995, 20), afterTick.onInventoryChange(counts(4151, 1, 995, 20)));

		ChestLootWatch zero = new ChestLootWatch();
		zero.reset(counts(995, 5));
		zero.onChestMessage();
		Assert.assertTrue(zero.onInventoryChange(counts(995, 5)).isEmpty());
		Assert.assertTrue(zero.isAwaiting());

		Assert.assertFalse(ChestLootMessages.isChestLoot(null));
		Assert.assertFalse(ChestLootMessages.isChestLoot(""));
		Assert.assertFalse(ChestLootMessages.isChestLoot("<col=ff0000></col>"));
		Assert.assertTrue(ChestLootMessages.isChestLoot("You have opened Larran\u2019s big chest."));
		Assert.assertFalse(ChestLootMessages.isChestLoot("You find treasure and supplies within the chest"));
		Assert.assertEquals("null item", ItemNames.clean("null item"));
		Assert.assertEquals("Coins", ItemNames.clean(" <img=1>Coins "));
	}

	@Test
	public void soundNamesAreCaseInsensitiveAndIgnorePadding() throws Exception
	{
		Path folder = Files.createTempDirectory("all-drop-sounds-names");
		Files.createFile(folder.resolve("COINS.WAV"));
		Files.createFile(folder.resolve("Twisted bow .wav"));
		Files.createFile(folder.resolve("Low.wav.bak"));
		Files.createFile(folder.resolve("notes.WAV.txt"));

		SoundLibrary library = new SoundLibrary();
		library.setFolder(TestFiles.of(folder));
		Assert.assertEquals(2, library.size());
		Assert.assertNotNull(library.find("coins"));
		Assert.assertEquals(TestFiles.of(folder.resolve("Twisted bow .wav")), library.find("  Twisted bow  "));
		Assert.assertNull(library.find("Low.wav"));
		Assert.assertNull(library.find("notes.WAV"));
		Assert.assertNull(library.findDropSound(null, null));
		Assert.assertNull(library.findDropSound("   ", ValueTier.LOW));
	}

	private static ValueTier tier(long value)
	{
		return ValueTiers.match(value, LOW, MEDIUM, HIGH, INSANE, INSANE_PLUS, INSANE_PLUS_PLUS);
	}

	private static Map<Integer, Integer> counts(int... idThenQuantity)
	{
		Map<Integer, Integer> counts = new HashMap<Integer, Integer>();
		for (int i = 0; i < idThenQuantity.length; i += 2)
		{
			counts.put(idThenQuantity[i], idThenQuantity[i + 1]);
		}
		return counts;
	}
}
