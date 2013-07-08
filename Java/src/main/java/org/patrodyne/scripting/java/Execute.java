// PatroDyne: Patron Supported Dynamic Executables, http://patrodyne.org
// Released under LGPL license. See terms at http://www.gnu.org.
package org.patrodyne.scripting.java;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

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
	/**
	 * Entry point for command line invocation of the JavaCode
	 * Script Engine.
	 * 
	 * @param args - command line options.
	 * 
	 * @throws IOException When the source file cannot be read or closed.
	 * @throws ScriptException When the script contains errors.
	 */
    public static void main(String[] args) throws IOException
    {
    	if ( args.length > 0 )
    	{
    		Execute executor = new Execute();
    		executor.run(args);
    	}
    	else
    		println("Usage: java -jar patrodyne-scripting-java-X.X.X.jar <filename> [args]");
    }

    
	/** 
	 * Load a Java source script into a string, skip shebang, when present.
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
		char[] arr = new char[BLOCK_SIZE];
		StringBuilder buf = new StringBuilder();
		int numChars;
		try
		{
			while ((numChars = reader.read(arr, 0, arr.length)) > 0)
				buf.append(arr, 0, numChars);
		}
		catch (IOException exp)
		{
			throw new ScriptException(exp);
		}
		String script = buf.toString();
		
		// Skip SHEBANG line when present.
		if (script.startsWith(SHEBANG))
			script = script.substring(script.indexOf(EOL));
		return script;
	}
	
	// Run a program using the JavaCode script engine.
	private void run(String[] args)
		throws FileNotFoundException, IOException
	{
		// Create script file.
		File script = new File(args[0]);
		
		// Verify script exists.
		if ( script.exists() )
		{
			// Create a script engine manager and use this instance as the ScriptReader.
			ScriptEngineFactory factory = new JavaCodeScriptEngineFactory(this);
			
			// Create a script engine.
			ScriptEngine engine = factory.getScriptEngine();
			
			// Create a simple script context.
			ScriptContext ctx = new SimpleScriptContext();

			// Add filename to engine context.
			ctx.setAttribute(ScriptEngine.FILENAME, script.getName(), ScriptContext.ENGINE_SCOPE);
			
			// Add script arguments to engine context.
			if ( args.length > 1 )
			{
				String[] arguments = Arrays.copyOfRange(args, 1, args.length);
				ctx.setAttribute("arguments", arguments, ScriptContext.ENGINE_SCOPE);
			}
			
			// Execute script code using a file reader.
			Reader reader = null;
			try
			{
				reader = new FileReader(script);
				engine.eval(reader, ctx);
			}
			catch (ScriptException sex)
			{
				println("\n"+sex.getClass().getSimpleName()+": "+sex.getMessage()+"\n");
			}
			finally
			{
				if ( reader != null )
					reader.close();
			}
		}
		else
			println("script does not exist: "+script);
	}
    
    // Print an object to the standard error stream.
    private static void println(Object obj)
    {
    	if ( obj != null)
    		System.err.println(obj.toString());
    }
}
// vi:set tabstop=4 hardtabs=4 shiftwidth=4:
