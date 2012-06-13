// PatroDyne: Patron Supported Dynamic Executables, http://patrodyne.org
// Released under LGPL license. See terms at http://www.gnu.org.
package org.patrodyne.scripting.java;

import java.io.File;
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
public class Execute
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
    		// Create script file.
    		File script = new File(args[0]);
    		
    		// Verify script exists.
    		if ( script.exists() )
    		{
        		// Create a script engine manager.
    			ScriptEngineFactory factory = new JavaCodeScriptEngineFactory();
        		
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
        		Reader scriptReader = null;
        		try
        		{
            		scriptReader = new FileReader(script);
					engine.eval(scriptReader, ctx);
        		}
        		catch (ScriptException sex)
        		{
        			println("\n"+sex.getClass().getSimpleName()+": "+sex.getMessage()+"\n");
        		}
        		finally
        		{
        			if ( scriptReader != null )
        				scriptReader.close();
        		}
    		}
    		else
    			println("script does not exist: "+script);
    	}
    	else
    		println("Usage: java -jar patrodyne-scripting-java-X.X.X.jar <filename> [args]");
    }
    
    // Print an object to the standard error stream.
    private static void println(Object obj)
    {
    	if ( obj != null)
    		System.err.println(obj.toString());
    }
}
// vi:set tabstop=4 hardtabs=4 shiftwidth=4:
