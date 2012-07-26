// PatroDyne: Patron Supported Dynamic Executables, http://patrodyne.org
// Released under LGPL license. See terms at http://www.gnu.org.
package org.patrodyne.scripting.java;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.script.ScriptException;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * Simple interface to Java compiler using JSR 199 Compiler API.
 * 
 * Modified from the original to:
 * 
 * <ul>
 * <li>Format source.</li>
 * <li>Organize imports.</li>
 * <li>Parameterize raw types.</li>
 * <li>Reference makeStringSource statically.</li>
 * </ul>
 * 
 * @author A. Sundararajan
 * @author Rick O'Sullivan
 */
public class JavaCompiler
{
	private javax.tools.JavaCompiler systemJavaCompiler;
	/**
	 * Gets the Java™ programming language compiler provided with this platform.
	 * 
	 * @return The compiler provided with this platform or null if no compiler is provided
	 * @throws ScriptException When the System does not provide a Java Compiler.
	 */
	protected javax.tools.JavaCompiler getSystemJavaCompiler() throws ScriptException
	{
		if ( systemJavaCompiler == null )
		{
			systemJavaCompiler = ToolProvider.getSystemJavaCompiler();
			if ( systemJavaCompiler == null )
				throw new ScriptException("Java compiler not available. Are you using a JRE instead of a JDK?");
		}
		return systemJavaCompiler;
	}

	private StandardJavaFileManager standardFileManager;
	/**
	 * Get the standard Java file manager implementation for this tool. 
	 * The file manager will use the given diagnostic listener for 
	 * producing any non-fatal diagnostics. Fatal errors will be 
	 * signaled with the appropriate exceptions.
	 * 
	 * @return A Java file manager tool.
	 * @throws ScriptException When the System does not provide a Java Compiler.
	 */
	protected StandardJavaFileManager getStandardFileManager() throws ScriptException
	{
		if ( standardFileManager == null)
			standardFileManager = getSystemJavaCompiler().getStandardFileManager(null, null, null);
		return standardFileManager;
	}

	/**
	 * Compile given source string and return byte codes as a memory map.
	 * 
	 * @param fileName source fileName to be used for error messages etc.
	 * @param source Java source as String
	 * 
	 * @return A memory map of class name and byte code pairs.
	 * @throws ScriptException When the System does not provide a Java Compiler.
	 */
	public Map<String, byte[]> compile(String fileName, String source) throws ScriptException
	{
		PrintWriter err = new PrintWriter(System.err);
		return compile(source, fileName, err, null, null);
	}

	/**
	 * Compile given source string and return byte codes as a memory map.
	 * 
	 * @param fileName source fileName to be used for error messages etc.
	 * @param source Java source as String
	 * @param err error writer where diagnostic messages are written
	 * 
	 * @return A memory map of class name and byte code pairs.
	 * @throws ScriptException When the System does not provide a Java Compiler.
	 */
	public Map<String, byte[]> compile(String fileName, String source, Writer err) throws ScriptException
	{
		return compile(fileName, source, err, null, null);
	}

	/**
	 * Compile given source string and return byte codes as a memory map.
	 * 
	 * @param fileName source fileName to be used for error messages etc.
	 * @param source Java source as String
	 * @param err error writer where diagnostic messages are written
	 * @param sourcePath location of additional .java source files
	 * 
	 * @return A memory map of class name and byte code pairs.
	 * @throws ScriptException When the System does not provide a Java Compiler.
	 */
	public Map<String, byte[]> compile(String fileName, String source, Writer err, String sourcePath) throws ScriptException
	{
		return compile(fileName, source, err, sourcePath, null);
	}

	/**
	 * <p>Compile given source string and return byte codes as a memory map.</p>
	 * 
	 * <p>If the compilation fails, diagnostics are sent to the standard error stream.</p>
	 * 
	 * @param sourceName The file name to identify the source in diagnostics, etc.
	 * @param source The string containing the source to be compiled
	 * @param err The error writer where diagnostic messages are written.
	 * @param sourcePath The location of additional .java source files.
	 * @param classPath The location of additional .class files.
	 * 
	 * @return A memory map of class name and byte code pairs or null when compilation fails.
	 * @throws ScriptException When the System does not provide a Java Compiler.
	 */
	public Map<String, byte[]> compile(String sourceName, String source, Writer err, String sourcePath, String classPath) throws ScriptException
	{
		// A) Create a new memory JavaFileManager
		MemoryJavaFileManager<StandardJavaFileManager> javaFileManager = 
			new MemoryJavaFileManager<StandardJavaFileManager>(getStandardFileManager());
		
		// B) Create diagnostics to collect errors, warnings etc.
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		
		// C) Java compiler options
		List<String> options = createCompilerOptions(sourcePath, classPath);
		
		// D) Prepare the compilation unit
		//    1) create a list of JavaFileObjects
		//    2) create a JavaFileObject instance for the current source name and code.
		//    3) Add the instance to the list.
		List<JavaFileObject> javaFileObjects = new ArrayList<JavaFileObject>(1);
		javaFileObjects.add(MemoryJavaFileManager.createJavaFileObject(sourceName, source));
		
		PrintWriter perr = null;
		if ( err instanceof PrintWriter )
			perr = (PrintWriter) err;
		else if ( err != null)
			perr = new PrintWriter(err);
		else 
			perr = new PrintWriter(System.err);
		
		// Create a compilation task.
		CompilationTask compilationTask = 
			getSystemJavaCompiler().getTask(perr, javaFileManager, diagnostics, options, null, javaFileObjects);
		
        // Performs this compilation task. 
		// True, if and only if, all the files compiled without errors.
		Map<String, byte[]> memoryMap = null;
		if (compilationTask.call())
		{
			memoryMap = javaFileManager.getMemoryMap();
			try
			{
				javaFileManager.close();
			}
			catch (IOException exp)
			{
				perr.println(exp.getClass().getName()+": "+exp.getMessage());
				perr.flush();
			}
		}
		else
		{
			for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics())
				report(diagnostic, perr);
			perr.flush();
		}
		
		// Return compilation results or null.
		return memoryMap;
	}

	/**
	 * Report a compiler diagnostic to the given print writer.
	 * 
	 * @param dg A complier diagnostic.
	 * @param pw A print writer.
	 */
	protected void report(Diagnostic<?> dg, PrintWriter pw)
	{
		pw.println();
		pw.println(dg.getKind()+"> Line="+dg.getLineNumber()+", Column="+dg.getColumnNumber());
		pw.println("Message> "+dg.getMessage(null));
//		pw.println("Line="+dg.getLineNumber()+", Column="+dg.getColumnNumber());
//		pw.println("Start=" + dg.getStartPosition()+", At="+dg.getPosition()+", End="+dg.getEndPosition());
	}
	
	/**
	 *  <p>Create Java compiler options for the given source path and class path.</p>
	 *  
	 *  <p>Options include:</p>
	 *  <ul>
	 *  <li><code>-Xlint:all</code> - enable all recommended warnings.</li>
	 *  <li><code>-g:lines,vars</code> - enable debugging for line numbers and local variables.</li>
	 *  <li><code>-sourcepath</code> - colon separated list of paths for source code resolution.</li>
	 *  <li><code>-classpath</code> - colon separated list of paths or jars for class resolution.</li>
	 *  </ul>
	 *  
	 *  @param sourcePath A colon separated list of paths for source code resolution.
	 *  @param classPath A colon separated list of paths or jars for class resolution.
	 *  
	 *  @return A list of compiler options as individual strings.
	 */
	protected List<String> createCompilerOptions(String sourcePath, String classPath)
	{
		List<String> options = new ArrayList<String>();
		
		// enable all recommended warnings.
		options.add("-Xlint:all");
		
		// enable debugging for line numbers and local variables.
		options.add("-g:lines,vars");
		
		// Specify  the  source  code  path  to search for class or interface definitions. 
		// As with the user class path, source path entries are separated by colons (:) and 
		// can be directories, JAR archives, or ZIP archives. If  packages  are  used,  the
        // local path name within the directory or archive must reflect the package name.
		if (sourcePath != null)
		{
			options.add("-sourcepath");
			options.add(sourcePath);
		}
		
		// Class path entries are separated by colons (:) and can be directories, JAR archives, 
		// ZIP archives or .class files. Each classpath entry should end with a filename or
		// directory; otherwise, the entry will be ignored.
		if (classPath != null)
		{
			options.add("-classpath");
			options.add(classPath);
		}
		return options;
	}
}
// vi:set tabstop=4 hardtabs=4 shiftwidth=4:
