package com.alldropsounds;

/**
 * Decides whether a new sound may start. A higher priority interrupts.
 * The same or a lower priority waits out the cooldown.
 */
public final class SoundGate
{
	private SoundGate()
	{
	}

	public static boolean allow(int incomingPriority, int currentPriority, long elapsedMs, int cooldownMs)
	{
		if (incomingPriority > currentPriority)
		{
			return true;
		}
		if (cooldownMs <= 0)
		{
			return true;
		}
		return elapsedMs >= cooldownMs;
	}
}
