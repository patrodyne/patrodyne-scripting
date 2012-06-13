import java.net.*;
import java.util.*;
import javax.script.*;

public class Utility
{  
	protected static void dump(ScriptContext ctx)
	{
		println("");
		println("-- Script Context -------------------------");
		for ( Integer scope : ctx.getScopes())
		{
			println("Scope: "+scope);
			Bindings bindings = ctx.getBindings(scope);
			if ( bindings != null )
			{
				for (String key : bindings.keySet())
					println("\t"+key+"="+bindings.get(key));
			}
		}
		println("-- Script Context -------------------------");
	}

	protected static void dumpClasspath(ClassLoader loader)
	{
		println("\tClassloader " + loader + ":");

		if (loader instanceof URLClassLoader)
		{
			URLClassLoader ucl = (URLClassLoader)loader;
			println("\t" + Arrays.toString(ucl.getURLs()));
		}
		else
			println("\t(cannot display components as not a URLClassLoader)");

		if (loader.getParent() != null)
			dumpClasspath(loader.getParent());
	}

	protected static void println(Object obj)
	{
		if ( obj != null )
			System.out.println(obj.toString());
	}
}
// vi:set tabstop=4 hardtabs=4 shiftwidth=4:
