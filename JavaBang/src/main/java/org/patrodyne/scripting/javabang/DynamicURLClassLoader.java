// PatroDyne: Patron Supported Dynamic Executables, http://patrodyne.org
// Released under LGPL license. See terms at http://www.gnu.org.
package org.patrodyne.scripting.javabang;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * This dynamic class loader extends {@link URLClassLoader} to expose
 * the addURL(URL) method for public access. It assumes the system
 * class loader is a URLClassLoader. This could fail if there is a
 * SecurityManager.
 *
 * It is used to load classes and resources from a search path of URLs
 * referring to both JAR files and directories. Any URL that ends with
 * a '/' is assumed to refer to a directory. Otherwise, the URL is
 * assumed to refer to a JAR file which will be opened as needed.
 *
 * @author Rick O'Sullivan
 */
public class DynamicURLClassLoader
	extends URLClassLoader
{
	/**
	 * Construct a new DynanmicURLClassLoader from the system ClassLoader.
	 * Assumes that the system class loader is a URLClassLoader.
	 */
	public DynamicURLClassLoader()
	{
		this(new URLClassLoader(new URL[0], ClassLoader.getPlatformClassLoader()));
	}

	/**
	 * Constructs a new DynanmicURLClassLoader for the specified
	 * <code>URLClassLoader</code>.
	 *
	 * @param urlClassLoader The class loader providing existing URLs.
	 */
	public DynamicURLClassLoader(URLClassLoader urlClassLoader)
	{
		super(urlClassLoader.getURLs());
	}

	/**
	 * Constructs a new DynanmicURLClassLoader for the given URLClassLoader.
	 *
	 * @param urlClassLoader The class loader providing existing URLs.
	 * @param parent The parent class loader for delegation
	 */
	public DynamicURLClassLoader(URLClassLoader urlClassLoader, ClassLoader parent)
	{
		super(urlClassLoader.getURLs(), parent);
	}

	/**
	 * Appends the specified URL to the list of URLs to search for
	 * classes and resources.
	 * <p>
	 * If the URL specified is <code>null</code> or is already in the
	 * list of URLs, or if this loader is closed, then invoking this
	 * method has no effect.
	 *
	 * @param url The locator to be added to the search path of URLs
	 */
	@Override
	public void addURL(URL url)
	{
		super.addURL(url);
	}
}
// vi:set tabstop=4 hardtabs=4 shiftwidth=4:
