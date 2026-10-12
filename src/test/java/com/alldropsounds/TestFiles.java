package com.alldropsounds;

import java.nio.file.Path;
import net.runelite.client.util.Filepath;

final class TestFiles
{
	private TestFiles()
	{
	}

	static Filepath of(Path path)
	{
		return Filepath.Unchecked.getRooted(path);
	}
}
