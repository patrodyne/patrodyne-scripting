// PatroDyne: Patron Supported Dynamic Executables, http://patrodyne.org
// Released under LGPL license. See terms at http://www.gnu.org.
package org.patrodyne.scripting.java;

import java.io.Reader;

import javax.script.ScriptException;

/**
 * An interface to abstract the method used to load the Java
 * source script into a string.
 * 
 * @author Rick O'Sullivan
 */
public interface ScriptReader
{
	/** The number of characters to read per I/O block. */
	public static int BLOCK_SIZE = 8 * 1024;

	/**
	 * As the leading characters in a script, indicates when
	 * the first line is consumed by a Linux shell interpreter
	 * and is to be ignored by the ScriptEngine.
	 */
	public static final String SHEBANG = "#!";
	
	/** End of Line character. */
	public static final String EOL = "\n";
		
	/**
	 * Load a script into string.
	 * 
	 * @param reader An I/O Reader bound to a Java source script.
	 * @return A string containing the source script.
	 * @throws ScriptException When the source cannot be loaded.
	 */
	public abstract String loadScript(Reader reader)
		throws ScriptException;
}
// vi:set tabstop=4 hardtabs=4 shiftwidth=4:
