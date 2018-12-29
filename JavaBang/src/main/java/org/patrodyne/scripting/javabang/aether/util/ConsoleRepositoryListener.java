// PatroDyne: Patron Supported Dynamic Executables, http://patrodyne.org
// Released under LGPL license. See terms at http://www.gnu.org.
// Modified from Sonatype, Inc. examples.
package org.patrodyne.scripting.javabang.aether.util;

import java.io.PrintStream;

import org.patrodyne.scripting.java.Console;
import org.eclipse.aether.AbstractRepositoryListener;
import org.eclipse.aether.RepositoryEvent;

/**
 * A simplistic repository listener that logs events to the console.
 */
public class ConsoleRepositoryListener
	extends AbstractRepositoryListener
{
	private Console console;

	public ConsoleRepositoryListener()
	{
		console = Console.getStandard();
	}

	public ConsoleRepositoryListener( PrintStream out )
	{
		console = new Console(out);
	}

	public void artifactDeployed( RepositoryEvent event )
	{
		println( "Deployed " + event.getArtifact() + " to " + event.getRepository() );
	}

	public void artifactDeploying( RepositoryEvent event )
	{
		println( "Deploying " + event.getArtifact() + " to " + event.getRepository() );
	}

	public void artifactDescriptorInvalid( RepositoryEvent event )
	{
		println( "Invalid artifact descriptor for " + event.getArtifact() + ": "
			+ event.getException().getMessage() );
	}

	public void artifactDescriptorMissing( RepositoryEvent event )
	{
		println( "Missing artifact descriptor for " + event.getArtifact() );
	}

	public void artifactInstalled( RepositoryEvent event )
	{
		println( "Installed " + event.getArtifact() + " to " + event.getFile() );
	}

	public void artifactInstalling( RepositoryEvent event )
	{
		println( "Installing " + event.getArtifact() + " to " + event.getFile() );
	}

	public void artifactResolved( RepositoryEvent event )
	{
		println( "Resolved artifact " + event.getArtifact() + " from " + event.getRepository() );
	}

	public void artifactDownloading( RepositoryEvent event )
	{
		println( "Downloading artifact " + event.getArtifact() + " from " + event.getRepository() );
	}

	public void artifactDownloaded( RepositoryEvent event )
	{
		println( "Downloaded artifact " + event.getArtifact() + " from " + event.getRepository() );
	}

	public void artifactResolving( RepositoryEvent event )
	{
		println( "Resolving artifact " + event.getArtifact() );
	}

	public void metadataDeployed( RepositoryEvent event )
	{
		println( "Deployed " + event.getMetadata() + " to " + event.getRepository() );
	}

	public void metadataDeploying( RepositoryEvent event )
	{
		println( "Deploying " + event.getMetadata() + " to " + event.getRepository() );
	}

	public void metadataInstalled( RepositoryEvent event )
	{
		println( "Installed " + event.getMetadata() + " to " + event.getFile() );
	}

	public void metadataInstalling( RepositoryEvent event )
	{
		println( "Installing " + event.getMetadata() + " to " + event.getFile() );
	}

	public void metadataInvalid( RepositoryEvent event )
	{
		println( "Invalid metadata " + event.getMetadata() );
	}

	public void metadataResolved( RepositoryEvent event )
	{
		println( "Resolved metadata " + event.getMetadata() + " from " + event.getRepository() );
	}

	public void metadataResolving( RepositoryEvent event )
	{
		println( "Resolving metadata " + event.getMetadata() + " from " + event.getRepository() );
	}
	
	// Output an object to the standard console.
	protected void println(Object obj)
	{
		console.println(obj);
	}
}
// vi:set tabstop=4 hardtabs=4 shiftwidth=4:
