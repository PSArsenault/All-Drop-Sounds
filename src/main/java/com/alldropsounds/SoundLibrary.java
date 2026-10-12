package com.alldropsounds;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Maps an item name to a wav file in the sounds folder.
 * The file name, without .wav, is the item name.
 * "Twisted bow 2.wav" takes a turn with "Twisted bow.wav".
 * "Clue scroll.wav" also matches "Clue scroll (easy)".
 */
public final class SoundLibrary
{
	private static final Pattern VARIANT = Pattern.compile("^(.*?)(?:\\s+(\\d+))?$");

	private final Map<String, List<Path>> groups = new HashMap<String, List<Path>>();
	private final List<String> wildcards = new ArrayList<String>();
	private final Map<String, Integer> nextVariant = new HashMap<String, Integer>();
	private Path folder;
	private long folderStamp = Long.MIN_VALUE;
	private int fileCount;

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
		String key = normalize(itemName);
		if (key == null)
		{
			return null;
		}

		if (groups.containsKey(key))
		{
			return takeTurn(key);
		}

		String prefix = longestPrefix(key);
		if (prefix != null)
		{
			return takeTurn(prefix);
		}

		String wildcard = longestWildcard(key);
		if (wildcard != null)
		{
			return takeTurn(wildcard);
		}
		return null;
	}

	private Path takeTurn(String key)
	{
		List<Path> files = groups.get(key);
		if (files == null || files.isEmpty())
		{
			return null;
		}

		int index = nextVariant.containsKey(key) ? nextVariant.get(key).intValue() : 0;
		Path path = files.get(Math.floorMod(index, files.size()));
		nextVariant.put(key, Integer.valueOf(index + 1));
		return path;
	}

	private String longestPrefix(String itemName)
	{
		String best = null;
		for (String base : groups.keySet())
		{
			if (base.indexOf('*') >= 0)
			{
				continue;
			}
			if (itemName.equals(base) || itemName.startsWith(base + " ("))
			{
				if (best == null || base.length() > best.length())
				{
					best = base;
				}
			}
		}
		return best;
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

		long stamp = Files.isDirectory(folder) ? folder.toFile().lastModified() : Long.MIN_VALUE;
		if (stamp == folderStamp && fileCount > 0)
		{
			return;
		}

		folderStamp = stamp;
		clear();
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
				if (stem.isEmpty())
				{
					continue;
				}

				String base = baseName(stem);
				if (base.isEmpty())
				{
					continue;
				}

				List<Path> files = groups.get(base);
				if (files == null)
				{
					files = new ArrayList<Path>();
					groups.put(base, files);
					if (base.indexOf('*') >= 0)
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

		for (List<Path> files : groups.values())
		{
			Collections.sort(files, new Comparator<Path>()
			{
				@Override
				public int compare(Path left, Path right)
				{
					int byNumber = variantNumber(left) - variantNumber(right);
					if (byNumber != 0)
					{
						return byNumber;
					}
					return left.getFileName().toString().compareToIgnoreCase(right.getFileName().toString());
				}
			});
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

	private static int variantNumber(Path path)
	{
		String stem = path.getFileName().toString();
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
			if (c == '*')
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
