// PatroDyne: Patron Supported Dynamic Executables, http://patrodyne.org
// Released under LGPL license. See terms at http://www.gnu.org.
package org.patrodyne.scripting.javabang.aether.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.patrodyne.scripting.javabang.aether.manual.ManualRepositorySystemFactory;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;

/**
 * A factory to boot the repository system and a repository system session.
 */
public class RepositorySystemFactory
{
	// Represents the standard WagonRepositoryConnector type.
	private static final String WAGON_CONNECTOR_TYPE = "default";
	
	private String localRepository;
	/**
	 * Get the location of the repository on the local file system used to 
	 * cache contents of remote repositories and to store locally installed
	 * artifacts.
	 * @return The local repository.
	 */
	public String getLocalRepository()
	{
		return localRepository;
	}
	/*
	 * Set the location of the repository on the local file system used to 
	 * cache contents of remote repositories and to store locally installed
	 * artifacts.
	 * @param localRepository The local repository to set.
	 */
	private void setLocalRepository(String localRepository)
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
		return remoteRepositories;
	}
	/*
	 * Set the list of remote repositories used to download artifacts
	 * and dependencies of varying types.
	 * @param remoteRepositories The list of remote repositories to set.
	 */
	private void setRemoteRepositories(List<String> remoteRepositories)
	{
		this.remoteRepositories = remoteRepositories;
	}
	
	/**
	 * Construct factory with repository configurations.
	 * 
	 * @param localRepository The local repository path.
	 * @param remoteRepositories The remote repository locations.
	 */
	public RepositorySystemFactory(String localRepository, List<String> remoteRepositories)
	{
		super();
		setLocalRepository(localRepository);
		setRemoteRepositories(remoteRepositories);
	}
	
	/**
	 * Produce a new RepositorySystem instance that employs Aether's 
	 * built-in service locator infrastructure to wire up the system's components.
	 * 
	 * @return An instance of Aether's repository system.
	 */
	public RepositorySystem newRepositorySystem()
	{
		return ManualRepositorySystemFactory.newRepositorySystem();
	}

	/**
	 * Produce a new session instance for the RepositorySystem.
	 * 
	 * @param system An instance of the RepositorySystem.
	 * 
	 * @return A session containing settings and components for a RepositorySystem.
	 */
	public RepositorySystemSession newRepositorySystemSession( RepositorySystem system )
	{
		MavenRepositorySystemSession session = new MavenRepositorySystemSession();

		LocalRepository localRepo = new LocalRepository( getLocalRepository() );
		session.setLocalRepositoryManager( system.newLocalRepositoryManager( localRepo ) );

		// Add console listeners.
		session.setTransferListener( new ConsoleTransferListener() );
		session.setRepositoryListener( new ConsoleRepositoryListener() );

		// uncomment to generate dirty trees
		// session.setDependencyGraphTransformer( null );

		return session;
	}

	/**
	 * Produce a new list of new RemoteRepository instances.
	 * @return A new list of new RemoteRepository instances.
	 */
	public List<RemoteRepository> newRemoteRepositories()
	{
		List<RemoteRepository> repositories = new ArrayList<RemoteRepository>();
		int id = 0;
		for (String repository : getRemoteRepositories())
		{
			repositories.add(new RemoteRepository( "RemoteRepository-"+(id++), 
				WAGON_CONNECTOR_TYPE, repository ));
		}
		return repositories;
	}
}
// vi:set tabstop=4 hardtabs=4 shiftwidth=4:

