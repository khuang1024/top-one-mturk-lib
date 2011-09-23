package edu.ucsc.cs.mturk.demo;

import java.util.ArrayList;

import com.amazonaws.mturk.service.axis.RequesterService;
import com.amazonaws.mturk.util.PropertiesClientConfig;

import edu.ucsc.cs.mturk.lib.topone.BubbleAlgorithm;

/*
 * Run this class to demo the algorithm.
 */
public class DemoBubbleAlgorithm {

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
	DemoHit demoHit = new DemoHit();
	RequesterService service = new RequesterService(
		new PropertiesClientConfig(System.getProperty("user.dir") +
			java.io.File.separator + "mturk.properties"));
	questions.add("1");
	questions.add("2");
	questions.add("3");
	questions.add("4");
	questions.add("5");
	
	BubbleAlgorithm bubble = new BubbleAlgorithm.Builder(questions, demoHit).
		inputSize(numberOfInputs).
		outputSize(numberOfOutputs).
		numberOfAssignments(numberOfAssignments).
		numberOfTieAssignments(numberOfTieAssignments).
		service(service).build();
	
	bubble.start();
	while(!bubble.isDone()) {
	    try {
		Thread.sleep(1000*5);
	    } catch(InterruptedException e) {
		e.printStackTrace();
	    }
	}
	
	System.out.println("The final answer of the algorithm is: " + bubble.getFinalAnswer().toString());
    }

}
