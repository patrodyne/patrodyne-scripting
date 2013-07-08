// PatroDyne: Patron Supported Dynamic Executables, http://patrodyne.org
// Released under LGPL license. See terms at http://www.gnu.org.
package org.patrodyne.scripting.java;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Map;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

/**
 * This is script engine for Java programming language.
 * 
 * Modified from the original to:
 * 
 * <ul>
 * <li>Format source.</li>
 * <li>Organize imports.</li>
 * <li>Parameterize raw types.</li>
 * <li>Change class name to differentiate from 'JavaScript'</li>
 * <li>Change system property prefix to 'org.patrodyne.scripting'</li>
 * </ul>
 * 
 * @author A. Sundararajan
 * @author Rick O'Sullivan
 */
public class JavaCodeScriptEngine
	extends AbstractScriptEngine
	implements Compilable, ScriptReader
{
	private JavaCompiler compiler;
	/**
	 * Get or create the compiler.
	 * @return A Java compiler.
	 */
	protected JavaCompiler getCompiler()
	{
		if ( compiler == null)
			compiler = new JavaCompiler();
		return compiler;
	}

	private ScriptEngineFactory factory;
	/**
	 * Get or create the script engine factory.
	 * @return A Java script engine factory.
	 */
	public ScriptEngineFactory getFactory()
	{
		synchronized (this)
		{
			if (factory == null)
				setFactory(new JavaCodeScriptEngineFactory());
		}
		return factory;
	}
	/**
	 * Set the script engine factory.
	 * @param factory A script engine factory.
	 */
	protected void setFactory(ScriptEngineFactory factory)
	{
		this.factory = factory;
	}

	private ScriptReader scriptReader;
	/**
	 * Get the object used to load a Java source script.
	 * Default is this JavaCodeScriptEngine instance.
	 * @return the scriptReader
	 */
	public ScriptReader getScriptReader()
	{
		if ( scriptReader == null )
			scriptReader = this;
		return scriptReader;
	}
	/**
	 * Set the object used to load a Java source script.
	 * @param scriptReader the scriptReader to set
	 */
	public void setScriptReader(ScriptReader scriptReader)
	{
		this.scriptReader = scriptReader;
	}

	// Java implementation for CompiledScript
	private class JavaCompiledScript
		extends CompiledScript
	{
		private Class<?> clazz;

		protected JavaCompiledScript(Class<?> clazz)
		{
			this.clazz = clazz;
		}

		public ScriptEngine getEngine()
		{
			return JavaCodeScriptEngine.this;
		}

		public Object eval(ScriptContext ctx)
			throws ScriptException
		{
			return evalClass(clazz, ctx);
		}
	}

	/**
	 * Compiles the script (source represented as a String) for later execution.
	 */
	public CompiledScript compile(String script)
		throws ScriptException
	{
		Class<?> clazz = parse(script, context);
		return new JavaCompiledScript(clazz);
	}

	/**
	 * Compiles the script (source read from Reader) for later execution.
	 */
	public CompiledScript compile(Reader reader)
		throws ScriptException
	{
		return compile(getScriptReader().loadScript(reader));
	}

	/**
	 * Causes the immediate execution of the script whose 
	 * source is the String passed as the first argument.
	 */
	public Object eval(String str, ScriptContext ctx)
		throws ScriptException
	{
		Class<?> clazz = parse(str, ctx);
		return evalClass(clazz, ctx);
	}

	/**
	 * Causes the immediate execution of the script whose 
	 * source is the Reader passed as the first argument.
	 */
	public Object eval(Reader reader, ScriptContext ctx)
		throws ScriptException
	{
		return eval(getScriptReader().loadScript(reader), ctx);
	}

	/**
	 * Returns an uninitialized Bindings.
	 */
	public Bindings createBindings()
	{
		return new SimpleBindings();
	}

	// Internals only below this point
	
	// Parse source with the given context which may contain:
	// 1) A source name.
	// 2) A source path.
	// 3) A class path.
	private Class<?> parse(String source, ScriptContext ctx)
		throws ScriptException
	{
		String sourceName = getFileName(ctx);
		String sourcePath = getSourcePath(ctx);
		String classPath = getClassPath(ctx);
		
		Map<String, byte[]> memoryMap = 
			getCompiler().compile(sourceName, source, ctx.getErrorWriter(), sourcePath, classPath);
		
		if (memoryMap == null )
		{
			try
			{
				ctx.getErrorWriter().write("RECOVERY> Auto wrapping source in a 'main' method.\n");
			}
			catch (IOException e)
			{
				// Oh, well.
			}
			source = getFactory().getProgram(source.split("[\\r\\n]+"));
			memoryMap= getCompiler().compile(sourceName, source, ctx.getErrorWriter(), sourcePath, classPath);
		}
		
		if (memoryMap == null)
			throw new ScriptException("compilation failed");
		
		// create a ClassLoader to load classes from MemoryJavaFileManager
		MemoryClassLoader loader = 
			new MemoryClassLoader(memoryMap, classPath, getParentLoader(ctx));
		try
		{
			try
			{
				String mainClassName = getMainClassName(ctx);
				if (mainClassName != null)
				{
					Class<?> clazz = loader.load(mainClassName);
					Method mainMethod = findMainMethod(clazz);
					if (mainMethod == null)
						throw new ScriptException("no main method in " + mainClassName);
					return clazz;
				}
				
				// No main class configured - load all compiled classes
				Iterable<Class<?>> classes = loader.loadAll();
				// search for class with main method
				Class<?> c = findMainClass(classes);
				if (c != null)
					return c;
				else
				{
					// if no class with "main" method, then return first class
					 Iterator<Class<?>> itr = classes.iterator();
					 if (itr.hasNext())
						return itr.next();
					 else
						return null;
				}
			}
			finally
			{
				loader.close();
			}
		}
		catch (Exception ex)
		{
			throw new ScriptException(ex);
		}
	}

	private static Class<?> findMainClass(Iterable<Class<?>> classes)
	{
		// find a public class with public static main method
		for (Class<?> clazz : classes)
		{
			int modifiers = clazz.getModifiers();
			if (Modifier.isPublic(modifiers))
			{
				Method mainMethod = findMainMethod(clazz);
				if (mainMethod != null)
					return clazz;
			}
		}
		
		// okay, try to find package private class that
		// has public static main method
		for (Class<?> clazz : classes)
		{
			Method mainMethod = findMainMethod(clazz);
			if (mainMethod != null)
				return clazz;
		}
		
		// no main class found!
		return null;
	}

	// find public static void main(String[]) method, if any
	private static Method findMainMethod(Class<?> clazz)
	{
		try
		{
			Method mainMethod = clazz.getMethod("main", new Class[] { String[].class });
			int modifiers = mainMethod.getModifiers();
			if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers))
				return mainMethod;
		}
		catch (NoSuchMethodException nsme)
		{
		}
		return null;
	}

	// find public static void setScriptContext(ScriptContext) method, if any
	private static Method findSetScriptContextMethod(Class<?> clazz)
	{
		try
		{
			Method setCtxMethod = 
				clazz.getMethod("setScriptContext", new Class[] { ScriptContext.class });
			int modifiers = setCtxMethod.getModifiers();
			if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers))
				return setCtxMethod;
		}
		catch (NoSuchMethodException nsme)
		{
		}
		return null;
	}

	// Get source name from the first of:
	// 1) ScriptContext: javax.script.filename
	// 2) "$unnamed.java"
	private static String getFileName(ScriptContext ctx)
	{
		int scope = ctx.getAttributesScope(ScriptEngine.FILENAME);
		if (scope != -1)
			return ctx.getAttribute(ScriptEngine.FILENAME, scope).toString();
		else
			return "$unnamed.java";
	}

	// for certain variables, we look for System properties. This is
	// the prefix used for such System properties
	private static final String SYSPROP_PREFIX = "org.patrodyne.scripting.java.";
	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	private static final String ARGUMENTS = "arguments";

	// Get source path from the first of:
	// 1) ScriptContext: arguments
	private static String[] getArguments(ScriptContext ctx)
	{
		int scope = ctx.getAttributesScope(ARGUMENTS);
		if (scope != -1)
		{
			Object obj = ctx.getAttribute(ARGUMENTS, scope);
			if (obj instanceof String[])
				return (String[]) obj;
		}
		// return zero length array
		return EMPTY_STRING_ARRAY;
	}

	// Get source path from the first of:
	// 1) ScriptContext: sourcepath
	// 2) -Dorg.patrodyne.scripting.java.sourcepath
	private static final String SOURCEPATH = "sourcepath";
	private static String getSourcePath(ScriptContext ctx)
	{
		int scope = ctx.getAttributesScope(SOURCEPATH);
		if (scope != -1)
			return ctx.getAttribute(SOURCEPATH).toString();
		else
			return System.getProperty(SYSPROP_PREFIX + SOURCEPATH);
	}

	// Get class path from the first of:
	// 1) ScriptContext: classpath
	// 2) -Dorg.patrodyne.scripting.java.classpath
	// 3) -Djava.class.path
	private static final String CLASSPATH = "classpath";
	private static String getClassPath(ScriptContext ctx)
	{
		int scope = ctx.getAttributesScope(CLASSPATH);
		if (scope != -1)
			return ctx.getAttribute(CLASSPATH).toString();
		else
		{
			String res = System.getProperty(SYSPROP_PREFIX + CLASSPATH);
			if (res == null)
				res = System.getProperty("java.class.path");
			return res;
		}
	}

	private static final String MAINCLASS = "mainClass";
	private static String getMainClassName(ScriptContext ctx)
	{
		int scope = ctx.getAttributesScope(MAINCLASS);
		if (scope != -1)
			return ctx.getAttribute(MAINCLASS).toString();
		else
		{
			// look for "org.patrodyne.scripting.java.mainClass"
			return System.getProperty(SYSPROP_PREFIX + MAINCLASS);
		}
	}

	private static final String PARENTLOADER = "parentLoader";
	private static ClassLoader getParentLoader(ScriptContext ctx)
	{
		int scope = ctx.getAttributesScope(PARENTLOADER);
		if (scope != -1)
		{
			Object loader = ctx.getAttribute(PARENTLOADER);
			if (loader instanceof ClassLoader)
			{
				return (ClassLoader) loader;
			} // else fall through..
		}
		return null;
	}

	private static Object evalClass(Class<?> clazz, ScriptContext ctx)
		throws ScriptException
	{
		// JSR-223 requirement
		ctx.setAttribute("context", ctx, ScriptContext.ENGINE_SCOPE);
		if (clazz == null)
			return null;
		
		try
		{
			boolean isPublicClazz = Modifier.isPublic(clazz.getModifiers());
			
			// find the setScriptContext method
			Method setCtxMethod = findSetScriptContextMethod(clazz);
			
			// call setScriptContext and pass current ctx variable
			if (setCtxMethod != null)
			{
				if (!isPublicClazz)
				{
					// try to relax access
					setCtxMethod.setAccessible(true);
				}
				setCtxMethod.invoke(null, new Object[] { ctx });
			}
			
			// find the main method
			Method mainMethod = findMainMethod(clazz);
			if (mainMethod != null)
			{
				if (!isPublicClazz)
				{
					// try to relax access
					mainMethod.setAccessible(true);
				}
				
				// get "command line" args for the main method
				String[] args = getArguments(ctx);
				
				// call main method
				mainMethod.invoke(null, new Object[] { args });
			}
			
			// return main class as eval's result
			return clazz;
		}
		catch (Exception exp)
		{
			throw new ScriptException(exp);
		}
	}

	/** 
	 * Load a Java source script into a string.
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
		return buf.toString();
	}
}
// vi:set tabstop=4 hardtabs=4 shiftwidth=4:
