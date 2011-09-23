package edu.ucsc.cs.mturk.demo;

import java.util.ArrayList;

import com.amazonaws.mturk.service.axis.RequesterService;
import com.amazonaws.mturk.util.PropertiesClientConfig;

import edu.ucsc.cs.mturk.lib.topone.TreeAlgorithm;

/*
 * Run this class to demo the algorithm.
 */
public class DemoTreeAlgorithm {

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
	questions.add("19");
	
	TreeAlgorithm tree = new TreeAlgorithm.Builder(questions, demoHit).
		inputSize(numberOfInputs).outputSize(numberOfOutputs).
		numberOfAssignments(numberOfAssignments).
		numberOfTieAssignments(numberOfTieAssignments).
		service(service).build();
	
	tree.start();
    }

}
