// PatroDyne: Patron Supported Dynamic Executables, http://patrodyne.org
// Released under LGPL license. See terms at http://www.gnu.org.
package org.patrodyne.scripting.javabang;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.eclipse.aether.RepositoryException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.patrodyne.scripting.java.Console;
import org.patrodyne.scripting.java.JavaCodeScriptEngine;
import org.patrodyne.scripting.java.JavaCodeScriptEngineFactory;
import org.patrodyne.scripting.java.MemoryClassLoader;
import org.patrodyne.scripting.java.ScriptReader;
import org.patrodyne.scripting.java.Verbose;
import org.patrodyne.scripting.javabang.aether.ResolveTransitiveDependencies;

/**
 * Execute a program using the JavaCode script engine
 * and use Maven/Aether to resolve dependencies.
 *
 * Declare locators for remote repositories using special
 * comments.
 *
 * <pre>
 * //@ http:/remote.repository.1
 * //@ http:/remote.repository.2
 * </pre>
 *
 * Declare the path to your local repository.
 *
 * <pre>
 * //: /path/to/local/repository
 * </pre>
 *
 * Dependencies are declared in the script using
 * Maven artifact coordinates.
 *
 * <pre>
 * //+ groupId:artifactId:version
 * //+ groupId:artifactId:packaging:version
 * //+ groupId:artifactId:packaging:classifier:version
 * </pre>
 *
 * Add more source file paths.
 *
 * <pre>
 * //& /absolute-path/AnotherSource.java
 * //& relative-path/AnotherSource.java
 * </pre>
 *
 * @author Rick O'Sullivan
 */
public class Execute implements ScriptReader
{
	// Directives
	public static String DIRECTIVE_COMMENT = org.patrodyne.scripting.java.Execute.DIRECTIVE_COMMENT;
	public static String DIRECTIVE_PROPERTY = org.patrodyne.scripting.java.Execute.DIRECTIVE_PROPERTY;
	public static String DIRECTIVE_LOCAL_REPOSITORY = DIRECTIVE_COMMENT+":";
	public static String DIRECTIVE_REMOTE_REPOSITORY = DIRECTIVE_COMMENT+"@";
	public static String DIRECTIVE_INCLUDE_DEPENDENCY = DIRECTIVE_COMMENT+"+";
	public static String DIRECTIVE_EXCLUDE_DEPENDENCY = DIRECTIVE_COMMENT+"-";
	public static String DIRECTIVE_INCLUDE_SOURCEPATH = DIRECTIVE_COMMENT+"&";

	// Directive Default Values
	private static String DEFAULT_LOCAL_REPOSITORY =
		System.getProperty("user.home")+"/.m2/repository";
	private static String DEFAULT_REMOTE_REPOSITORY = "http://repo1.maven.org/maven2/";

	private String localRepository;
	/**
	 * Get the location of the repository on the local file system used to
	 * cache contents of remote repositories and to store locally installed
	 * artifacts.
	 * @return The local repository.
	 */
	public String getLocalRepository()
	{
		if (localRepository == null)
			setLocalRepository(DEFAULT_LOCAL_REPOSITORY);
		return localRepository;
	}
	/**
	 * Set the location of the repository on the local file system used to
	 * cache contents of remote repositories and to store locally installed
	 * artifacts.
	 * @param localRepository The local repository to set.
	 */
	public void setLocalRepository(String localRepository)
	{
		this.localRepository = localRepository;
	}

	private List<String> remoteRepositories;
	/**
	 * Get the list of remote repositories used to download artifacts
	 * and dependencies of varying types.
	 * @return The list of remote repositories.
	 */
	public List<String> getRemoteRepositories()
	{
		if ( remoteRepositories == null )
			setRemoteRepositories(new ArrayList<String>());
		return remoteRepositories;
	}
	/**
	 * Set the list of remote repositories used to download artifacts
	 * and dependencies of varying types.
	 * @param remoteRepositories The list of remote repositories to set.
	 */
	public void setRemoteRepositories(List<String> remoteRepositories)
	{
		this.remoteRepositories = remoteRepositories;
	}

	private List<String> includeDependencies;
	/**
	 * Get the list of dependencies to include in the classpath, using
	 * the Maven coordinate form:
	 * <code>groupId:artifactId:packaging:classifier:version</code>.
	 * @return The list of dependencies to include in the classpath.
	 */
	public List<String> getIncludeDependencies()
	{
		if (includeDependencies == null)
			setIncludeDependencies(new ArrayList<String>());
		return includeDependencies;
	}
	/**
	 * Set the list of dependencies to include in the classpath, using
	 * the Maven coordinate form:
	 * <code>groupId:artifactId:packaging:classifier:version</code>.
	 * @param includeDependencies The list of included dependencies to set.
	 */
	public void setIncludeDependencies(List<String> includeDependencies)
	{
		this.includeDependencies = includeDependencies;
	}

	private List<String> excludeDependencies;
	/**
	 * Get the list of dependencies to exclude from the classpath, using
	 * the Maven coordinate form:
	 * <code>groupId:artifactId:packaging:classifier:version</code>.
	 * @return The list of dependencies to exclude in the classpath.
	 */
	public List<String> getExcludeDependencies()
	{
		if (excludeDependencies == null)
			setExcludeDependencies(new ArrayList<String>());
		return excludeDependencies;
	}
	/**
	 * Set the list of dependencies to exclude from the classpath, using
	 * the Maven coordinate form:
	 * <code>groupId:artifactId:packaging:classifier:version</code>.
	 * @param excludeDependencies The list of excluded dependencies to set.
	 */
	public void setExcludeDependencies(List<String> excludeDependencies)
	{
		this.excludeDependencies = excludeDependencies;
	}

	private List<String> includeSourcePaths;
	/**
	 * Get additional paths to search for source files.
	 * @return The list of included source paths.
	 */
	public List<String> getIncludeSourcePaths()
	{
		if ( includeSourcePaths == null )
			setIncludeSourcePaths(new ArrayList<String>());
		return includeSourcePaths;
	}
	/**
	 * Set additional paths to search for source files.
	 * @param includeSourcePaths The list of included source paths to set.
	 */
	public void setIncludeSourcePaths(List<String> includeSourcePaths)
	{
		this.includeSourcePaths = includeSourcePaths;
	}

	private Properties properties;
	/**
	 * Get properties for Maven/Aether configuration.
	 * @return The properties for Maven/Aether configuration.
	 */
	public Properties getProperties()
	{
		if ( properties == null )
			setProperties(new Properties());
		return properties;
	}
	/**
	 * Get properties for Maven/Aether configuration.
	 * @param properties The configuration properties to set.
	 */
	public void setProperties(Properties properties)
	{
		this.properties = properties;
	}

	private Boolean offline;
	/**
	 * Is the resolution mode offline?
	 * @return True when remote repos are not scanned; otherwise, false.
	 */
	public boolean isOffline()
	{
		if ( offline == null )
			offline = Boolean.parseBoolean(getProperties().getProperty("offline", "false"));
		return offline;
	}

	private Verbose verbose;
	/**
	 * Get the verbosity level.
	 * @return Govern the amount of feedback.
	 */
	public Verbose getVerbose()
	{
		if ( verbose == null )
		{
			try
			{
				verbose = Verbose.valueOf(getProperties().getProperty("verbose", "BRIEF").toUpperCase());
			}
			catch (IllegalArgumentException iae)
			{
				errorln("verbose options: "+Verbose.options(), iae);
			}
		}
		return verbose;
	}

	private String[] options;
	/**
	 * Get compiler options.
	 * @return The options for compiling classes.
	 */
	public String[] getOptions()
	{
		if ( options == null )
		{
			String property = getProperties().getProperty("options", null);
			options = ((property != null) && !property.isEmpty())
					? property.split("\\s+") : JavaCodeScriptEngine.EMPTY_STRING_ARRAY;
		}
		return options;
	}

	/**
	 * Entry point for command line invocation of the JavaCode
	 * Script Engine.
	 *
	 * @param args - command line options.
	 */
	public static void main(String[] args)
	{
		if ( args.length > 0 )
		{
			Execute executor = new Execute();
			executor.run(args);
		}
		else
			errorln("Usage: java -jar patrodyne-scripting-javabang-X.X.X.jar <filename> [args]");
	}

	/**
	 * Load a Java source script into a string, parse directives
	 * and skip shebang, when present.
	 *
	 * @param reader An I/O Reader bound to a Java source script.
	 *
	 * @return A string containing the source script.
	 * @throws ScriptException When the source cannot be loaded.
	 *
	 * @see org.patrodyne.scripting.java.ScriptReader#readScript(java.io.Reader)
	 */
	@Override
	public String loadScript(Reader reader)
		throws ScriptException
	{
		StringBuilder script = new StringBuilder();
		try (LineNumberReader lnr = new LineNumberReader(reader))
		{
			String line;
			while ( (line = lnr.readLine()) != null )
			{
				if (line.startsWith(SHEBANG))
					line = DIRECTIVE_COMMENT + line;
				else if (line.startsWith(DIRECTIVE_REMOTE_REPOSITORY))
					getRemoteRepositories().add(chop(line, DIRECTIVE_REMOTE_REPOSITORY));
				else if (line.startsWith(DIRECTIVE_LOCAL_REPOSITORY))
					setLocalRepository(chop(line, DIRECTIVE_LOCAL_REPOSITORY));
				else if (line.startsWith(DIRECTIVE_INCLUDE_DEPENDENCY))
					getIncludeDependencies().add(chop(line, DIRECTIVE_INCLUDE_DEPENDENCY));
				else if (line.startsWith(DIRECTIVE_EXCLUDE_DEPENDENCY))
					getExcludeDependencies().add(chop(line, DIRECTIVE_EXCLUDE_DEPENDENCY));
				else if (line.startsWith(DIRECTIVE_INCLUDE_SOURCEPATH))
					getIncludeSourcePaths().add(chop(line, DIRECTIVE_INCLUDE_SOURCEPATH));
				else if (line.startsWith(DIRECTIVE_PROPERTY))
				{
					String property = chop(line, DIRECTIVE_PROPERTY);
					String[] entry = property.split("=");
					if (entry.length > 1)
						getProperties().put(entry[0].toLowerCase().trim(), entry[1].trim());
				}
				// Append line to buffer.
				script.append(line+EOL);
			}
			if (getRemoteRepositories().isEmpty())
				getRemoteRepositories().add(DEFAULT_REMOTE_REPOSITORY);
		}
		catch (Exception ioe)
		{
			throw new ScriptException(ioe);
		}
		return script.toString();
	}

	// Chop off the directive's head.
	private String chop(String s, String head)
	{
		return s.substring(head.length()).trim();
	}

