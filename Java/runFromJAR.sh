#!/bin/sh
java \
	-Dorg.patrodyne.scripting.java.sourcepath="demo" \
	-Dorg.patrodyne.scripting.java.classpath="demo" \
	-jar target/patrodyne-scripting-java-1.0.0-SNAPSHOT.jar \
	demo/HelloWorld.java "arg0" "arg1" "arg2"
# vi:set tabstop=4 hardtabs=4 shiftwidth=4:
