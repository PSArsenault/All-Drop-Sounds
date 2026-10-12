package com.alldropsounds;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

/**
 * Clears a settings checkbox after RuneLite has left it ticked.
 * The label and the box are siblings in the settings row.
 */
public final class SettingsCheckbox
{
	private SettingsCheckbox()
	{
	}

	public static void clear(String label)
	{
		for (Window window : Window.getWindows())
		{
			clear(window, label);
		}
	}

	static boolean clear(Component component, String label)
	{
		boolean cleared = false;
		if (component instanceof JLabel && label.equals(((JLabel) component).getText()))
		{
			cleared = clearSiblingBox(component.getParent());
		}
		if (component instanceof Container)
		{
			for (Component child : ((Container) component).getComponents())
			{
				if (clear(child, label))
				{
					cleared = true;
				}
			}
		}
		return cleared;
	}

	private static boolean clearSiblingBox(Container parent)
	{
		if (parent == null)
		{
			return false;
		}
		boolean cleared = false;
		for (Component child : parent.getComponents())
		{
			if (child instanceof JCheckBox)
			{
				((JCheckBox) child).setSelected(false);
				cleared = true;
			}
		}
		return cleared;
	}
}
