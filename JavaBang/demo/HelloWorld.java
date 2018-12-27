import java.net.*;
import java.util.*;
import javax.script.*;

class HelloWorld
{  
    public static void setScriptContext(ScriptContext ctx)
	{
		Utility.dump(ctx);
	}

	public static void main(String[] args)
	{
		println("");
		println("-- Main -----------------------------------");
		println("Hello World!");
		println("Arguments");
		for (int argno=0;  argno < args.length; ++argno)
			println("\t"+argno+": "+args[argno]);
		println("Classpath");
		Utility.dumpClasspath(HelloWorld.class.getClassLoader());
		println("Resource URL: "+HelloWorld.class.getResource("/resource.txt"));
		println("-- Main -----------------------------------");
		println("");
	}

	private static void println(Object obj)
	{
		Utility.println(obj);
	}
}
// vi:set tabstop=4 hardtabs=4 shiftwidth=4:
