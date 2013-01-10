(C) 2013 tobias.gierke@code-sourcery.de

1. Run instrumented example

java "-javaagent:target/controlflow.jar=packages=TestClass;debugDir=tmp" -classpath target/controlflow.jar de.codesourcery.asm.profiling.TestApplication

Transformed classes will be written to ./tmp

2. Create control-flow graphs in .dot format for transformed classes

java -jar target/controlflow.jar -constructors -search tmp -dir dot de.codesourcery.asm.TestClass

Files will be written to ./dot

3. Create PNG from .dot file (requires graphviz to be installed)

dot -O -Tpng dot/testMethod__Z_V.dot

(this will generate dot/testMethod__Z_V.dot.png) 
