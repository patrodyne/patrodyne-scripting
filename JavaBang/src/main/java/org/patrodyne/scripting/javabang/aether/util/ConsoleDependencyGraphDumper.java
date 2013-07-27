// PatroDyne: Patron Supported Dynamic Executables, http://patrodyne.org
// Released under LGPL license. See terms at http://www.gnu.org.
package org.patrodyne.scripting.javabang.aether.util;

import java.io.PrintStream;

import org.patrodyne.scripting.java.Console;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.graph.DependencyVisitor;

/**
 * A dependency visitor that dumps the graph to the console.
 */
public class ConsoleDependencyGraphDumper
	implements DependencyVisitor
{
	private Console console;
	private String currentIndent = "";

	public ConsoleDependencyGraphDumper()
	{
		console = Console.getStandard();
	}

	public ConsoleDependencyGraphDumper( PrintStream out )
	{
		console = new Console(out);
	}

	public boolean visitEnter( DependencyNode node )
	{
		if ( !"null".equals(node.toString()) )
			println( currentIndent + node );
		if ( currentIndent.length() <= 0 )
			currentIndent = "+- ";
		else
			currentIndent = "|	" + currentIndent;
		return true;
	}

	public boolean visitLeave( DependencyNode node )
	{
		if ( currentIndent.length() > 3)
			currentIndent = currentIndent.substring( 3, currentIndent.length() );
		else
			currentIndent = "";
		return true;
	}
	
	// Output an object to the standard console.
	protected void println(Object obj)
	{
		console.println(obj);
	}
}
// vi:set tabstop=4 hardtabs=4 shiftwidth=4:
