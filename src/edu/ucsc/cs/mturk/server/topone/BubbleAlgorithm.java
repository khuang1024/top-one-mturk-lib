package edu.ucsc.cs.mturk.server.topone;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import com.amazonaws.mturk.service.axis.RequesterService;
import com.amazonaws.mturk.util.PropertiesClientConfig;
import com.amazonaws.mturk.requester.HIT;
import com.amazonaws.mturk.requester.HITStatus;

public class BubbleAlgorithm implements Algorithm {

    /* The inputs and output of the algorithm*/
    private RequesterService service;	//MTurk service
    private ArrayList<Object> questions;
    private Object finalAnswer;
    private int nInput;		// number of outputs of a HIT
    private int nOutput;	// number of outputs of a HIT
    private int nAssignment;	// number of assignments of a normal HIT
    private int nTieAssignment;	// number of assignments of a tie-solving HIT 
    private boolean isShuffled;	// shuffle the inputs
    private boolean isLogged;	// whether generate log automatically
    private String jobId;	// programmers can assign an ID to this job.
    
    /* The IP and port used for communicating with client*/
    private String clientIp;
    private int clientPort;
    
    private boolean isDone;
    
    public BubbleAlgorithm(ArrayList<Object> questions, int numberOfInputs,
	    int numberOfOutputs, int numberOfAssignments, 
	    int numberOfTieAssignments, boolean isShuffled, 
	    boolean isLogged, String jobId, String clientIp, 
	    int clientPort, String propertyFileName) {
	
	// First, check the input parameters.
	validateInitialization(questions, numberOfInputs, numberOfOutputs, 
		    numberOfAssignments, numberOfTieAssignments);
	
	// Initialize member field.
	this.service = new RequesterService(
		new PropertiesClientConfig(
			System.getProperty("user.dir") + 
			java.io.File.separator + 
			propertyFileName));
	this.questions = questions;
	this.nInput = numberOfInputs;
	this.nOutput = numberOfOutputs;
	this.nAssignment = numberOfAssignments;
	this.nTieAssignment = numberOfTieAssignments;
	this.isShuffled = isShuffled;
	this.isLogged = isLogged;
	this.jobId = jobId;
	this.clientIp = clientIp;
	this.clientPort = clientPort;
    }
    
    /*
     * This function validates the values of parameters input by library users.
     */
    private void validateInitialization(ArrayList<Object> questions,
	    int numberOfInputs,int numberOfOutputs, 
	    int numberOfAssignments, int numberOfTieAssignments) {
	if (questions.size() == 0) {
	    throw new BubbleAlgorithmException("The size of questions is 0." +
	    		" [questions.size() == 0]");
	}
	if (questions.size() < numberOfInputs) {
	    throw new BubbleAlgorithmException("The size of questions is" +
	    		" less than the number of inputs of a HIT." +
	    		" [questions.size() < numberOfInputs]");
	}
	if (questions.size() < numberOfOutputs) {
	    throw new BubbleAlgorithmException("The size of questions is" +
	    		" less than the number of outputs of a HIT." +
	    		" [questions.size() < numberOfOutputs]");
	}
	if (numberOfInputs < numberOfOutputs) {
	    throw new BubbleAlgorithmException("The number of inputs of a HIT" +
	    		" is less than the number of outputs of a HIT." +
	    		" [numberOfInputs < numberOfOutputs]");
	}
	if (numberOfInputs < 0) {
	    throw new BubbleAlgorithmException("The number of inputs of a HIT" +
	    		" is negative." +
	    		" [numberOfInputs < 0]");
	}
	if (numberOfOutputs < 0) {
	    throw new BubbleAlgorithmException("The number of outputs of a HIT" +
	    		" is negative." +
	    		" [numberOfOutputs < 0]");
	}
	if (numberOfAssignments < 1) {
	    throw new BubbleAlgorithmException("The number of assignments of " +
	    		"a normal HIT is less than 1." +
	    		" [numberOfAssignments < 1]");
	}
	if (numberOfTieAssignments < 1) {
	    throw new BubbleAlgorithmException("The number of assignments of " +
	    		"a tie-solving HIT is less than 1." +
	    		" [numberOfTieAssignments < 1]");
	}
    }
    
    public void start() {
	if (isShuffled) {
	    Collections.shuffle(questions);
	}
	
	if (isLogged) {
	    String log = "";
	    
	    LogWriter.createOrResetLog(jobId);
	    log += "The tree algorithm started at " + new Date().toString() + "\n\n";
	    log += "The Table of Parameters \n" + 
	           "+-----------------------------------------------+-------------------------------+ \n" +  
		   "| Number of Inputs of a Normal HIT              | " + nInput + "\n" + 
		   "+-----------------------------------------------+-------------------------------+ \n" +
		   "| Number of Outputs of a Normal HIT             | " + nOutput + "\n" + 
		   "+-----------------------------------------------+-------------------------------+ \n" +
		   "| Number of Assignments of a Normal HIT         | " + nAssignment + "\n" + 
		   "+-----------------------------------------------+-------------------------------+ \n" +
		   "| Number of Assignments of a Tie-Solving HIT    | " + nTieAssignment + "\n" +
		   "+-----------------------------------------------+-------------------------------+ \n" +
		   "| Inputs Are Shuffled                           | " + isShuffled + "\n" + 
		   "+-----------------------------------------------+-------------------------------+ \n" +
		   "| Generate Automatic Log                        | " + isLogged + "\n" +
		   "+-----------------------------------------------+-------------------------------+ \n" +
		   "| The Name of the Automatic Log                 | " + jobId + "\n" +
		   "+-----------------------------------------------+-------------------------------+ \n" +
		   "| Job ID                                        | " + jobId + "\n" + 
		   "+-----------------------------------------------+-------------------------------+ \n\n";
	    LogWriter.writeLog(log, jobId);
	    System.out.println(log);
	}
	
	while (questions.size() > 1) {
	    ArrayList<Object> answers = new ArrayList<Object>();
	    ArrayList<Object> inputs = new ArrayList<Object>();
	    
	    if (questions.size() >= nInput) {
		for (int i = 0; i < nInput; i++) {
		    inputs.add(questions.get(0));
		    questions.remove(0);
		}
	    } else {
		nOutput = 1;
		int questionSize = questions.size();
		for (int i = 0; i < questionSize; i++) {
		    inputs.add(questions.get(0));
		    questions.remove(0);
		}
	    }
	    
	    // Create a HIT with the inputs.
	    String hitId = null;
	    try {
		hitId = HitOperation.createHit(inputs, nOutput, nAssignment,
			clientIp, clientPort, jobId);
	    } catch (UnknownHostException e) {
		throw new TopOneServerException("UnknowHostException." +
				" Please check your socket IP and port.");
	    } catch (IOException e) {
		throw new TopOneServerException("IOException." +
			" Please check your socket IP and port.");
	    }
	    
	    if (isLogged) {
		LogWriter.writeBubbleCreateHitLog(service, hitId, nAssignment, nOutput, inputs, jobId);
	    }
	    
	    // Keep waiting and checking the status of this new HIT, until it is done.
	    HIT hit = service.getHIT(hitId);
	    while (hit.getHITStatus() != HITStatus.Reviewable) {
		try {
		    Thread.sleep(1000*5);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
		hit = service.getHIT(hitId);
	    }
	    
	    // Get the answers which have been checked and refined.
	    ArrayList<Object> rawAnswers;
	    try {
		rawAnswers = HitOperation.getAnswer(hitId, clientIp, clientPort, jobId);
	    } catch (UnknownHostException e) {
		throw new TopOneServerException("UnknownHostException." +
			"Please check the IP and port of client socket.");
	    } catch (IOException e) {
		throw new TopOneServerException("IOException." +
			"Please check the IP and port of client socket.");
	    }
	    answers = AnswerProcessor.refineRawAnswers(
		    rawAnswers, service, nOutput, nTieAssignment, 
		    isLogged, jobId, AnswerProcessor.BUBBLE_ALGORITHM, 
		    clientIp, clientPort);
	    
	    if (isLogged) {
		LogWriter.writeGetAnswerLog(hitId, answers, jobId);
	    }
	    
	    // Put the new answers into the questions queue.
	    for (int i = answers.size() -1; i >= 0; i--) {
		questions.add(0, answers.get(i));
	    }
	    
	    // Deal with the this already used HIT.
//	    myHit.dumpPastHit(service, hitId);
	}
	
	finalAnswer = questions.get(0);
	
	String info = "The bubble algorithm ended at " + 
    		new Date().toString() + "\n\n";;
	info = "The final answer is: " + finalAnswer + "\n";
	LogWriter.writeLog(info, jobId);
	System.out.println(info);
    }
    
    /**
     * Indicate whether this instance is done.
     * 
     * @return <tt>true</tt> if the program is done.
     */
    public boolean isDone() {
	return isDone;
    }
    
    /**
     * Return the final answer.
     * 
     * @return The final answer.
     */
    public Object getFinalAnswer() {
	return finalAnswer;
    }
}
