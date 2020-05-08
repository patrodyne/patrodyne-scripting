// PatroDyne: Patron Supported Dynamic Executables, http://patrodyne.org
// Released under LGPL license. See terms at http://www.gnu.org.
// Modified from Sonatype, Inc. examples.
package org.patrodyne.scripting.javabang.aether.manual;

import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.providers.http.LightweightHttpWagon;
import org.apache.maven.wagon.providers.http.LightweightHttpsWagon;
import org.eclipse.aether.connector.wagon.WagonProvider;

/**
 * A simplistic provider for wagon instances when no Plexus-compatible IoC container is used.
 */
public class ManualWagonProvider
	implements WagonProvider
{
	@Override
	public Wagon lookup( String roleHint )
		throws Exception
	{
//		LightweightHttpWagonAuthenticator authenticator = new LightweightHttpWagonAuthenticator();
		if ( "https".equals( roleHint ) )
		{
			LightweightHttpsWagon wagon = new LightweightHttpsWagon();
//			wagon.setAuthenticator(authenticator);
			return wagon;
		}
		else if ( "http".equals( roleHint ) )
		{
			LightweightHttpWagon wagon =  new LightweightHttpWagon();
//			wagon.setAuthenticator(authenticator);
			return wagon;
		}
		else
			return null;
	}

	@Override
	public void release( Wagon wagon )
	{
		// No action
	}
}
// vi:set tabstop=4 hardtabs=4 shiftwidth=4:
