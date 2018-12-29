// PatroDyne: Patron Supported Dynamic Executables, http://patrodyne.org
// Released under LGPL license. See terms at http://www.gnu.org.
// Modified from Sonatype, Inc. examples.
package org.patrodyne.scripting.javabang.aether.manual;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.connector.file.FileRepositoryConnectorFactory;
import org.eclipse.aether.connector.wagon.WagonProvider;
import org.eclipse.aether.connector.wagon.WagonRepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;

/**
 * A factory for repository system instances that employs Aether's
 * built-in service locator infrastructure to wire up the system's components.
 */
public class ManualRepositorySystemFactory
{
	public static RepositorySystem newRepositorySystem()
	{
		/*
		 * Aether's components implement org.eclipse.aether.spi.locator.Service
		 * to ease manual wiring and using the pre-populated
		 * DefaultServiceLocator, we only need to register the repository
		 * connector factories.
		 */
		DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();

		// Add service for local file system repository connections.
		locator.addService( RepositoryConnectorFactory.class, FileRepositoryConnectorFactory.class );

		// Add service for remote repository connections.
		locator.addService( RepositoryConnectorFactory.class, WagonRepositoryConnectorFactory.class );

		// Add a component to acquire and release wagon instances for uploads/downloads.
		locator.setServices( WagonProvider.class, new ManualWagonProvider() );

		RepositorySystem system = locator.getService( RepositorySystem.class );
		System.err.println("system...: "+system);
		return system;
	}
}
// vi:set tabstop=4 hardtabs=4 shiftwidth=4:

