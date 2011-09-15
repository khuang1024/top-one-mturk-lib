package edu.ucsc.cs.mturk.test;

import java.util.ArrayList;

import com.amazonaws.mturk.service.axis.RequesterService;
import com.amazonaws.mturk.util.PropertiesClientConfig;

import edu.ucsc.cs.mturk.lib.topone.TreeAlgorithm;

/*
 * Run this class to demo the algorithm.
 */
public class Test {

    /**
     * @param args
     */
    public static void main(String[] args) {
	// TODO Auto-generated method stub
	
	ArrayList<Object> questions = new ArrayList<Object>();
	int numberOfInputs = 3;
	int numberOfOutputs = 2;
	int numberOfAssignments = 2; 
	int numberOfTieAssignments = 1;
	TestHit testHit = new TestHit();
	RequesterService service = new RequesterService(
		new PropertiesClientConfig(System.getProperty("user.dir") +
			java.io.File.separator + "mturk.properties"));
	questions.add("1");
	questions.add("2");
	questions.add("3");
	questions.add("4");
	questions.add("5");
	questions.add("6");
	questions.add("7");
	questions.add("8");
	questions.add("9");
	questions.add("10");
	questions.add("11");
	questions.add("12");
	questions.add("13");
	questions.add("14");
	questions.add("15");
	questions.add("16");
	questions.add("17");
	questions.add("18");
	
	TreeAlgorithm tree = new TreeAlgorithm(questions, numberOfInputs,
		    numberOfOutputs, numberOfAssignments, 
		    numberOfTieAssignments, testHit, service);
	tree.start();
	while (!tree.isDone()) {
	    try {
		Thread.sleep(1000 * 10);
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	}
	
	System.out.println("The finall answer is: " + tree.getFinalAnswer());
    }

}
