// PatroDyne: Patron Supported Dynamic Executables, http://patrodyne.org
// Released under LGPL license. See terms at http://www.gnu.org.
package org.patrodyne.scripting.java;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Properties;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

/**
 * Execute a program using the JavaCode script engine.
 *
 * @author Rick O'Sullivan
 */
public class Execute implements ScriptReader
{
	// Directives
	public static String DIRECTIVE_COMMENT = "//";
	public static String DIRECTIVE_PROPERTY = DIRECTIVE_COMMENT+"=";

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

	// Get the 'addmain' directive as a boolean.
	private boolean getAddMain()
	{
		return Boolean.parseBoolean(getProperties().getProperty(JavaCodeScriptEngine.ADDMAIN, "false"));
	}

	private Verbose verbose;
	/**
	 * Get the verbosity level.
	 * @return Govern the amount of feedback.
	 */
	public Verbose getVerbose()
	{
		if ( verbose == null )
			verbose = Verbose.valueOf(getProperties().getProperty("verbose", "BRIEF").toUpperCase());
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
    		errorln("Usage: java -jar patrodyne-scripting-java-X.X.X.jar <filename> [args]");
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

				// Add scripting context attributes.
				ctx.setAttribute(JavaCodeScriptEngine.OPTIONS, getOptions(), ScriptContext.ENGINE_SCOPE);

				// Execute script code using a file reader.
				engine.eval(script, ctx);
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
