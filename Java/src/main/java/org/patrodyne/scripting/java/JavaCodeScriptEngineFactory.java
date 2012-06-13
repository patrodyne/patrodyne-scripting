// PatroDyne: Patron Supported Dynamic Executables, http://patrodyne.org
// Released under LGPL license. See terms at http://www.gnu.org.
package org.patrodyne.scripting.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

/**
 * This is script engine factory for a "Java" script engine.
 *
 * Modified from the original to:
 * 
 * <ul>
 * <li>Format source.</li>
 * <li>Organize imports.</li>
 * <li>Changed short names to 'JavaCode' and 'Java' because BeanShell usurped 'java'.</li>
 * <li>Changed engine name to 'JavaCode'.</li>
 * <li>Use installed 'java.version' as the language version.</li>
 * <li>Added MIME type 'application/java'.</li>
 * <li>Changed class name prefix to 'org_patrodyne_scripting'</li>
 * <li>Change class name to differentiate from 'JavaScript'</li>
 * </ul>
 *
 * @author A. Sundararajan
 * @author Rick O'Sullivan
 */
public class JavaCodeScriptEngineFactory
	implements ScriptEngineFactory
{
	private static long nextClassNum = 0L;
	private static List<String> names;
	private static List<String> extensions;
	private static List<String> mimeTypes;
	static
	{
		names = new ArrayList<String>();
		names.add("JavaCode");
		names.add("Java");
		names = Collections.unmodifiableList(names);
		
		extensions = new ArrayList<String>();
		extensions.add("java");
		extensions = Collections.unmodifiableList(extensions);
		
		mimeTypes = new ArrayList<String>();
		mimeTypes.add("application/java");
		mimeTypes = Collections.unmodifiableList(mimeTypes);
	}
	
	/** Represents the engine name for metadata discovery. */
	public String getEngineName() {	return "JavaCode"; }

	/** Represents the engine version for metadata discovery. */
	public String getEngineVersion() { return "1.0.0"; }

	/** Represents the language name for metadata discovery. */
	public String getLanguageName() { return "java"; }

	/** Represents the language version for metadata discovery. */
	public String getLanguageVersion() { return System.getProperty("java.version"); }

	/** Represents the list of aliases used to discover the script engine. */
	public List<String> getNames() { return names; }

	/** Represents the list of file suffixes used to identify scripts. */
	public List<String> getExtensions() { return extensions; }

	/** Represents the list of MIME (content) types to discover the script engine. */
	public List<String> getMimeTypes() { return mimeTypes; }

	/**
	 * Returns a String which can be used to invoke a method of a Java object using 
	 * the syntax of the supported scripting language.
	 */
	public String getMethodCallSyntax(String obj, String m, String... args)
	{
		StringBuilder buf = new StringBuilder();
		buf.append(obj);
		buf.append(".");
		buf.append(m);
		buf.append("(");
		
		if (args.length != 0)
		{
			int i = 0;
			for (; i < args.length - 1; i++)
				buf.append(args[i] + ", ");
			buf.append(args[i]);
		}
		
		buf.append(")");
		return buf.toString();
	}

	/**
	 * Returns a String that can be used as a statement to display the specified 
	 * String using the syntax of the supported scripting language.
	 */
	public String getOutputStatement(String toDisplay)
	{
		StringBuilder buf = new StringBuilder();
		buf.append("System.out.print(\"");
		int len = toDisplay.length();
		
		for (int i = 0; i < len; i++)
		{
			char ch = toDisplay.charAt(i);
			switch (ch)
			{
				case '"':
					buf.append("\\\"");
					break;
				case '\\':
					buf.append("\\\\");
					break;
				default:
					buf.append(ch);
					break;
			}
		}
		buf.append("\");");
		return buf.toString();
	}

	/**
	 * Returns the value of an attribute whose meaning may be implementation-specific.
	 */
	public String getParameter(String key)
	{
		if (key.equals(ScriptEngine.ENGINE))
			return getEngineName();
		else if (key.equals(ScriptEngine.ENGINE_VERSION))
			return getEngineVersion();
		else if (key.equals(ScriptEngine.NAME))
			return getEngineName();
		else if (key.equals(ScriptEngine.LANGUAGE))
			return getLanguageName();
		else if (key.equals(ScriptEngine.LANGUAGE_VERSION))
			return getLanguageVersion();
		else if (key.equals("THREADING"))
			return "MULTITHREADED";
		else
			return null;
	}
	
	/**
	 * Returns a valid scripting language executable program with given statements.
	 * 
	 * Generates a Main class with main method that contains all the given statements.
	 */
	public String getProgram(String... statements)
	{
		StringBuilder buf = new StringBuilder();
		buf.append("class ");
		buf.append(getClassName());
		buf.append("\n{\n");
		buf.append("\tpublic static void main(String[] args)\n");
		buf.append("{\n");
		if (statements.length != 0)
		{
			for (int i = 0; i < statements.length; i++)
			{
				buf.append("\t");
				buf.append(statements[i]);
				buf.append(";\n");
			}
		}
		buf.append("\t}\n");
		buf.append("}\n");
		return buf.toString();
	}

	/**
	 * Returns an instance of the ScriptEngine associated with this ScriptEngineFactory.
	 */
	public ScriptEngine getScriptEngine()
	{
		JavaCodeScriptEngine engine = new JavaCodeScriptEngine();
		engine.setFactory(this);
		return engine;
	}

	// Generate a unique class name in getProgram().
	private String getClassName()
	{
		return "org_patrodyne_scripting_java_Main$" + getNextClassNumber();
	}

	// Increments a unique class counter.
	private static synchronized long getNextClassNumber()
	{
		return nextClassNum++;
	}
}
// vi:set tabstop=4 hardtabs=4 shiftwidth=4:
