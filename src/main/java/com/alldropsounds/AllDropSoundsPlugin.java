package com.alldropsounds;

import com.google.inject.Provides;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.TileItem;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.RuneLite;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PluginDescriptor(
	name = "All Drop Sounds",
	description = "Plays a wav named after an item when it drops or comes from a chest",
	tags = {"sound", "drops", "loot", "chest"}
)
public class AllDropSoundsPlugin extends Plugin
{
	private static final Logger log = LoggerFactory.getLogger(AllDropSoundsPlugin.class);
	private static final int THEATRE_OF_BLOOD_REGION = 12867;
	private static final int THEATRE_OF_BLOOD_LOBBY = 14642;
	private static final int[] PVP_LOOT_CONTAINERS = {
		InventoryID.DEADMAN_LOOT_INV0,
		InventoryID.DEADMAN_LOOT_INV1,
		InventoryID.DEADMAN_LOOT_INV2,
		InventoryID.DEADMAN_LOOT_INV3,
		InventoryID.DEADMAN_LOOT_INV4
	};

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ItemManager itemManager;

	@Inject
	private AllDropSoundsConfig config;

	private final SoundLibrary soundLibrary = new SoundLibrary();
	private final DropSoundPlayer player = new DropSoundPlayer();
	private final ChestLootWatch chestLootWatch = new ChestLootWatch();
	private final Map<Integer, Integer> droppedAtTick = new HashMap<>();
	private final Set<Integer> playedRewardGroups = new HashSet<>();
	private final Set<String> playedThisTick = new HashSet<>();

	private int blockGroundSoundsUntilTick = Integer.MAX_VALUE;
	private int pendingRewardGroup = -1;
	private int pendingRewardTicks;

	@Override
	protected void startUp()
	{
		Path folder = resolveFolder();
		soundLibrary.setFolder(folder);
		droppedAtTick.clear();
		playedRewardGroups.clear();
		playedThisTick.clear();
		pendingRewardGroup = -1;
		blockGroundSoundsUntilTick = Integer.MAX_VALUE;

		clientThread.invokeLater(() ->
		{
			resetInventoryWatch();
			if (client.getGameState() == GameState.LOGGED_IN)
			{
				blockGroundSoundsUntilTick = client.getTickCount();
				announceFolder(folder);
			}
			log.info("All Drop Sounds folder: {} ({} wav files)", folder, soundLibrary.size());
		});
	}

	@Override
	protected void shutDown()
	{
		player.shutdown();
		droppedAtTick.clear();
		playedRewardGroups.clear();
		playedThisTick.clear();
		pendingRewardGroup = -1;
	}

	@Provides
	AllDropSoundsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AllDropSoundsConfig.class);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		GameState state = event.getGameState();
		if (state == GameState.LOADING || state == GameState.HOPPING || state == GameState.LOGIN_SCREEN)
		{
			blockGroundSoundsUntilTick = Integer.MAX_VALUE;
			playedRewardGroups.clear();
			pendingRewardGroup = -1;
			return;
		}

		if (state == GameState.LOGGED_IN)
		{
			blockGroundSoundsUntilTick = client.getTickCount() + 1;
			clientThread.invokeLater(this::resetInventoryWatch);
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!AllDropSoundsConfig.GROUP.equals(event.getGroup()))
		{
			return;
		}
		soundLibrary.setFolder(resolveFolder());
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		playedThisTick.clear();
		chestLootWatch.onGameTick();

		int tick = client.getTickCount();
		droppedAtTick.entrySet().removeIf(entry -> tick - entry.getValue() > 2);

		if (pendingRewardGroup != -1 && pendingRewardTicks > 0)
		{
			pendingRewardTicks--;
			offerRewardGroup(pendingRewardGroup);
			if (pendingRewardTicks == 0)
			{
				pendingRewardGroup = -1;
			}
		}
	}

	@Subscribe
	public void onItemSpawned(ItemSpawned event)
	{
		if (!config.playGroundDrops() || client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}
		if (client.getTickCount() < blockGroundSoundsUntilTick)
		{
			return;
		}

		TileItem tileItem = event.getItem();
		if (tileItem == null)
		{
			return;
		}
		if (config.onlyYourDrops() && !isYourDrop(tileItem))
		{
			return;
		}
		if (wasJustDropped(tileItem.getId()))
		{
			return;
		}

		playItem(tileItem.getId(), tileItem.getQuantity());
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (!config.playChestLoot())
		{
			return;
		}

		ChatMessageType type = event.getType();
		if (type != ChatMessageType.GAMEMESSAGE && type != ChatMessageType.SPAM && type != ChatMessageType.MESBOX)
		{
			return;
		}
		if (!ChestLootMessages.isChestLoot(event.getMessage()))
		{
			return;
		}

		for (Map.Entry<Integer, Integer> gained : chestLootWatch.onChestMessage().entrySet())
		{
			playItem(gained.getKey(), gained.getValue());
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() != InventoryID.INV)
		{
			return;
		}

		Map<Integer, Integer> now = counts(event.getItemContainer());
		Map<Integer, Integer> gained = chestLootWatch.onInventoryChange(now);
		for (Integer itemId : chestLootWatch.getLastLost())
		{
			droppedAtTick.put(canonical(itemId), client.getTickCount());
		}
		if (!config.playChestLoot())
		{
			return;
		}
		for (Map.Entry<Integer, Integer> entry : gained.entrySet())
		{
			playItem(entry.getKey(), entry.getValue());
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (!config.playChestLoot())
		{
			return;
		}

		int groupId = event.getGroupId();
		if (!isRewardChest(groupId) || playedRewardGroups.contains(groupId))
		{
			return;
		}
		if (groupId == InterfaceID.TOB_CHESTS && !inTobChestRegion())
		{
			return;
		}

		pendingRewardGroup = groupId;
		pendingRewardTicks = 5;
		clientThread.invokeLater(() -> offerRewardGroup(groupId));
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed event)
	{
		int groupId = event.getGroupId();
		playedRewardGroups.remove(groupId);
		if (pendingRewardGroup == groupId)
		{
			pendingRewardGroup = -1;
		}
	}

	private void offerRewardGroup(int groupId)
	{
		if (playedRewardGroups.contains(groupId))
		{
			return;
		}

		boolean found = false;
		if (groupId == InterfaceID.WILDY_LOOT_CHEST)
		{
			for (int containerId : PVP_LOOT_CONTAINERS)
			{
				found |= playContainer(containerId);
			}
		}
		else
		{
			Integer containerId = rewardContainer(groupId);
			found = containerId != null && playContainer(containerId);
		}

		if (found)
		{
			playedRewardGroups.add(groupId);
			if (pendingRewardGroup == groupId)
			{
				pendingRewardGroup = -1;
			}
		}
	}

	private boolean playContainer(int containerId)
	{
		ItemContainer container = client.getItemContainer(containerId);
		if (container == null)
		{
			return false;
		}

		boolean found = false;
		for (Item item : container.getItems())
		{
			if (item.getId() <= 0 || item.getQuantity() <= 0)
			{
				continue;
			}
			found = true;
			playItem(item.getId(), item.getQuantity());
		}
		return found;
	}

	private void playItem(int itemId, int quantity)
	{
		String name = itemName(itemId);
		if (name == null || !playedThisTick.add(name.toLowerCase(Locale.ENGLISH)))
		{
			return;
		}

		Path wav = soundLibrary.findDropSound(name, tierFor(itemId, quantity));
		if (wav == null)
		{
			return;
		}
		player.play(wav, config.volume());
	}

	private ValueTier tierFor(int itemId, int quantity)
	{
		int id = canonical(itemId);
		long gePrice = itemManager.getItemPrice(id);
		long haPrice = itemManager.getItemComposition(id).getHaPrice();
		long value = PriceSource.stackValue(PriceSource.unitPrice(gePrice, haPrice, config.priceSource()), quantity);
		return ValueTiers.match(
			value,
			config.lowValuePrice(),
			config.mediumValuePrice(),
			config.highValuePrice(),
			config.insaneValuePrice(),
			config.insanePlusValuePrice(),
			config.insanePlusPlusValuePrice()
		);
	}

	private String itemName(int itemId)
	{
		String name = itemManager.getItemComposition(canonical(itemId)).getName();
		if (name == null)
		{
			return null;
		}
		name = Text.removeTags(name).trim();
		if (name.isEmpty() || name.equalsIgnoreCase("null"))
		{
			return null;
		}
		return name;
	}

	private int canonical(int itemId)
	{
		return itemManager.canonicalize(itemId);
	}

	private boolean wasJustDropped(int itemId)
	{
		Integer tick = droppedAtTick.get(canonical(itemId));
		return tick != null && client.getTickCount() - tick <= 1;
	}

	private static boolean isYourDrop(TileItem tileItem)
	{
		int ownership = tileItem.getOwnership();
		return ownership == TileItem.OWNERSHIP_SELF || ownership == TileItem.OWNERSHIP_GROUP;
	}

	private boolean inTobChestRegion()
	{
		if (client.getLocalPlayer() == null)
		{
			return false;
		}

		WorldPoint worldPoint = WorldPoint.fromLocalInstance(client, client.getLocalPlayer().getLocalLocation());
		int region = worldPoint.getRegionID();
		return region == THEATRE_OF_BLOOD_REGION || region == THEATRE_OF_BLOOD_LOBBY;
	}

	private static boolean isRewardChest(int groupId)
	{
		return rewardContainer(groupId) != null || groupId == InterfaceID.WILDY_LOOT_CHEST;
	}

	private static Integer rewardContainer(int groupId)
	{
		if (groupId == InterfaceID.BARROWS_REWARD)
		{
			return InventoryID.TRAIL_REWARDINV;
		}
		if (groupId == InterfaceID.RAIDS_REWARDS)
		{
			return InventoryID.RAIDS_REWARDS;
		}
		if (groupId == InterfaceID.TOB_CHESTS)
		{
			return InventoryID.TOB_CHESTS;
		}
		if (groupId == InterfaceID.TOA_CHESTS)
		{
			return InventoryID.TOA_CHESTS;
		}
		if (groupId == InterfaceID.PMOON_REWARD)
		{
			return InventoryID.PMOON_REWARDINV;
		}
		if (groupId == InterfaceID.COLOSSEUM_REWARD_CHEST_2)
		{
			return InventoryID.COLOSSEUM_REWARDS;
		}
		return null;
	}

	private void resetInventoryWatch()
	{
		chestLootWatch.reset(counts(client.getItemContainer(InventoryID.INV)));
	}

	private static Map<Integer, Integer> counts(ItemContainer container)
	{
		Map<Integer, Integer> counts = new HashMap<>();
		if (container == null)
		{
			return counts;
		}

		for (Item item : container.getItems())
		{
			if (item.getId() <= 0 || item.getQuantity() <= 0)
			{
				continue;
			}
			counts.merge(item.getId(), item.getQuantity(), Integer::sum);
		}
		return counts;
	}

	private Path resolveFolder()
	{
		String configured = config.soundsFolder();
		Path folder;
		if (configured == null || configured.trim().isEmpty())
		{
			folder = new File(RuneLite.RUNELITE_DIR, "all-drop-sounds").toPath();
		}
		else
		{
			folder = new File(configured.trim()).toPath();
		}

		File directory = folder.toFile();
		if (!directory.isDirectory() && !directory.mkdirs())
		{
			log.warn("Could not create sounds folder {}", directory);
		}
		return folder;
	}

	private void announceFolder(Path folder)
	{
		client.addChatMessage(
			ChatMessageType.GAMEMESSAGE,
			"",
			"All Drop Sounds is reading wavs from " + folder,
			null
		);
	}
}
