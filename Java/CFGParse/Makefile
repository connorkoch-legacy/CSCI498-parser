JFLAGS = -g
JC = javac
JVM= java 
FILE=


.SUFFIXES: .java .class

.java.class:
	mkdir -p out
	$(JC) $(JFLAGS) $*.java -d out

CLASSES = src/*.java

MAIN = Main

default: classes

classes: $(CLASSES:.java=.class)

run:
	chmod u+x CFGPARSE
	./CFGPARSE
	# $(MAIN).class
	# $(JVM) $(MAIN)

clean:
	$(RM) out/*
