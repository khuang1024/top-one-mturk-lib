## Introduction
This is a library exclusively for Amazon Mechanical Turk. This library 
implements two algorithms which are tree-like and bubble-like respectively.

Sometimes, MTurk requesters have a big task which needs to be divided into
small sub-tasks and combines their sub-results to reach the final result.
This process can be organized like a tree or a bubble. With this library, 
you can easily make this feature. 

## Library
There are three main components in this library.
* **src/edu/ucsc/cs/mturk/demo** contains the demo programs for the use of Java
library (if you use Java) or algorithm server (if you use other language, you
can run the algorithm server and pass your questions into the server to process
through socket).
* **src/edu/ucsc/cs/mturk/lib** contains the source files of the Java library.
* **src/edu/ucsc/cs/mturk/server** contains the source files of the algorithm
server.

## Sample Log:
* ***2118318914*** is the sample log file generated by tree algorithm using socket.
* ***2119516603*** is the sample log file generated by bubble algorithm using socket.
* The text file with prefix ***"Bubble Algirithm"*** is the sample log file generated by bubble algorithm with Java library.
* The text file with prefix ***"Tree Algorithm"*** is the sample log file generated by tree algorithm with Java library.

## Remark:
* You can see more information [here]( http://users.soe.ucsc.edu/~khuang/mturk/topone/topone.html).
* See the [Java API documentation](http://users.soe.ucsc.edu/~khuang/mturk/topone/top1doc/).
