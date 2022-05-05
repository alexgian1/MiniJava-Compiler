all: compile

compile:
	java -jar jtb-javacc-2017/project_2/jtb132di.jar -te minijava.jj
	java -jar jtb-javacc-2017/project_2/javacc5.jar minijava-jtb.jj
	javac Main.java

clean:
	rm -f *.class *~ JavaCharStream* JTBToolkit* MiniJavaParser* minijava-jtb.jj ParseException* Token* *.class
	rm -rf syntaxtree visitor

