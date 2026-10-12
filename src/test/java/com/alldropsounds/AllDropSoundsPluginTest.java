package com.alldropsounds;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class AllDropSoundsPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(AllDropSoundsPlugin.class);
		RuneLite.main(args);
	}
}
