package edu.ucsc.cs.mturk.test;

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
	
	BubbleAlgorithm tree = new BubbleAlgorithm(questions, numberOfInputs,
		    numberOfOutputs, numberOfAssignments, 
		    numberOfTieAssignments, demoHit, service);
	tree.start();
    }

}
