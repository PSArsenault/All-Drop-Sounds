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
	private volatile Clip active;
	private int generation;
	private int activePriority = SoundPriority.NONE;

	public void play(Path wav, int volumePercent)
	{
		play(wav, volumePercent, 0);
	}

	public synchronized void play(Path wav, int volumePercent, int priority)
	{
		if (wav == null || volumePercent <= 0)
		{
			return;
		}

		int ticket = generation;
		if (priority > activePriority)
		{
			ticket = ++generation;
			stopActive();
			activePriority = priority;
		}

		int volume = Math.min(100, volumePercent);
		int playbackTicket = ticket;
		executor().execute(() -> playNow(wav, volume, playbackTicket));
	}

	public synchronized void shutdown()
	{
		generation++;
		stopActive();
		activePriority = SoundPriority.NONE;
		if (executor != null)
		{
			executor.shutdownNow();
		}
	}

	private void stopActive()
	{
		Clip clip = active;
		active = null;
		if (clip != null)
		{
			clip.stop();
			clip.close();
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

	private void playNow(Path wav, int volumePercent, int ticket)
	{
		try (AudioInputStream input = AudioSystem.getAudioInputStream(wav.toFile()))
		{
			Clip clip = AudioSystem.getClip();
			try
			{
				clip.open(input);
			}
			catch (Exception ex)
			{
				clip.close();
				log.warn("Could not play {}", wav, ex);
				return;
			}

			synchronized (this)
			{
				if (ticket != generation)
				{
					clip.close();
					return;
				}
				active = clip;
			}

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
