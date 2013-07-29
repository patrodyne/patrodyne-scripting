#!./javabang
//@ http://repo1.maven.org/maven2/
//= target/local/repository
//+ org.apache.commons:commons-lang3:3.1
//: offline=false
//: verbose=false
//: debug=false

import org.apache.commons.lang3.*;

public class HelloWorld
{
	public static void main(String[] args)
	{
		println("------------");
		println("Hello World!");
		println("------------");
		for ( int i=0; i < args.length; ++i)
			println("arg #"+i+": "+args[i]);

		println("");
		println("Java Runtime");
		println("  Name....: " + SystemUtils.JAVA_RUNTIME_NAME);
		println("  Version.: " + SystemUtils.JAVA_RUNTIME_VERSION);
		println("");
		println("Java Virtual Machine");
		println("  Name....: " + SystemUtils.JAVA_VM_NAME);
		println("  Version.: " + SystemUtils.JAVA_VM_VERSION);
		println("  Vendor..: " + SystemUtils.JAVA_VM_VENDOR);
		println("  Info....: " + SystemUtils.JAVA_VM_INFO);
		println("");
		println("Java Virtual Machine Specification");
		println("  Name....: " + SystemUtils.JAVA_VM_SPECIFICATION_NAME);
		println("  Version.: " + SystemUtils.JAVA_VM_SPECIFICATION_VERSION);
		println("  Vendor..: " + SystemUtils.JAVA_VM_SPECIFICATION_VENDOR);
		println("");
		println("Operating System");
		println("  Name.........: " + SystemUtils.OS_NAME);
		println("  Version......: " + SystemUtils.OS_VERSION);
		println("  Architecture.: " + SystemUtils.OS_ARCH);
	}

	public static void println(String text)
	{
		System.out.println(text);
	}
}
// vi:set tabstop=4 hardtabs=4 shiftwidth=4:
