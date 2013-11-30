// PatroDyne: Patron Supported Dynamic Executables, http://patrodyne.org
// Released under LGPL license. See terms at http://www.gnu.org.
package org.patrodyne.scripting.java;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Prints formatted representations of objects to a text-output output.
 * Printing is suppressed by default, using a the verbose mode; but errors
 * are always printed.
 * 
 * @author Rick O'Sullivan
 */
public class Console
{
	// Represents the standard console per thread.
	private static final ThreadLocal<Console> standard = 
		new ThreadLocal<Console>()
		{
	       @Override protected Console initialValue() { return new Console(); }
	    };

	/**
	 * Get this thread's standard console.
	 * @return This thread's standard console.
	 */
	public static Console getStandard()
	{
		return standard.get();
	}
	    
	private PrintStream output;
	/**
	 * Get the stream for output.
	 * @return the output
	 */
	public PrintStream getOutput()
	{
		if (output == null)
			output = System.err;
		return output;
	}
	/**
	 * Set the stream for output.
	 * @param output the output to set
	 */
	public void setOutput(PrintStream stream)
	{
		this.output = stream;
	}

	private Verbose verbose = Verbose.BRIEF;
	/**
	 * A verbosity level to allow detailed output. The default is brief.
	 * @return The verbose level.
	 */
	public Verbose getVerbose()
	{
		return verbose;
	}
	/**
	 * Set verbose level to allow detailed output.
	 * @param verbose the verbose flag for detailed output.
	 */
	public void setVerbose(Verbose verbose)
	{
		this.verbose = verbose;
	}
	
	/** Construct with the default output stream, System.err. */
	public Console()
	{
		this(System.err);
	}
	
	/**
	 * Construct with a PrintStream.
	 * @param output The stream to output to.
	 */
	public Console(PrintStream output)
	{
		setOutput(output);
	}
	
	/**
	 * Print an object using its toString() method.
	 * Verbose mode allows printing.
	 * Null objects are not printed.
	 * @param obj The object to print.
	 */
	public void print(Object obj)
	{
		if ( (getVerbose().atLeast(Verbose.DEBUG)) && (obj != null) )
			getOutput().print(obj.toString());
	}
	
	/**
	 * Print an object using its toString() method and terminate the line.
	 * Verbose mode allows printing.
	 * Null objects are not printed.
	 * @param obj The object to print.
	 */
	public void println(Object obj)
	{
		if ( (getVerbose().atLeast(Verbose.DEBUG)) && (obj != null) )
			getOutput().println(obj.toString());
	}

	/**
	 * Print an error object using its toString() method and terminate the line.
	 * Verbose mode is ignored, errors are always printed.
	 * @param err The error object or message.
	 */
	public void errorln(Object err)
	{
		error(err);
		getOutput().println();
	}
	
	/**
	 * Print an error object using its toString() method.
	 * Verbose mode is ignored, errors are always printed.
	 * @param err The error object or message.
	 */
	public void error(Object err)
	{
		if ( err != null)
		{
			if ( err instanceof Throwable )
			{
				// Collect causes
				Throwable tprev = null;
				Throwable tnext = (Throwable) err;
				List<String> causes = new ArrayList<String>();
				do
				{
					causes.add(tnext.getClass().getSimpleName()+"[\""+tnext.getMessage()+"\"]");
					tprev = tnext;
					tnext = tprev.getCause();
				} while ( (tnext != null) && (tnext != tprev) );
				// Output causes in reverse order.
				for ( int index=causes.size()-1; index >= 0; --index)
				{
					if ( index > 0 )
						getOutput().print(causes.get(index) + " > ");
					else
						getOutput().print(causes.get(index));
				}
			}
			else
				getOutput().print(err.toString());
		}
		else
			getOutput().print("ERROR: No information.");
	}

	/**
	 * Print an error using the string representation of an 
	 * object and a of a throwable.
	 * In non-verbose mode, only the error message is output.
	 * @param err A informative object or message.
	 * @param cause The thrown exception. 
	 */
	public void errorln(Object err, Throwable cause)
	{
		if ( err != null )
		{
			if ((getVerbose().atLeast(Verbose.DEBUG)))
				errorln(err);
			else
			{
				if ( cause != null )
					error(err + " > ");
				else
					error(err);
			}
		}
		if ( (getVerbose().atLeast(Verbose.DEBUG)) && (cause != null) )
			cause.printStackTrace(getOutput());
		errorln(cause);
	}
}
// vi:set tabstop=4 hardtabs=4 shiftwidth=4:
