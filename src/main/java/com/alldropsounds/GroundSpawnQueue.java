package com.alldropsounds;

import java.util.ArrayList;
import java.util.List;

/**
 * Ground spawns wait until the following ticks so an inventory drop is visible first.
 */
public final class GroundSpawnQueue
{
	static final class Spawn
	{
		final int itemId;
		final int quantity;
		final int ownership;
		final int tick;

		Spawn(int itemId, int quantity, int ownership, int tick)
		{
			this.itemId = itemId;
			this.quantity = quantity;
			this.ownership = ownership;
			this.tick = tick;
		}
	}

	private final List<Spawn> pending = new ArrayList<Spawn>();

	public void add(int itemId, int quantity, int ownership, int tick)
	{
		pending.add(new Spawn(itemId, quantity, ownership, tick));
	}

	public List<Spawn> drainReady(int currentTick)
	{
		List<Spawn> ready = new ArrayList<Spawn>();
		for (int i = pending.size() - 1; i >= 0; i--)
		{
			Spawn spawn = pending.get(i);
			long age = (long) currentTick - spawn.tick;
			if (age >= 2)
			{
				ready.add(spawn);
				pending.remove(i);
			}
		}
		return ready;
	}
}
