#!/bin/sh
mvn clean package exec:java \
	-Dorg.patrodyne.scripting.java.sourcepath="demo" \
	-Dorg.patrodyne.scripting.java.classpath="demo" \
	-Dexec.mainClass="org.patrodyne.scripting.java.Execute" \
	-Dexec.args="demo/HelloWorld.java arg0 arg1 arg2"
# vi:set tabstop=4 hardtabs=4 shiftwidth=4:
