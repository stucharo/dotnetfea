Book: G.Nikishkov, Programming Finite Elements in Java, Springer, 2009.

ADDITIONAL MATERIALS
                    
Directories contain:
src - Java source files;
classes - Java class files;
example.. - examples


* To run examples you need:

JVM 1.6 or higher and Java 3D 1.5 or higher installed.
You can download JDK or JRE and Java 3D API from: 
http://java.sun.com/javase/downloads/
http://java.sun.com/javase/technologies/desktop/java3d/


* To compile Java source files into Java class files:

You need Java compiler installed. Java compiler javac is a part of JDK.

Compilation is performed with the following two lines
(your current directory contains subdirectories src and classes)
  
javac -verbose -sourcepath src -d classes src/fea/*.java
javac -verbose -sourcepath src -d classes src/gener/*.java

These two commands are placed in file jc.bat.


* To run examples:

Go to an example directory and follow directions of ReadMe file.

