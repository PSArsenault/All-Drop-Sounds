package com.alldropsounds;

import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.ActorSpotAnim;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.IterableHashTable;
import net.runelite.api.NPC;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.TileItem;
import net.runelite.api.TileObject;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.DecorativeObjectSpawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicChanged;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.ItemSpawned;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WallObjectSpawned;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.ItemComposition;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Filepath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PluginDescriptor(
	name = "All Drop Sounds",
	internalName = "all-drop-sounds",
	description = "Plays a wav named after an item when it drops or comes from a chest",
	tags = {"sound", "drops", "loot", "chest"}
)
public class AllDropSoundsPlugin extends Plugin
{
	private static final Logger log = LoggerFactory.getLogger(AllDropSoundsPlugin.class);
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
	private ConfigManager configManager;

	@Inject
	private AllDropSoundsConfig config;

	private final SoundLibrary soundLibrary = new SoundLibrary();
	private final DropSoundPlayer player = new DropSoundPlayer();
	private final ChestLootWatch chestLootWatch = new ChestLootWatch();
	private final Map<Integer, Integer> droppedAtTick = new HashMap<>();
	private final Set<Integer> playedRewardGroups = new HashSet<>();
	private final Set<String> playedThisTick = new HashSet<>();
	private final Set<String> announcedPurple = new HashSet<>();
	private final Set<Integer> revealedLoot = new HashSet<>();

	private int blockGroundSoundsUntilTick = Integer.MAX_VALUE;
	private boolean purplePlayedThisTick;
	private boolean toaSarcophagusLoaded;
	private int pendingRewardGroup = -1;
	private int pendingRewardTicks;
	private int toaRevealTicks;
	private List<String> highlightedLists = Collections.emptyList();

	@Override
	protected void startUp() throws Exception
	{
		Filepath folder = getPluginDirectory();
		folder.createDirectories();
		soundLibrary.setFolder(folder);
		refreshHighlightedLists();
		droppedAtTick.clear();
		playedRewardGroups.clear();
		playedThisTick.clear();
		announcedPurple.clear();
		revealedLoot.clear();
		toaRevealTicks = 0;
		purplePlayedThisTick = false;
		toaSarcophagusLoaded = false;
		pendingRewardGroup = -1;
		blockGroundSoundsUntilTick = Integer.MAX_VALUE;

		clientThread.invokeLater(() ->
		{
			resetInventoryWatch();
			if (client.getGameState() == GameState.LOGGED_IN)
			{
				blockGroundSoundsUntilTick = client.getTickCount();
				announceFolder(folder);
				scanPurpleChests();
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
		announcedPurple.clear();
		revealedLoot.clear();
		toaRevealTicks = 0;
		purplePlayedThisTick = false;
		toaSarcophagusLoaded = false;
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
			droppedAtTick.clear();
			playedThisTick.clear();
			playedRewardGroups.clear();
			announcedPurple.clear();
			revealedLoot.clear();
			toaRevealTicks = 0;
			purplePlayedThisTick = false;
			toaSarcophagusLoaded = false;
			pendingRewardGroup = -1;
			return;
		}

		if (state == GameState.LOGGED_IN)
		{
			blockGroundSoundsUntilTick = client.getTickCount() + 1;
			clientThread.invokeLater(() ->
			{
				resetInventoryWatch();
				scanPurpleChests();
			});
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		refreshHighlightedLists();
		if (AllDropSoundsConfig.GROUP.equals(event.getGroup())
			&& "purpleNotification".equals(event.getKey())
			&& config.purpleNotification())
		{
			clientThread.invokeLater(this::scanPurpleChests);
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		playedThisTick.clear();
		purplePlayedThisTick = false;
		chestLootWatch.onGameTick();

		int tick = client.getTickCount();
		droppedAtTick.entrySet().removeIf(entry -> GroundDropRules.dropExpired(entry.getValue(), tick));

		if (pendingRewardGroup != -1 && pendingRewardTicks > 0)
		{
			pendingRewardTicks--;
			offerRewardGroup(pendingRewardGroup);
			if (pendingRewardTicks == 0)
			{
				pendingRewardGroup = -1;
			}
		}
		if (toaRevealTicks > 0)
		{
			toaRevealTicks--;
			if (playToaReveal())
			{
				toaRevealTicks = 0;
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
		if (GroundDropRules.spawnBlocked(client.getTickCount(), blockGroundSoundsUntilTick))
		{
			return;
		}

		TileItem tileItem = event.getItem();
		if (tileItem == null)
		{
			return;
		}
		boolean selfDrop = GroundDropRules.isRememberedDrop(droppedAtTick.get(canonical(tileItem.getId())), client.getTickCount());
		if (!GroundDropRules.shouldPlay(config.playSelfDrops(), config.onlyYourDrops(), tileItem.getOwnership(), selfDrop))
		{
			return;
		}

		playItem(tileItem.getId(), tileItem.getQuantity());
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (!GroundDropRules.isManualDrop(event.getMenuOption(), event.isItemOp()) || event.getItemId() <= 0)
		{
			return;
		}
		droppedAtTick.put(canonical(event.getItemId()), client.getTickCount());
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		GameObject gameObject = event.getGameObject();
		noteObject(gameObject);
		noteToaLootObject(gameObject);
	}

	@Subscribe
	public void onGroundObjectSpawned(GroundObjectSpawned event)
	{
		noteObject(event.getGroundObject());
	}

	@Subscribe
	public void onWallObjectSpawned(WallObjectSpawned event)
	{
		noteObject(event.getWallObject());
	}

	@Subscribe
	public void onDecorativeObjectSpawned(DecorativeObjectSpawned event)
	{
		noteObject(event.getDecorativeObject());
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		noteNpc(event.getNpc());
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if (!PurpleChests.isToaSarcophagusVarbit(event.getVarbitId())
			|| !PurpleChests.toaSarcophagusIsPurple(event.getValue())
			|| !toaSarcophagusLoaded)
		{
			return;
		}
		notePurple(PurpleChests.TOA_KEY);
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
		if (groupId == InterfaceID.TFA_CHESTS || groupId == InterfaceID.TOA_CHESTS)
		{
			revealedLoot.clear();
			toaRevealTicks = 0;
		}
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

		Item[] items = container.getItems();
		if (items == null)
		{
			return false;
		}

		boolean found = false;
		for (Item item : items)
		{
			if (item == null || item.getId() <= 0 || item.getQuantity() <= 0)
			{
				continue;
			}
			found = true;
			playItem(item.getId(), item.getQuantity());
		}
		return found;
	}

	@Subscribe
	public void onGraphicsObjectCreated(GraphicsObjectCreated event)
	{
		noteTfaReveal(event.getGraphicsObject().getId());
	}

	@Subscribe
	public void onGraphicChanged(GraphicChanged event)
	{
		Actor actor = event.getActor();
		if (actor == null)
		{
			return;
		}
		noteTfaReveal(actor.getGraphic());
		IterableHashTable<ActorSpotAnim> spotAnims = actor.getSpotAnims();
		if (spotAnims == null)
		{
			return;
		}
		for (ActorSpotAnim spotAnim : spotAnims)
		{
			if (spotAnim != null)
			{
				noteTfaReveal(spotAnim.getId());
			}
		}
	}

	private void noteToaLootObject(GameObject gameObject)
	{
		if (gameObject == null || !config.playChestLoot() || !config.lootReveal().playsOnReveal())
		{
			return;
		}
		int rawId = gameObject.getId();
		if (!ToaLootReveal.isLootObject(rawId) && !ToaLootReveal.isLootObject(resolvedObjectId(rawId)))
		{
			return;
		}
		if (!playToaReveal())
		{
			toaRevealTicks = 20;
		}
	}

	private boolean playToaReveal()
	{
		ItemContainer container = client.getItemContainer(InventoryID.TOA_CHESTS);
		if (container == null || container.getItems() == null)
		{
			return false;
		}
		boolean found = false;
		for (Item item : container.getItems())
		{
			if (item == null || item.getId() <= 0 || item.getQuantity() <= 0)
			{
				continue;
			}
			found = true;
			noteRevealedItem(item.getId(), item.getQuantity());
		}
		return found;
	}

	private void noteTfaReveal(int spotanimId)
	{
		int itemId = TfaLootReveal.itemId(spotanimId);
		if (itemId == 0)
		{
			return;
		}
		noteRevealedItem(itemId, 1);
	}

	private void noteRevealedItem(int itemId, int quantity)
	{
		if (!config.playChestLoot() || !config.lootReveal().playsOnReveal())
		{
			return;
		}
		int id = canonical(itemId);
		if (!revealedLoot.add(id))
		{
			return;
		}
		playItem(id, quantity, false);
	}

	private void playItem(int itemId, int quantity)
	{
		playItem(itemId, quantity, true);
	}

	private void playItem(int itemId, int quantity, boolean skipRevealed)
	{
		if (skipRevealed
			&& !config.lootReveal().playsOnInterface()
			&& revealedLoot.contains(canonical(itemId)))
		{
			return;
		}

		String name = itemName(itemId);
		if (name == null)
		{
			return;
		}

		if (IgnoredItems.contains(config.ignoredItems(), name))
		{
			return;
		}

		String key = name.toLowerCase(Locale.ENGLISH);
		if (playedThisTick.contains(key))
		{
			return;
		}

		Filepath specific = soundLibrary.find(name);
		ValueTier valueTier = tierFor(itemId, quantity);
		boolean highlighted = specific == null && HighlightedLists.matches(highlightedLists, name, quantity);
		Filepath highlightedFile = highlighted ? soundLibrary.findExact(DropSoundPlan.HIGHLIGHTED_FILE) : null;
		boolean unique = specific == null && highlightedFile == null && config.playUniques() && isUnique(itemId, name);
		Filepath uniqueFile = null;
		if (unique)
		{
			uniqueFile = soundLibrary.findExact(UniqueItems.categoryStem(name));
			if (uniqueFile == null)
			{
				uniqueFile = soundLibrary.findExact(DropSoundPlan.UNIQUE_FILE);
			}
		}
		ValueTier tier = specific == null && highlightedFile == null && uniqueFile == null ? valueTier : null;
		Filepath tierFile = tier == null ? null : soundLibrary.find(tier.getFileName());
		DropSoundPlan plan = DropSoundPlan.choose(
			specific,
			highlightedFile != null ? highlightedFile : uniqueFile,
			tierFile,
			config.itemVolume(),
			highlightedFile != null ? config.highlightedVolume() : config.uniqueVolume(),
			tier == null ? 0 : volumeFor(tier)
		);
		if (plan == null)
		{
			return;
		}

		int volume = GameAudio.applyOutput(
			DropSoundPlan.applyMaster(plan.volume, config.masterVolume()),
			gameVolumePercent()
		);
		if (volume <= 0)
		{
			return;
		}

		player.play(plan.file, volume);
		playedThisTick.add(key);
	}

	private int volumeFor(ValueTier tier)
	{
		switch (tier)
		{
			case LOW:
				return config.lowValueVolume();
			case MEDIUM:
				return config.mediumValueVolume();
			case HIGH:
				return config.highValueVolume();
			case INSANE:
				return config.insaneValueVolume();
			case INSANE_PLUS:
				return config.insanePlusValueVolume();
			case INSANE_PLUS_PLUS:
				return config.insanePlusPlusValueVolume();
			default:
				return 0;
		}
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

	private boolean isUnique(int itemId, String name)
	{
		int id = canonical(itemId);
		ItemComposition composition = itemManager.getItemComposition(id);
		long gePrice = itemManager.getItemPrice(id);
		long haPrice = composition.getHaPrice();
		return UniqueItems.isUnique(name, composition.isTradeable(), gePrice, haPrice);
	}

	private int gameVolumePercent()
	{
		try
		{
			return GameAudio.outputPercent(
				client.getVarpValue(VarPlayerID.OPTION_MASTER_VOLUME),
				client.getVarpValue(VarPlayerID.OPTION_SOUNDS)
			);
		}
		catch (Exception ex)
		{
			return 100;
		}
	}

	private void refreshHighlightedLists()
	{
		List<String> keys = configManager.getConfigurationKeys("");
		List<String> lists = new ArrayList<String>();
		if (keys != null)
		{
			for (String wholeKey : keys)
			{
				if (!HighlightedLists.isHighlightKey(wholeKey))
				{
					continue;
				}
				String[] parts = HighlightedLists.groupAndKey(wholeKey);
				if (parts == null)
				{
					continue;
				}
				String value = configManager.getConfiguration(parts[0], parts[1]);
				if (HighlightedLists.isItemList(value))
				{
					lists.add(value);
				}
			}
		}
		highlightedLists = lists;
	}

	private String itemName(int itemId)
	{
		String name = itemManager.getItemComposition(canonical(itemId)).getName();
		if (name == null)
		{
			return null;
		}
		return ItemNames.clean(name);
	}

	private int canonical(int itemId)
	{
		return itemManager.canonicalize(itemId);
	}

	private boolean inTobChestRegion()
	{
		if (client.getLocalPlayer() == null)
		{
			return false;
		}

		WorldPoint worldPoint = WorldPoint.fromLocalInstance(client, client.getLocalPlayer().getLocalLocation());
		int region = worldPoint.getRegionID();
		return RewardChests.inTheatreOfBlood(region);
	}

	private static boolean isRewardChest(int groupId)
	{
		return RewardChests.isRewardChest(groupId);
	}

	private static Integer rewardContainer(int groupId)
	{
		return RewardChests.rewardContainer(groupId);
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

		Item[] items = container.getItems();
		if (items == null)
		{
			return counts;
		}

		for (Item item : items)
		{
			if (item == null || item.getId() <= 0 || item.getQuantity() <= 0)
			{
				continue;
			}
			counts.merge(item.getId(), item.getQuantity(), InventoryDiff::addQuantity);
		}
		return counts;
	}

	private void scanPurpleChests()
	{
		if (!config.purpleNotification() || client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		WorldView worldView = client.getTopLevelWorldView();
		Scene scene = worldView == null ? null : worldView.getScene();
		Tile[][][] tiles = scene == null ? null : scene.getTiles();
		if (tiles != null)
		{
			for (Tile[][] plane : tiles)
			{
				if (plane == null)
				{
					continue;
				}
				for (Tile[] row : plane)
				{
					if (row == null)
					{
						continue;
					}
					for (Tile tile : row)
					{
						if (tile == null)
						{
							continue;
						}
						GameObject[] gameObjects = tile.getGameObjects();
						if (gameObjects != null)
						{
							for (GameObject gameObject : gameObjects)
							{
								noteObject(gameObject);
							}
						}
						noteObject(tile.getGroundObject());
						noteObject(tile.getWallObject());
						noteObject(tile.getDecorativeObject());
					}
				}
			}
		}

		if (worldView != null && worldView.npcs() != null)
		{
			for (NPC npc : worldView.npcs())
			{
				noteNpc(npc);
			}
		}

		if (toaSarcophagusLoaded && PurpleChests.toaSarcophagusIsPurple(client.getVarbitValue(VarbitID.TOA_VAULT_SARCOPHAGUS)))
		{
			notePurple(PurpleChests.TOA_KEY);
		}
	}

	private void noteObject(TileObject object)
	{
		if (object == null)
		{
			return;
		}

		int rawId = object.getId();
		int resolvedId = resolvedObjectId(rawId);
		boolean sarcophagus = PurpleChests.isToaSarcophagus(rawId) || PurpleChests.isToaSarcophagus(resolvedId);
		if (sarcophagus)
		{
			toaSarcophagusLoaded = true;
		}

		WorldPoint point = object.getWorldLocation();
		if (point != null && PurpleChests.isPurpleObject(resolvedId))
		{
			notePurple(PurpleChests.objectKey(resolvedId, point.getX(), point.getY(), point.getPlane()));
			return;
		}

		if (sarcophagus && PurpleChests.toaSarcophagusIsPurple(client.getVarbitValue(VarbitID.TOA_VAULT_SARCOPHAGUS)))
		{
			notePurple(PurpleChests.TOA_KEY);
		}
	}

	private void noteNpc(NPC npc)
	{
		if (npc == null || !PurpleChests.isPurpleNpc(npc.getId()))
		{
			return;
		}

		WorldPoint point = npc.getWorldLocation();
		if (point == null)
		{
			return;
		}
		notePurple(npc.getId() + ":" + point.getX() + ":" + point.getY() + ":" + point.getPlane());
	}

	private int resolvedObjectId(int objectId)
	{
		if (!PurpleChests.isPurpleParent(objectId))
		{
			return objectId;
		}

		try
		{
			ObjectComposition composition = client.getObjectDefinition(objectId);
			ObjectComposition impostor = composition == null ? null : composition.getImpostor();
			if (impostor != null)
			{
				return impostor.getId();
			}
		}
		catch (Exception ignored)
		{
			log.debug("Could not resolve object {}", objectId);
		}
		return objectId;
	}

	private void notePurple(String key)
	{
		if (!config.purpleNotification() || key == null || announcedPurple.contains(key))
		{
			return;
		}

		announcedPurple.add(key);
		if (purplePlayedThisTick)
		{
			return;
		}

		Filepath wav = soundLibrary.findExact(PurpleChests.FILE);
		int volume = wav == null ? 0 : GameAudio.applyOutput(
			DropSoundPlan.applyMaster(100, config.masterVolume()),
			gameVolumePercent()
		);
		if (wav == null || volume <= 0)
		{
			announcedPurple.remove(key);
			return;
		}

		purplePlayedThisTick = true;
		player.play(wav, volume);
	}

	private void announceFolder(Filepath folder)
	{
		client.addChatMessage(
			ChatMessageType.GAMEMESSAGE,
			"",
			"All Drop Sounds is reading wavs from " + folder,
			null
		);
	}
}
