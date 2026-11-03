package com.alldropsounds;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Pets and other untradeable uniques that have no Grand Exchange price.
 * Item definitions do not mark pets, so a pet is recognized from its inventory name.
 * The set is every collection-log pet plus inventory names used by metamorphs.
 * A name that starts with "Pet " or ends with " (pet)" is a pet without being listed.
 * A listed name followed by " (variant)" is that pet too.
 * Items that exist to metamorphose a pet use the pet sound as well.
 */
public final class UniqueItems
{
	private static final Set<String> PETS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
		"abyssal orphan",
		"abyssal protector",
		"aggy",
		"akkhito",
		"baby chinchompa",
		"baby mole",
		"baby mole-rat",
		"babi",
		"baron",
		"beaver",
		"beef",
		"bloodhound",
		"bone squirrel",
		"bran",
		"butch",
		"callisto cub",
		"chaos elemental jr.",
		"chompy chick",
		"corrupted youngllef",
		"dark squirrel",
		"dom",
		"elidinis' damaged guardian",
		"elidinis' guardian",
		"enraged tektiny",
		"flying vespina",
		"giant squirrel",
		"great blue heron",
		"greatish guardian",
		"gull",
		"gulliver",
		"hellpuppy",
		"herbi",
		"heron",
		"huberte",
		"ikkle hydra",
		"jal-nib-rek",
		"jalrek-jad",
		"kalphite princess",
		"kephriti",
		"lil' creator",
		"lil' destructor",
		"lil' zik",
		"lil'viathan",
		"little nightmare",
		"little parasite",
		"maggot marquess",
		"midnight",
		"moxi",
		"mr mcgroot",
		"muphin",
		"nexling",
		"nid",
		"noon",
		"olmlet",
		"orion",
		"phoenix",
		"prince black dragon",
		"puppadile",
		"quetzin",
		"rax",
		"red",
		"ric",
		"rift guardian",
		"rock golem",
		"rocky",
		"scorpia's offspring",
		"scurry",
		"skotos",
		"smol heredit",
		"smolcano",
		"soup",
		"sraracha",
		"tangleroot",
		"tektiny",
		"tiny tempor",
		"tumeken's damaged guardian",
		"tumeken's guardian",
		"tzrek-jad",
		"tzrek-zuk",
		"vasa minirio",
		"vanguard",
		"venenatis spiderling",
		"vespina",
		"vet'ion jr.",
		"vorki",
		"wisp",
		"yami",
		"youngllef",
		"zebo",
		"ziggy"
	)));

	private static final Set<String> MORPH_ITEMS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
		"ancient remnant",
		"blue egg sac",
		"calcified acorn",
		"charged ice",
		"coagulated venom",
		"dark acorn",
		"fox whistle",
		"funky shaped log",
		"golden pheasant egg",
		"guardian's eye",
		"metamorphic dust",
		"orange egg sac",
		"parasitic egg",
		"remnant of akkha",
		"remnant of ba-ba",
		"remnant of kephri",
		"remnant of zebak",
		"sanguine dust"
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
		if (isPet(key) || isMorphItem(key))
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
		if (isPet(key) || isMorphItem(key) || key.startsWith("jar of ") || key.contains("champion"))
		{
			return true;
		}
		return !tradeable && gePrice <= 0 && haPrice <= 0 && key.contains("unique");
	}

	private static boolean isPet(String key)
	{
		if (key.startsWith("pet ") || key.endsWith(" (pet)"))
		{
			return true;
		}
		if (PETS.contains(key))
		{
			return true;
		}
		for (String pet : PETS)
		{
			if (key.startsWith(pet + " ("))
			{
				return true;
			}
		}
		return false;
	}

	private static boolean isMorphItem(String key)
	{
		if (MORPH_ITEMS.contains(key))
		{
			return true;
		}
		for (String item : MORPH_ITEMS)
		{
			if (key.startsWith(item + " ("))
			{
				return true;
			}
		}
		return false;
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
