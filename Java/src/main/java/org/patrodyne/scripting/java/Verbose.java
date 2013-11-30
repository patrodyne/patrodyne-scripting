// PatroDyne: Patron Supported Dynamic Executables, http://patrodyne.org
// Released under LGPL license. See terms at http://www.gnu.org.
package org.patrodyne.scripting.java;

/**
 * Verbosity levels.
 * @author Rick O'Sullivan
 */
public enum Verbose
{
	BRIEF(0), DEBUG(1), TRACE(2);

	// Represents the verbosity level.
	private int level = 0;
	/**
	 * Get the verbosity level.
	 * @return The verbosity level.
	 */
	public int getLevel()
	{
		return level;
	}
	/**
	 * Set the verbosity level.
	 * @param level The verbosity level.
	 */
	private void setLevel(int level)
	{
		this.level = level;
	}

	private Verbose(int level)
	{
		setLevel(level);
	}
	
	public boolean atLeast(Verbose level)
	{
		return getLevel() >= level.getLevel();
	}
	
	public static String options()
	{
		StringBuilder options = new StringBuilder();
		for (Verbose value : values())
		{
			if ( options.length() == 0 )
				options.append(value);
			else
				options.append(", ").append(value);
		}
		return options.toString();
	}
	
}
// vi:set tabstop=4 hardtabs=4 shiftwidth=4:
