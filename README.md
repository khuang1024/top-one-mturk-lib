This is a library exclusivly for Amazon Mechanical Turk. This library 
implements two algorithms which are tree-like and bubble-like respectively.

Sometimes, MTurk requesters have a big task which needs to be divided into
small sub-tasks and combines their sub-results to reach the final answer.
This process can be organized like a tree or a bubble.

With this library, you can easily make this feature. By implementing an 
interface, passing the parameters which serve your specific purpose to the
constructor of an algorithm instance and running this instance, everything
will be done.