	// Run a program using the JavaCode script engine.
	private void run(String[] args)
	{
		// Create script file.
		File scriptFile = new File(args[0]);

		// Verify script exists.
		if ( scriptFile.exists() )
		{
			// Create a script engine manager and use this instance as the ScriptReader.
			ScriptEngineFactory factory = new JavaCodeScriptEngineFactory(this);

			// Create a script engine.
			ScriptEngine engine = factory.getScriptEngine();

			// Create a simple script context.
			ScriptContext ctx = new SimpleScriptContext();

			// Add filename to engine context.
			ctx.setAttribute(ScriptEngine.FILENAME, scriptFile.getName(), ScriptContext.ENGINE_SCOPE);

			// Add script arguments to engine context.
			if ( args.length > 1 )
			{
				String[] arguments = Arrays.copyOfRange(args, 1, args.length);
				ctx.setAttribute(ScriptEngine.ARGV, arguments, ScriptContext.ENGINE_SCOPE);
			}

			///////////////////////////////////////////
			// Read, load, resolve and evaluate script.
			///////////////////////////////////////////
			try (Reader reader = new FileReader(scriptFile))
			{
				// Load script into a string and parse directives.
				String script = loadScript(reader);

				// Set scripting context attribute to add a Main method.
				ctx.setAttribute(JavaCodeScriptEngine.ADDMAIN, getAddMain(), ScriptContext.ENGINE_SCOPE);

				// Set console mode.
				getConsole().setVerbose(getVerbose());

				// Resolve Transitive Dependencies
				ResolveTransitiveDependencies rtd = new ResolveTransitiveDependencies(this);
				List<ArtifactResult> artifactResults = rtd.execute();

				DynamicURLClassLoader ducl = new DynamicURLClassLoader();
				addClassPath(ducl);
				for ( ArtifactResult artifactResult : artifactResults )
					ducl.addURL(artifactResult.getArtifact().getFile().toURI().toURL());

				// Add scripting context attributes.
				ctx.setAttribute(JavaCodeScriptEngine.OPTIONS, getOptions(), ScriptContext.ENGINE_SCOPE);
				ctx.setAttribute(JavaCodeScriptEngine.CLASSPATH, classpath(artifactResults), ScriptContext.ENGINE_SCOPE);
				ctx.setAttribute(JavaCodeScriptEngine.PARENTLOADER, ducl, ScriptContext.ENGINE_SCOPE);

				// Execute script code using a file reader.
				engine.eval(script, ctx);
			}
			catch (RepositoryException rex)
			{
				errorln("cannot resolve dependencies", rex);
			}
			catch (ScriptException sex)
			{
				errorln("cannot evaluate script", sex);
			}
			catch (IOException ioe)
			{
				errorln("cannot read script", ioe);
			}
		}
		else
			errorln("script does not exist: "+scriptFile);
	}

	// Need to add the declared classpath to the DUCL,
	// so the dynamic artifacts can find resources (example: log4j.properties)
	private void addClassPath(DynamicURLClassLoader ducl)
	{
		String path =
			System.getProperty(JavaCodeScriptEngine.SYSPROP_PREFIX + JavaCodeScriptEngine.CLASSPATH);
		if ((path != null) && !path.isEmpty())
		{
			for (URL url : MemoryClassLoader.toURLs(path))
			{
				ducl.addURL(url);
				// For directories, add jars.
				if ("file".equals(url.getProtocol()))
				{
					File file = new File(url.getPath());
					if (file.isDirectory())
					{
						for (File jar : file.listFiles(classpathFilter))
						{
							try
							{
								ducl.addURL(jar.toURI().toURL());
							}
							catch (MalformedURLException mue)
							{
								errorln("Warning: classpath", mue);
							}
						}
					}
				}
			}
		}
	}

	private FileFilter classpathFilter = new FileFilter()
	{
		@Override
		public boolean accept(File pathname)
		{
			return pathname.isFile() &&
				pathname.getName().toLowerCase().endsWith(".jar");
		}
	};

	// Get the 'addmain' directive as a boolean.
	private boolean getAddMain()
	{
		return Boolean.parseBoolean(getProperties().getProperty(JavaCodeScriptEngine.ADDMAIN, "false"));
	}

	// Generate classpath from resolved artifacts.
	private String classpath(List<ArtifactResult> artifactResults)
	{
		String ps = System.getProperty("path.separator", ":");
		StringBuilder classpath = new StringBuilder();
		for (int index=0; index < artifactResults.size(); ++index)
		{
			ArtifactResult artifactResult = artifactResults.get(index);
			if ( artifactResult.isResolved() )
			{
				File artifactFile = artifactResult.getArtifact().getFile();
				classpath.append(artifactFile.getAbsolutePath());
				if ( (index+1) < artifactResults.size())
					classpath.append(ps);
			}
		}
		return classpath.toString();
	}

	// Get the console for this thread.
	private static Console getConsole()
	{
		return Console.getStandard();
	}

	// Output an object to the standard error console.
	protected static void errorln(Object obj)
	{
		getConsole().errorln(obj);
	}

	// Output an object and throwable to the standard error console.
	protected static void errorln(Object obj, Throwable err)
	{
		getConsole().errorln(obj, err);
	}
}
// vi:set tabstop=4 hardtabs=4 shiftwidth=4:
