package com.alldropsounds;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.runelite.client.util.Filepath;

/**
 * Maps an item name to a wav file in the sounds folder.
 * The file name, without .wav, is the item name.
 * "Twisted bow 2.wav" takes a turn with "Twisted bow.wav".
 * A file matches an item only by its exact name, unless the file name contains %.
 * A name ending in " (uncharged)" also matches the wav without that suffix.
 */
public final class SoundLibrary
{
	private static final Pattern VARIANT = Pattern.compile("^(.*?)(?:\\s+(\\d+))?$");

	private final Map<String, List<Filepath>> groups = new HashMap<String, List<Filepath>>();
	private final List<String> wildcards = new ArrayList<String>();
	private final Map<String, Integer> nextVariant = new HashMap<String, Integer>();
	private Filepath folder;
	private long folderStamp = Long.MIN_VALUE;
	private int fileCount;

	public synchronized void setFolder(Filepath folder)
	{
		this.folder = folder;
		folderStamp = Long.MIN_VALUE;
		refreshIfNeeded();
	}

	public synchronized Filepath find(String itemName)
	{
		refreshIfNeeded();
		return lookup(itemName);
	}

	/**
	 * Item-named wavs win. Otherwise the price-tier wav is used.
	 */
	public synchronized Filepath findDropSound(String itemName, ValueTier tier)
	{
		refreshIfNeeded();
		Filepath specific = lookup(itemName);
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

	private Filepath lookup(String itemName)
	{
		String key = normalize(itemName);
		if (key == null)
		{
			return null;
		}

		if (groups.containsKey(key))
		{
			return takeTurn(key);
		}

		String base = withoutUncharged(key);
		if (!base.equals(key) && groups.containsKey(base))
		{
			return takeTurn(base);
		}

		String wildcard = longestWildcard(key);
		if (wildcard == null && !base.equals(key))
		{
			wildcard = longestWildcard(base);
		}
		if (wildcard != null)
		{
			return takeTurn(wildcard);
		}
		return null;
	}

	/**
	 * Charged gear drops as "Name (uncharged)". That suffix is not part of the wav name.
	 */
	static String withoutUncharged(String key)
	{
		final String suffix = " (uncharged)";
		if (key != null && key.endsWith(suffix) && key.length() > suffix.length())
		{
			String base = key.substring(0, key.length() - suffix.length()).trim();
			if (!base.isEmpty())
			{
				return base;
			}
		}
		return key;
	}

	public synchronized Filepath findExact(String name)
	{
		refreshIfNeeded();
		String key = normalize(name);
		if (key == null || key.indexOf('%') >= 0 || !groups.containsKey(key))
		{
			return null;
		}
		return takeTurn(key);
	}

	private Filepath takeTurn(String key)
	{
		List<Filepath> files = groups.get(key);
		if (files == null || files.isEmpty())
		{
			return null;
		}

		int index = nextVariant.containsKey(key) ? nextVariant.get(key).intValue() : 0;
		Filepath path = files.get(Math.floorMod(index, files.size()));
		nextVariant.put(key, Integer.valueOf(index + 1));
		return path;
	}

	private String longestWildcard(String itemName)
	{
		String best = null;
		for (String pattern : wildcards)
		{
			if (wildcard(pattern, itemName) && (best == null || pattern.length() > best.length()))
			{
				best = pattern;
			}
		}
		return best;
	}

	public synchronized int size()
	{
		refreshIfNeeded();
		return fileCount;
	}

	private void refreshIfNeeded()
	{
		if (folder == null)
		{
			clear();
			folderStamp = Long.MIN_VALUE;
			return;
		}

		long stamp = directoryStamp(folder);
		if (stamp == folderStamp && fileCount > 0)
		{
			return;
		}

		folderStamp = stamp;
		clear();
		if (!folder.isDirectory())
		{
			return;
		}

		try (Stream<Filepath> stream = folder.walk(1))
		{
			Iterator<Filepath> iterator = stream.iterator();
			while (iterator.hasNext())
			{
				Filepath path = iterator.next();
				if (!path.isFile())
				{
					continue;
				}

				String fileName = path.getFileName();
				if (fileName.length() <= 4 || !fileName.toLowerCase(Locale.ENGLISH).endsWith(".wav"))
				{
					continue;
				}

				String stem = fileName.substring(0, fileName.length() - 4).trim();
				if (stem.isEmpty())
				{
					continue;
				}

				String base = baseName(stem);
				if (base.isEmpty())
				{
					continue;
				}

				List<Filepath> files = groups.get(base);
				if (files == null)
				{
					files = new ArrayList<Filepath>();
					groups.put(base, files);
					if (base.indexOf('%') >= 0)
					{
						wildcards.add(base);
					}
				}
				files.add(path);
				fileCount++;
			}
		}
		catch (IOException ex)
		{
			clear();
		}

		for (List<Filepath> files : groups.values())
		{
			Collections.sort(files, new Comparator<Filepath>()
			{
				@Override
				public int compare(Filepath left, Filepath right)
				{
					int byNumber = variantNumber(left) - variantNumber(right);
					if (byNumber != 0)
					{
						return byNumber;
					}
					return left.getFileName().compareToIgnoreCase(right.getFileName());
				}
			});
		}
	}

	private static long directoryStamp(Filepath folder)
	{
		if (!folder.isDirectory())
		{
			return Long.MIN_VALUE;
		}

		try
		{
			return folder.getLastModifiedTime().toMillis();
		}
		catch (IOException ex)
		{
			return Long.MIN_VALUE;
		}
	}

	private void clear()
	{
		groups.clear();
		wildcards.clear();
		nextVariant.clear();
		fileCount = 0;
	}

	static String normalize(String itemName)
	{
		if (itemName == null)
		{
			return null;
		}

		String key = itemName.trim().toLowerCase(Locale.ENGLISH).replaceAll("\\s+", " ");
		if (key.isEmpty() || "null".equals(key))
		{
			return null;
		}
		return key;
	}

	static String baseName(String stem)
	{
		String key = normalize(stem);
		if (key == null)
		{
			return "";
		}

		Matcher matcher = VARIANT.matcher(key);
		if (matcher.matches() && matcher.group(2) != null)
		{
			return matcher.group(1).trim();
		}
		return key;
	}

	private static int variantNumber(Filepath path)
	{
		String stem = path.getFileName();
		int dot = stem.toLowerCase(Locale.ENGLISH).lastIndexOf(".wav");
		if (dot > 0)
		{
			stem = stem.substring(0, dot);
		}
		Matcher matcher = VARIANT.matcher(stem.trim().toLowerCase(Locale.ENGLISH));
		if (matcher.matches() && matcher.group(2) != null)
		{
			return Integer.parseInt(matcher.group(2));
		}
		return 1;
	}

	static boolean wildcard(String pattern, String itemName)
	{
		StringBuilder regex = new StringBuilder("(?i)");
		for (int i = 0; i < pattern.length(); i++)
		{
			char c = pattern.charAt(i);
			if (c == '%')
			{
				regex.append(".*");
			}
			else if ("\\.^$|?+()[]{}".indexOf(c) >= 0)
			{
				regex.append('\\').append(c);
			}
			else
			{
				regex.append(c);
			}
		}
		return itemName.matches(regex.toString());
	}
}
