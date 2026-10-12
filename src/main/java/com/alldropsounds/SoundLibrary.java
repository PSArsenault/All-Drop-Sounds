package com.alldropsounds;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Maps an item name to a wav file in the sounds folder.
 * The file name, without .wav, is the item name.
 */
public final class SoundLibrary
{
	private final Map<String, Path> byItemName = new HashMap<>();
	private Path folder;
	private long folderStamp = Long.MIN_VALUE;

	public synchronized void setFolder(Path folder)
	{
		this.folder = folder;
		folderStamp = Long.MIN_VALUE;
		refreshIfNeeded();
	}

	public synchronized Path find(String itemName)
	{
		refreshIfNeeded();
		return lookup(itemName);
	}

	/**
	 * Item-named wavs win. Otherwise the price-tier wav is used.
	 */
	public synchronized Path findDropSound(String itemName, ValueTier tier)
	{
		refreshIfNeeded();
		Path specific = lookup(itemName);
		if (specific != null)
		{
			return specific;
		}
		if (tier == null)
		{
			return null;
		}
		return lookup(tier.getFileName());
	}

	private Path lookup(String itemName)
	{
		if (itemName == null)
		{
			return null;
		}

		String key = itemName.trim().toLowerCase(Locale.ENGLISH);
		if (key.isEmpty() || "null".equals(key))
		{
			return null;
		}
		return byItemName.get(key);
	}

	public synchronized int size()
	{
		refreshIfNeeded();
		return byItemName.size();
	}

	private void refreshIfNeeded()
	{
		if (folder == null)
		{
			byItemName.clear();
			folderStamp = Long.MIN_VALUE;
			return;
		}

		long stamp = Files.isDirectory(folder) ? folder.toFile().lastModified() : Long.MIN_VALUE;
		if (stamp == folderStamp && !byItemName.isEmpty())
		{
			return;
		}

		folderStamp = stamp;
		byItemName.clear();
		if (!Files.isDirectory(folder))
		{
			return;
		}

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder))
		{
			for (Path path : stream)
			{
				if (!Files.isRegularFile(path))
				{
					continue;
				}

				String fileName = path.getFileName().toString();
				if (fileName.length() <= 4 || !fileName.toLowerCase(Locale.ENGLISH).endsWith(".wav"))
				{
					continue;
				}

				String stem = fileName.substring(0, fileName.length() - 4).trim();
				if (!stem.isEmpty())
				{
					byItemName.put(stem.toLowerCase(Locale.ENGLISH), path);
				}
			}
		}
		catch (IOException ex)
		{
			byItemName.clear();
		}
	}
}
