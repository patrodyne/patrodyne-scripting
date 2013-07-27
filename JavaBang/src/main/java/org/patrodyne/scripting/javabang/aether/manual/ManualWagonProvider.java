package org.patrodyne.scripting.javabang.aether.manual;
/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *	 http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.providers.http.LightweightHttpWagon;
import org.sonatype.aether.connector.wagon.WagonProvider;

/**
 * A simplistic provider for wagon instances when no Plexus-compatible IoC container is used.
 */
public class ManualWagonProvider
	implements WagonProvider
{
	public Wagon lookup( String roleHint )
		throws Exception
	{
		if ( "http".equals( roleHint ) )
			return new LightweightHttpWagon();
		else
			return null;
	}

	public void release( Wagon wagon )
	{
		// No action
	}
}
// vi:set tabstop=4 hardtabs=4 shiftwidth=4:
