package edu.ucsc.cs.mturk.lib.topone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import com.amazonaws.mturk.service.axis.RequesterService;
import com.amazonaws.mturk.util.PropertiesClientConfig;
import com.amazonaws.mturk.requester.HIT;
import com.amazonaws.mturk.requester.HITStatus;

public class BubbleAlgorithm {

    
    /* The inputs and output of the algorithm*/
    private RequesterService service;	//MTurk service
    private MyHit myHit;	// the object for callback
    private ArrayList<Object> questions;
    private Object finalAnswer;
    private int nInput;		// number of outputs of a HIT
    private int nOutput;	// number of outputs of a HIT
    private int nAssignment;	// number of assignments of a normal HIT
    private int nTieAssignment;	// number of assignments of a tie-solving HIT 
    private boolean isShuffled;	// shuffle the inputs
    private boolean isLogged;	// whether generate log automatically
    private String logName;	// the name of the log file
    private String jobId;	// programmers can assign an ID to this job.
    
    private boolean isDone;

    /**
     * Constructs a bubble algorithm instance with <tt>service</tt>. 
     * By default, the questions will be shuffled before the algorithm 
     * starts; the process will be recorded in an automatically-generated log.
     * 
     * @param questions the questions to be solved by workers.
     * @param numberOfInputs the number of inputs of each HIT.
     * @param numberOfOutputs the number of outputs of each HIT.
     * @param numberOfAssignments the number of assignments of each normal HIT.
     * @param numberOfTieAssignments the number of assignments of 
     * each tie-solving HIT.
     * @param myHit the object which implements <i>MyHit</i> interface and 
     * provides callback functions.
     * @param service the service object of the library user.
     */
    public BubbleAlgorithm(ArrayList<Object> questions, int numberOfInputs,
	    int numberOfOutputs, int numberOfAssignments, 
	    int numberOfTieAssignments, MyHit myHit, 
	    RequesterService service) {
	this.service = service;
	this.initEssential(questions, numberOfInputs, numberOfOutputs, 
		           numberOfAssignments, numberOfTieAssignments, myHit);
	this.isShuffled = true;
	this.isLogged = true;
	this.logName = "Bubble_Algorithm_" + new Date().hashCode() + ".txt";
	this.jobId = null;
    }

    /**
     * Constructs a bubble algorithm instance with <tt>service</tt>. 
     * By default, the process will be recorded in an 
     * automatically-generated log.
     * 
     * @param questions the questions to be solved by workers.
     * @param numberOfInputs the number of inputs of each HIT.
     * @param numberOfOutputs the number of outputs of each HIT.
     * @param numberOfAssignments the number of assignments of each normal HIT.
     * @param numberOfTieAssignments the number of assignments of 
     * each tie-solving HIT.
     * @param myHit the object which implements <i>MyHit</i> interface and 
     * provides callback functions.
     * @param service the service object of the library user.
     * @param isShuffled <tt>true</tt> if the questions are shuffled by the 
     * algorithm.
     */
    public BubbleAlgorithm(ArrayList<Object> questions, int numberOfInputs,
	    int numberOfOutputs, int numberOfAssignments, 
	    int numberOfTieAssignments, MyHit myHit, 
	    RequesterService service, boolean isShuffled) {
	this.service = service;
	this.initEssential(questions, numberOfInputs, numberOfOutputs, 
		           numberOfAssignments, numberOfTieAssignments, myHit);
	this.isShuffled = isShuffled;
	this.isLogged = true;
	this.logName = "Bubble_Algorithm_" + new Date().hashCode() + ".txt";
	this.jobId = null;
    }
    
    /**
     * Constructs a bubble algorithm instance with <tt>service</tt>. 
     * 
     * @param questions the questions to be solved by workers.
     * @param numberOfInputs the number of inputs of each HIT.
     * @param numberOfOutputs the number of outputs of each HIT.
     * @param numberOfAssignments the number of assignments of each normal HIT.
     * @param numberOfTieAssignments the number of assignments of 
     * each tie-solving HIT.
     * @param myHit the object which implements <i>MyHit</i> interface and 
     * provides callback functions.
     * @param service the service object of the library user.
     * @param isShuffled <tt>true</tt> if the questions are shuffled by the 
     * algorithm.
     * @param isLogged <tt>true</tt> if the algorithm records the process in 
     * the log file with a default name whose prefix is <i>Bubble_Algorithm</i>.
     */
    public BubbleAlgorithm(ArrayList<Object> questions, int numberOfInputs,
	    int numberOfOutputs, int numberOfAssignments, 
	    int numberOfTieAssignments, MyHit myHit, 
	    RequesterService service, boolean isShuffled, 
	    boolean isLogged) {
	this.service = service;
	this.initEssential(questions, numberOfInputs, numberOfOutputs, 
		           numberOfAssignments, numberOfTieAssignments, myHit);
	this.isShuffled = isShuffled;
	this.isLogged = isLogged;
	this.logName = "Bubble_Algorithm_" + new Date().hashCode() + ".txt";
	this.jobId = null;
    }
    
    /**
     * Constructs a bubble algorithm instance with <tt>service</tt>.
     * 
     * @param questions the questions to be solved by workers.
     * @param numberOfInputs the number of inputs of each HIT.
     * @param numberOfOutputs the number of outputs of each HIT.
     * @param numberOfAssignments the number of assignments of each normal HIT.
     * @param numberOfTieAssignments the number of assignments of 
     * each tie-solving HIT.
     * @param myHit the object which implements <i>MyHit</i> interface and 
     * provides callback functions.
     * @param service the service object of the library user.
     * @param isShuffled <tt>true</tt> if the questions are shuffled by the 
     * algorithm.
     * @param isLogged <tt>true</tt> if the algorithm records the process.
     * @param logName the name of the log file.
     */
    public BubbleAlgorithm(ArrayList<Object> questions, int numberOfInputs,
	    int numberOfOutputs, int numberOfAssignments, 
	    int numberOfTieAssignments, MyHit myHit, 
	    RequesterService service, boolean isShuffled, 
	    boolean isLogged, String logName) {
	this.service = service;
	this.initEssential(questions, numberOfInputs, numberOfOutputs, 
		           numberOfAssignments, numberOfTieAssignments, myHit);
	this.isShuffled = isShuffled;
	this.isLogged = isLogged;
	this.logName = logName;
	this.jobId = null;
    }

    /**
     * Constructs a bubble algorithm instance with <tt>service</tt>  
     * and assigns a job ID to this instance.
     * 
     * @param questions the questions to be solved by workers.
     * @param numberOfInputs the number of inputs of each HIT.
     * @param numberOfOutputs the number of outputs of each HIT.
     * @param numberOfAssignments the number of assignments of each normal HIT.
     * @param numberOfTieAssignments the number of assignments of 
     * each tie-solving HIT.
     * @param myHit the object which implements <i>MyHit</i> interface and 
     * provides callback functions.
     * @param service the service object of the library user.
     * @param isShuffled <tt>true</tt> if the questions are shuffled by the 
     * algorithm.
     * @param isLogged <tt>true</tt> if the algorithm records the process.
     * @param logName the name of the log file.
     * @param jobId the identification assigned by the library user.
     */
    public BubbleAlgorithm(ArrayList<Object> questions, int numberOfInputs,
	    int numberOfOutputs, int numberOfAssignments, 
	    int numberOfTieAssignments, MyHit myHit, 
	    RequesterService service, boolean isShuffled, 
	    boolean isLogged, String logName, String jobId) {
	this.service = service;
	this.initEssential(questions, numberOfInputs, numberOfOutputs,
		 numberOfAssignments, numberOfTieAssignments, myHit);
	this.isShuffled = isShuffled;
	this.isLogged = isLogged;
	this.logName = logName;
	this.jobId = jobId;
    }
    
    
    /**
     * Constructs a bubble algorithm instance with the <i>property file</i>. 
     * By default, the questions will be shuffled before the algorithm 
     * starts; the process will be recorded in an automatically-generated log.
     * 
     * @param questions the questions to be solved by workers.
     * @param numberOfInputs the number of inputs of each HIT.
     * @param numberOfOutputs the number of outputs of each HIT.
     * @param numberOfAssignments the number of assignments of each normal HIT.
     * @param numberOfTieAssignments the number of assignments of 
     * each tie-solving HIT.
     * @param myHit the object which implements <i>MyHit</i> interface and 
     * provides callback functions.
     * @param propertyFileName the name of the property file.
     */
    public BubbleAlgorithm(ArrayList<Object> questions, int numberOfInputs,
	    int numberOfOutputs, int numberOfAssignments, 
	    int numberOfTieAssignments, MyHit myHit, String propertyFileName) {
	this.service = new RequesterService(
				new PropertiesClientConfig(
					System.getProperty("user.dir") + 
					java.io.File.separator + 
					propertyFileName));
	this.initEssential(questions, numberOfInputs, numberOfOutputs,
		           numberOfAssignments, numberOfTieAssignments, myHit);
	this.isShuffled = true;
	this.isLogged = true;
	this.logName = "Bubble_Algorithm_" + new Date().hashCode() + ".txt";
	this.jobId = null;
    }
    
    /**
     * Constructs a bubble algorithm instance with the <i>property file</i>. 
     * By default, the process will be recorded in an 
     * automatically-generated log.
     * 
     * @param questions the questions to be solved by workers.
     * @param numberOfInputs the number of inputs of each HIT.
     * @param numberOfOutputs the number of outputs of each HIT.
     * @param numberOfAssignments the number of assignments of each normal HIT.
     * @param numberOfTieAssignments the number of assignments of 
     * each tie-solving HIT.
     * @param myHit the object which implements <i>MyHit</i> interface and 
     * provides callback functions.
     * @param propertyFileName the name of the property file.
     * @param isShuffled <tt>true</tt> if the questions are shuffled by the 
     * algorithm.
     */
    public BubbleAlgorithm(ArrayList<Object> questions, int numberOfInputs,
	    int numberOfOutputs, int numberOfAssignments, 
	    int numberOfTieAssignments, MyHit myHit, 
	    String propertyFileName, boolean isShuffled) {
	this.service = new RequesterService(
		new PropertiesClientConfig(
			System.getProperty("user.dir") + 
			java.io.File.separator + 
			propertyFileName));
	this.initEssential(questions, numberOfInputs, numberOfOutputs, 
		           numberOfAssignments, numberOfTieAssignments, myHit);
	this.isShuffled = isShuffled;
	this.isLogged = true;
	this.logName = "Bubble_Algorithm_" + new Date().hashCode() + ".txt";
	this.jobId = null;
    }
    
    /**
     * Constructs a bubble algorithm instance with the <i>property file</i>. 
     * 
     * @param questions the questions to be solved by workers.
     * @param numberOfInputs the number of inputs of each HIT.
     * @param numberOfOutputs the number of outputs of each HIT.
     * @param numberOfAssignments the number of assignments of each normal HIT.
     * @param numberOfTieAssignments the number of assignments of 
     * each tie-solving HIT.
     * @param myHit the object which implements <i>MyHit</i> interface and 
     * provides callback functions.
     * @param propertyFileName the name of the property file.
     * @param isShuffled <tt>true</tt> if the questions are shuffled by the 
     * algorithm.
     * @param isLogged <tt>true</tt> if the algorithm records the process in 
     * the log file with a default name whose prefix is <i>Bubble_Algorithm</i>.
     */
    public BubbleAlgorithm(ArrayList<Object> questions, int numberOfInputs,
	    int numberOfOutputs, int numberOfAssignments, 
	    int numberOfTieAssignments, MyHit myHit, 
	    String propertyFileName, boolean isShuffled, 
	    boolean isLogged) {
	this.service = new RequesterService(
		new PropertiesClientConfig(
			System.getProperty("user.dir") + 
			java.io.File.separator + 
			propertyFileName));
	this.initEssential(questions, numberOfInputs, numberOfOutputs, 
		           numberOfAssignments, numberOfTieAssignments, myHit);
	this.isShuffled = isShuffled;
	this.isLogged = isLogged;
	this.logName = "Bubble_Algorithm_" + new Date().hashCode() + ".txt";
	this.jobId = null;
    }
    
    /**
     * Constructs a bubble algorithm instance with the <i>property file</i>.
     * 
     * @param questions the questions to be solved by workers.
     * @param numberOfInputs the number of inputs of each HIT.
     * @param numberOfOutputs the number of outputs of each HIT.
     * @param numberOfAssignments the number of assignments of each normal HIT.
     * @param numberOfTieAssignments the number of assignments of 
     * each tie-solving HIT.
     * @param myHit the object which implements <i>MyHit</i> interface and 
     * provides callback functions.
     * @param propertyFileName the name of the property file.
     * @param isShuffled <tt>true</tt> if the questions are shuffled by the 
     * algorithm.
     * @param isLogged <tt>true</tt> if the algorithm records the process.
     * @param logName the name of the log file.
     */
    public BubbleAlgorithm(ArrayList<Object> questions, int numberOfInputs,
	    int numberOfOutputs, int numberOfAssignments, 
	    int numberOfTieAssignments, MyHit myHit, 
	    String propertyFileName, boolean isShuffled, 
	    boolean isLogged, String logName) {
	this.service = new RequesterService(
		new PropertiesClientConfig(
			System.getProperty("user.dir") + 
			java.io.File.separator + 
			propertyFileName));
	this.initEssential(questions, numberOfInputs, numberOfOutputs, 
		           numberOfAssignments, numberOfTieAssignments, myHit);
	this.isShuffled = isShuffled;
	this.isLogged = isLogged;
	this.logName = logName;
	this.jobId = null;
    }

    /**
     * Constructs a bubble algorithm instance with the <i>property file</i> 
     * and assigns a job ID to this instance.
     * 
     * @param questions the questions to be solved by workers.
     * @param numberOfInputs the number of inputs of each HIT.
     * @param numberOfOutputs the number of outputs of each HIT.
     * @param numberOfAssignments the number of assignments of each normal HIT.
     * @param numberOfTieAssignments the number of assignments of 
     * each tie-solving HIT.
     * @param myHit the object which implements <i>MyHit</i> interface and 
     * provides callback functions.
     * @param propertyFileName the name of the property file.
     * @param isShuffled <tt>true</tt> if the questions are shuffled by the 
     * algorithm.
     * @param isLogged <tt>true</tt> if the algorithm records the process.
     * @param logName the name of the log file.
     * @param jobId the identification assigned by the library user.
     */
    public BubbleAlgorithm(ArrayList<Object> questions, int numberOfInputs,
	    int numberOfOutputs, int numberOfAssignments, 
	    int numberOfTieAssignments, MyHit myHit, 
	    String propertyFileName, boolean isShuffled, 
	    boolean isLogged, String logName, String jobId) {
	this.service = new RequesterService(
		new PropertiesClientConfig(
			System.getProperty("user.dir") + 
			java.io.File.separator + 
			propertyFileName));
	this.initEssential(questions, numberOfInputs, numberOfOutputs,
		 numberOfAssignments, numberOfTieAssignments, myHit);
	this.isShuffled = isShuffled;
	this.isLogged = isLogged;
	this.logName = logName;
	this.jobId = jobId;
    }
    
    private void initEssential(ArrayList<Object> questions,
	    int numberOfInputs,int numberOfOutputs, 
	    int numberOfAssignments, int numberOfTieAssignments, 
	    MyHit myHit) {
	this.checkInitialization(questions, numberOfInputs, 
		numberOfOutputs, numberOfAssignments, numberOfTieAssignments);
	this.myHit = myHit;
	this.questions = (ArrayList<Object>) questions;
	this.finalAnswer = null;
	this.nInput = numberOfInputs;
	this.nOutput = numberOfOutputs;
	this.nAssignment = numberOfAssignments;
	this.nTieAssignment = numberOfTieAssignments;
	this.isDone = false;
    }
    
    /*
     * This function validates the values of parameters input by library users.
     */
    private void checkInitialization(ArrayList<Object> questions,
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
    
//===============================================asdfjnakdfjnasdfnlakjsdfnakjlsdnflakbsdfkajbsdflkjabsdflkjabdsflkjasd=======================

	
	
	
    public void start() {
	if (isShuffled) {
	    Collections.shuffle(questions);
	}
	
	if (isLogged) {
	    String log = "";
	    
	    LogWriter.createOrResetLog(logName);
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
		   "| The Name of the Automatic Log                 | " + logName + "\n" +
		   "+-----------------------------------------------+-------------------------------+ \n" +
		   "| Job ID                                        | " + jobId + "\n" + 
		   "+-----------------------------------------------+-------------------------------+ \n\n";
	    LogWriter.writeLog(log, logName);
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
	    
	    
	    // Create a new HIT.
	    String hitId = myHit.createMyHit(service, inputs, nOutput, nAssignment);
	    
	    if (isLogged) {
		LogWriter.writeBubbleCreateHitLog(service, hitId, nAssignment, nOutput, inputs, logName);
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
	    
	    // Retrieve the answers of the HIT.
	    answers =  AnswerProcessor.refineRawAnswers(
		    		myHit.getMyHitAnswers(service, hitId), 
		    		nOutput, service, myHit, nTieAssignment, 
		    		isLogged, logName, AnswerProcessor.BUBBLE_ALGORITHM);
	    
	    if (isLogged) {
		LogWriter.writeGetAnswerLog(hitId, answers, logName);
	    }
	    
	    // Put the new answers into the questions queue.
	    for (int i = answers.size() -1; i >= 0; i--) {
		questions.add(0, answers.get(i));
	    }
	    
	    System.out.println(questions.size());
	    
	    // Deal with the this already used HIT.
	    myHit.dumpPastHit(service, hitId);
	}
	
	finalAnswer = questions.get(0);
	
	String info = "The bubble algorithm ended at " + 
    		new Date().toString() + "\n\n";;
	info = "The final answer is: " + finalAnswer + "\n";
	LogWriter.writeLog(info, logName);
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
