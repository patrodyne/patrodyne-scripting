// PatroDyne: Patron Supported Dynamic Executables, http://patrodyne.org
// Released under LGPL license. See terms at http://www.gnu.org.
// Modified from Sonatype, Inc. examples.
package org.patrodyne.scripting.javabang.aether.util;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.patrodyne.scripting.java.Console;
import org.eclipse.aether.transfer.AbstractTransferListener;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transfer.TransferResource;

/**
 * A simplistic transfer listener that logs uploads/downloads to the console.
 */
public class ConsoleTransferListener
	extends AbstractTransferListener
{
	private Console console;
	
	private Map<TransferResource, Long> downloads = new ConcurrentHashMap<TransferResource, Long>();

	public ConsoleTransferListener()
	{
		console = Console.getStandard();
	}

	public ConsoleTransferListener( PrintStream out )
	{
		console = new Console(out);
	}

	@Override
	public void transferInitiated( TransferEvent event )
	{
		TransferResource resource = event.getResource();
		String type = ( event.getRequestType() == TransferEvent.RequestType.PUT ? "Uploading" : "Downloading" );
		println( type + ": " + resource.getRepositoryUrl() + resource.getResourceName() );
		println("   10%  20   30   40   50   60   70   80   90  100");
		println("----+----+----+----+----+----+----+----+----+----+");
	}

	@Override
	public void transferProgressed( TransferEvent event )
	{
		// For the current event, put the resource and bytes into the map.
		TransferResource resource = event.getResource();
		long eventBytes = Long.valueOf(event.getTransferredBytes());
		downloads.put( resource, eventBytes);

		// Accumulate bytes received and total bytes expected.
		long total = 0L, bytes = 0L;
		for ( Map.Entry<TransferResource, Long> entry : downloads.entrySet() )
		{
			total += entry.getKey().getContentLength();
			bytes += entry.getValue();
		}
		
		// Calculate percentage of bytes received over total bytes expected.
		double percent = (total > 0) ? (double) bytes / (double) total : 0.0;

		// Calculate dots to be displayed on a 50 unit bar.
		int dots = (int) (50.0 * percent + 0.5);
		
		if ( dots > 0 )
		{
			StringBuilder dotbar = new StringBuilder();
			for (int dot=0; dot < dots; ++dot)
				dotbar.append("*");
			
			// Append carriage return (overwrite) and print a dot bar.
			dotbar.append("\r");
			print(dotbar);
		}
	}

	@Override
	public void transferSucceeded( TransferEvent event )
	{
		transferCompleted( event );

		TransferResource resource = event.getResource();
		long contentLength = event.getTransferredBytes();
		if ( contentLength >= 0 )
		{
			String type = ( event.getRequestType() == TransferEvent.RequestType.PUT ? "Uploaded" : "Downloaded" );
			String len = contentLength >= 1024 ? toKB( contentLength ) + " KB" : contentLength + " B";

			String throughput = "";
			long duration = System.currentTimeMillis() - resource.getTransferStartTime();
			if ( duration > 0 )
			{
				DecimalFormat format = new DecimalFormat( "0.0", new DecimalFormatSymbols( Locale.ENGLISH ) );
				double kbPerSec = ( contentLength / 1024.0 ) / ( duration / 1000.0 );
				throughput = " at " + format.format( kbPerSec ) + " KB/sec";
			}

			println( type + ": " + resource.getRepositoryUrl() + resource.getResourceName() + " (" + len
				+ throughput + ")" );
		}
	}

	@Override
	public void transferFailed( TransferEvent event )
	{
		transferCompleted( event );

		event.getException().printStackTrace( console.getOutput() );
	}

	private void transferCompleted( TransferEvent event )
	{
		println("");
		downloads.remove( event.getResource() );
	}

	public void transferCorrupted( TransferEvent event )
	{
		event.getException().printStackTrace( console.getOutput() );
	}

	protected long toKB( long bytes )
	{
		return ( bytes + 1023 ) / 1024;
	}
	
	// Output an object to the standard console without a line terminator.
	protected void print(Object obj)
	{
		console.print(obj);
	}
	
	// Output an object to the standard console with a line terminator.
	protected void println(Object obj)
	{
		console.println(obj);
	}
}
// vi:set tabstop=4 hardtabs=4 shiftwidth=4:

