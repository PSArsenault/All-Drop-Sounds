package com.alldropsounds;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Pets and other untradeable uniques that have no Grand Exchange price.
 */
public final class UniqueItems
{
	private static final Set<String> NAMES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
		"abyssal orphan",
		"baby mole",
		"bloodhound",
		"callisto cub",
		"chaos elemental jr.",
		"chompy chick",
		"hellpuppy",
		"herbi",
		"ikkle hydra",
		"jal-nib-rek",
		"kalphite princess",
		"lil' zik",
		"little nightmare",
		"nexling",
		"noon",
		"olmlet",
		"prince black dragon",
		"rift guardian",
		"rocky",
		"scorpia's offspring",
		"skotos",
		"smolcano",
		"sraracha",
		"tiny tempor",
		"tzrek-jad",
		"venenatis spiderling",
		"vet'ion jr.",
		"youngllef"
	)));

	private UniqueItems()
	{
	}

	public static String categoryStem(String name)
	{
		String key = normalize(name);
		if (key == null)
		{
			return null;
		}
		if (key.startsWith("pet ") || NAMES.contains(key))
		{
			return "pets";
		}
		if (key.startsWith("jar of "))
		{
			return "jars";
		}
		if (key.contains("champion"))
		{
			return "champions";
		}
		return null;
	}

	public static boolean isUnique(String name, boolean tradeable, long gePrice, long haPrice)
	{
		String key = normalize(name);
		if (key == null)
		{
			return false;
		}
		if (key.startsWith("pet ") || NAMES.contains(key) || key.startsWith("jar of ") || key.contains("champion"))
		{
			return true;
		}
		return !tradeable && gePrice <= 0 && haPrice <= 0 && key.contains("unique");
	}

	private static String normalize(String name)
	{
		String cleaned = ItemNames.clean(name);
		if (cleaned == null)
		{
			return null;
		}
		return cleaned.replaceAll("\\s+", " ").toLowerCase(Locale.ENGLISH);
	}
}
