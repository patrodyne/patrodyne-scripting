// PatroDyne: Patron Supported Dynamic Executables, http://patrodyne.org
// Released under LGPL license. See terms at http://www.gnu.org.
// Modified from Sonatype, Inc. examples.
package org.patrodyne.scripting.javabang.aether;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.aether.RepositoryException;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import org.patrodyne.scripting.java.Verbose;
import org.patrodyne.scripting.javabang.Execute;
import org.patrodyne.scripting.javabang.aether.util.ConsoleDependencyGraphDumper;
import org.patrodyne.scripting.javabang.aether.util.RepositorySystemFactory;

/**
 * Resolves the transitive (compile) dependencies of an artifact.
 */
public class ResolveTransitiveDependencies
{
	private Execute context;
	/**
	 * Get the context properties.
	 * @return The context properties.
	 */
	public Execute getContext()
	{
		return context;
	}
	/**
	 * Set the context properties.
	 * @param context The context properties to set.
	 */
	private void setContext(Execute context)
	{
		this.context = context;
	}

	/**
	 * Construct with the context for execution.
	 *
	 * @param context The context to execute.
	 */
	public ResolveTransitiveDependencies(Execute context)
	{
		super();
		setContext(context);
	}

	/**
	 * Execute context to Resolve Transitive Dependencies.
	 * @return A list of artifact results.
	 * @throws RepositoryException When there are unresolvable dependencies.
	 */
	public List<ArtifactResult> execute() throws RepositoryException
	{
		// A factory to boot the repository system and a repository system session.
		RepositorySystemFactory factory = new RepositorySystemFactory
			(
				getContext().getLocalRepository(),
				getContext().getRemoteRepositories(),
				getContext().isOffline(),
				getContext().getVerbose().atLeast(Verbose.TRACE)
			);

		// The main entry point to the repository system.
		RepositorySystem system = factory.newRepositorySystem();

		// Defines settings and components that control the repository system.
		RepositorySystemSession session = factory.newRepositorySystemSession( system );

		// A list of dependencies to resolve.
		List<Dependency> dependencies = new ArrayList<Dependency>();
		for (String dependency : getContext().getIncludeDependencies())
		{
			// A specific artifact with the specified coordinates.
			// If not specified in the artifact coordinates, the
			// artifact's extension defaults to {@code jar} and
			// classifier to an empty string.
			Artifact artifact = new DefaultArtifact( dependency );
			// Set the artifact scope to compile and run the script.
			dependencies.add(new Dependency( artifact, JavaScopes.COMPILE ));
		}

		// List of remote repositories for artifact resolution.
		// In offline mode, this list is used to verify remote availability.
		List<RemoteRepository> remoteRepositories = factory.newRemoteRepositories();

		// Create a request to collect the transitive dependencies and to build a dependency graph.
		Dependency root = null;
		CollectRequest collectRequest = new CollectRequest(root, dependencies, remoteRepositories);

		// A filter to include/exclude dependency nodes during other operations.
		// Set the artifact scope to compile and run the script.
		DependencyFilter classpathFlter =
			DependencyFilterUtils.classpathFilter( JavaScopes.COMPILE );

		// A request to resolve transitive dependencies.
		DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, classpathFlter);

		// Resolve and collect the transitive dependencies of an artifact.
		// Dump Console Dependency Graph
		DependencyResult dependencyResult = system.resolveDependencies(session, dependencyRequest);
		List<ArtifactResult> artifactResults = dependencyResult.getArtifactResults();
		dependencyResult.getRoot().accept(new ConsoleDependencyGraphDumper());

		return artifactResults;
	}
}
// vi:set tabstop=4 hardtabs=4 shiftwidth=4:
