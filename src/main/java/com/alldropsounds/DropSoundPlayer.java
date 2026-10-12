package com.alldropsounds;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DropSoundPlayer
{
	private static final Logger log = LoggerFactory.getLogger(DropSoundPlayer.class);

	private ExecutorService executor;

	public void play(Path wav, int volumePercent)
	{
		if (wav == null || volumePercent <= 0)
		{
			return;
		}

		int volume = Math.min(100, volumePercent);
		executor().execute(() -> playNow(wav, volume));
	}

	public void shutdown()
	{
		if (executor != null)
		{
			executor.shutdownNow();
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

	private static void playNow(Path wav, int volumePercent)
	{
		try (AudioInputStream input = AudioSystem.getAudioInputStream(wav.toFile()))
		{
			Clip clip = AudioSystem.getClip();
			clip.open(input);
			if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN))
			{
				FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
				float decibels = decibels(volumePercent);
				decibels = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), decibels));
				gain.setValue(decibels);
			}
			clip.addLineListener(event ->
			{
				if (event.getType() == LineEvent.Type.STOP)
				{
					clip.close();
				}
			});
			clip.start();
		}
		catch (Exception ex)
		{
			log.warn("Could not play {}", wav, ex);
		}
	}
}
