package com.alldropsounds;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.runelite.client.audio.AudioPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DropSoundPlayer
{
	private static final Logger log = LoggerFactory.getLogger(DropSoundPlayer.class);

	private final AudioPlayer audioPlayer = new AudioPlayer();
	private ExecutorService executor;

	public synchronized void play(Path wav, int volumePercent)
	{
		if (wav == null || volumePercent <= 0)
		{
			return;
		}

		int volume = Math.min(100, volumePercent);
		float gain = decibels(volume);
		executor().execute(() -> playNow(wav, gain));
	}

	public synchronized void shutdown()
	{
		if (executor != null)
		{
			executor.shutdownNow();
			executor = null;
		}
	}

	private ExecutorService executor()
	{
		if (executor == null || executor.isShutdown())
		{
			executor = Executors.newCachedThreadPool(runnable ->
			{
				Thread thread = new Thread(runnable, "all-drop-sounds");
				thread.setDaemon(true);
				return thread;
			});
		}
		return executor;
	}

	static float decibels(int volumePercent)
	{
		if (volumePercent <= 0)
		{
			return -80f;
		}
		if (volumePercent >= 100)
		{
			return 0f;
		}
		return (float) (20.0 * Math.log10(volumePercent / 100.0));
	}

	private void playNow(Path wav, float gain)
	{
		try
		{
			audioPlayer.play(wav.toFile(), gain);
		}
		catch (Exception ex)
		{
			log.warn("Could not play {}", wav, ex);
		}
	}
}
