// PatroDyne: Patron Supported Dynamic Executables, http://patrodyne.org
// Released under LGPL license. See terms at http://www.gnu.org.
package org.patrodyne.scripting.java;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.StringTokenizer;

/**
 * <p>An extension of URLClassLoader to store and load classes from
 * memory.</p>
 * 
 * <p>This class loader is constructed with a map of class
 * name and byte code pairs for in-memory loading.</p>
 * 
 * <p>Modified from the original to:</p>
 * 
 * <ul>
 * <li>Format source.</li>
 * <li>Organize imports.</li>
 * <li>Parameterize raw types.</li>
 * </ul>
 * 
 * @author A. Sundararajan
 * @author Rick O'Sullivan
 */
public final class MemoryClassLoader
	extends URLClassLoader
{
	private Map<String, byte[]> memoryMap;
	/**
	 * Get a map of class names and byte code.
	 * @return A map of class names and byte code.
	 */
	protected Map<String, byte[]> getMemoryMap()
	{
		return memoryMap;
	}
	/**
	 * Set a map of class names and byte code.
	 * @param memoryMap A map of class names and byte code.
	 */
	private void setMemoryMap(Map<String, byte[]> memoryMap)
	{
		this.memoryMap = memoryMap;
	}

	/**
	 * Construct with class map, path and parent loader.
	 * 
	 * @param memoryMap An in-memory map of class names and byte code.
	 * @param classPath Directories and jars for class loading.
	 * @param parentLoader The parent class loader for delegation.
	 */
	public MemoryClassLoader(Map<String, byte[]> memoryMap, String classPath, ClassLoader parentLoader)
	{
		super(toURLs(classPath), parentLoader);
		setMemoryMap(memoryMap);
	}

	/**
	 * Construct with class map and path.
	 * 
	 * @param memoryMap An in-memory map of class names and byte code.
	 * @param classPath Directories and jars for class loading.
	 */
	public MemoryClassLoader(Map<String, byte[]> memoryMap, String classPath)
	{
		this(memoryMap, classPath, null);
	}

	/**
	 * Load a class by name from the memory class map.
	 * 
	 * @return A class from the memory class map. 
	 * 
	 * @throws ClassNotFoundException When a class name cannot be found.
	 */
	public Class<?> load(String className)
		throws ClassNotFoundException
	{
		// loadClass(className) relies on findClass(String className) as implemented in this class.
		return loadClass(className);
	}

	/**
	 * Load all classes from the memory class map.
	 * 
	 * @return A iterator for all classes in the memory class map. 
	 * 
	 * @throws ClassNotFoundException When a class name cannot be found.
	 */
	public Iterable<Class<?>> loadAll()
		throws ClassNotFoundException
	{
		List<Class<?>> classes = new ArrayList<Class<?>>(getMemoryMap().size());
		// loadClass(className) relies on findClass(String className) as implemented in this class.
		for (String className : getMemoryMap().keySet())
			classes.add(loadClass(className));
		return classes;
	}

	/**
	 * Find class by class name. If the class name is in the
	 * memory class map, the associated byte code is converted
	 * into a Class and returned; otherwise, the search is
	 * performed as a URLClassLoader.
	 * 
	 * @return A Class for the given class name.
	 */
	protected Class<?> findClass(String className)
		throws ClassNotFoundException
	{
		byte[] bytecode = getMemoryMap().get(className);
		if (bytecode != null)
		{
			// clear the byte code in map -- we don't need it anymore
			getMemoryMap().put(className, null);
			return defineClass(className, bytecode, 0, bytecode.length);
		}
		else
			return super.findClass(className);
	}

	// Convert a class path to an array of URLs.
	// A null class path returns an empty array.
	private static URL[] toURLs(String classPath)
	{
		if (classPath == null)
			return new URL[0];
		
		List<URL> urls = new ArrayList<URL>();
		StringTokenizer st = new StringTokenizer(classPath, File.pathSeparator);
		while (st.hasMoreTokens())
		{
			String token = st.nextToken();
			File file = new File(token);
			if (file.exists())
			{
				try
				{
					urls.add(file.toURI().toURL());
				}
				catch (MalformedURLException mue)
				{
					// Skip this file.
				}
			}
			else
			{
				try
				{
					urls.add(new URL(token));
				}
				catch (MalformedURLException mue)
				{
					// Skip this token.
				}
			}
		}
		// Convert the list to an array of URLs,
		return urls.toArray(new URL[urls.size()]);
	}
}
// vi:set tabstop=4 hardtabs=4 shiftwidth=4:
