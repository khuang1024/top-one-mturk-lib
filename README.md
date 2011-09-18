This is a library exclusively for Amazon Mechanical Turk. This library 
implements two algorithms which are tree-like and bubble-like respectively.

Sometimes, MTurk requesters have a big task which needs to be divided into
small sub-tasks and combines their sub-results to reach the final result.
This process can be organized like a tree or a bubble. With this library, 
you can easily make this feature. 

There are three main components in this library.
1. src/edu/ucsc/cs/mturk/demo contains the demo programs for the use of Java
library (if you use Java) or server (if you use other language, you can run 
the algorithm server and pass your questions into the server to process through
socket).
2. src/edu/ucsc/cs/mturk/lib contains the source files of the Java library.
3. src/edu/ucsc/cs/mturk/server contains the source files of the algorithm
server.
