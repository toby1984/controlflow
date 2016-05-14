(C) 2013 tobias.gierke@code-sourcery.de

This project actually contains two different applications:

1. A tool for generating control-flow graphs from bytecode (conveniently packages as a self-executable JAR)

2. (work-in-progress) A Java instrumentation agent that utilizes control-flow analysis to inject
   bytecode for counting the number of executed bytecode instructions per thread

BUILDING
--------

Requirements:

JDK 1.7 or higher
Maven 2.2.1 or higher

Just run 'mvn clean package' and you should find a self-executable JAR named "controlflow.jar" inside the /target folder.

CREATING CONTROL-FLOW GRAPHS
----------------------------

First, install the fantastic Graphviz tool (http://www.graphviz.org/). On Linux systems, most distributions come with
a package (probably named 'graphviz') that can be installed using the package manager. 

Now we're ready to create some fancy graphs ! We'll try a simple test class first that is already included in the 
controlflow.jar.

Execute the following commands:

> java -jar target/controlflow.jar -constructors -dir dot de.codesourcery.asm.TestClass

The tool does not generate control-flow graphs for constructors by default so I pass the '-constructors' option to 
include them. See below for a complete list of available command-line options.

You should now have a folder named 'dot' with the following files:

dot/init___V.dot
dot/testMethod__Z_V.dot

Now use the graphviz tool to generate PNG files (graphviz also supports a host of other file formats) from them: 

> dot -O -Tpng dot/init___V.dot
> dot -O -Tpng dot/testMethod__Z_V.dot

The 'dot' folder should now also contain the PNG files: 

dot/init___V.dot
dot/init___V.dot.png
dot/testMethod__Z_V.dot
dot/testMethod__Z_V.dot.png 

COMMAND-LINE OPTIONS
--------------------

The control-flow grapher (self-executable JAR) supports the following command-line syntax:

Usage: [-debug] [-v] [-constructors] [-search <classpath entries>] [-match <regex>] -dir <directory> <CLASS NAME>

[-debug]                     => enable debug output
[-v]                         => enable verbose output
[-search <classpath entries> => Substitute for JVM -classpath option since that one does not work with self-executable JARs
-dir <directory>             => outputs .dot files to this directory
[-constructors]              => include constructors in flow analysis
[-match <regex>]             => only analyze methods whose name matches this regex
<CLASS NAME>                 => name of class to analyze

PROFILING AGENT
---------------

Note that this is very much WIP and I mostly used this as a sample application for my control-flow analysis. 

CAVEAT: Do NOT use this on production systems ! I'm a novice at bytecode generation and didn't read very much of the JVM spec yet ...

The class transformation done by this Java agent is non-trivial because it is introducing new code at the start of each control block and not just at the
start of each method...

1. To run an instrumented example application

java "-javaagent:target/controlflow.jar=packages=TestClass;debugDir=tmp" -classpath target/controlflow.jar de.codesourcery.asm.profiling.TestApplication

Because of the 'debugDir' option, transformed classes will be written to ./tmp

2. Create control-flow graphs in .dot format for transformed classes

java -jar target/controlflow.jar -constructors -search tmp -dir dot de.codesourcery.asm.TestClass

Files will be written to ./dot

3. Create PNG from .dot file (requires graphviz to be installed)

dot -O -Tpng dot/testMethod__Z_V.dot

(this will generate dot/testMethod__Z_V.dot.png) 
