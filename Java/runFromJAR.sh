#!/bin/sh
if [ -r target ]; then
	java \
		-Dorg.patrodyne.scripting.java.sourcepath="demo" \
		-Dorg.patrodyne.scripting.java.classpath="demo" \
		-jar target/patrodyne-scripting-java-*.jar \
		demo/HelloWorld.java "arg0" "arg1" "arg2"
else
	./runFromPOM.sh
fi
# vi:set tabstop=4 hardtabs=4 shiftwidth=4:
