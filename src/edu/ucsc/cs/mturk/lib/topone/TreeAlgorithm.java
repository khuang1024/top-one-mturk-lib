/*
 * TreeAlg Version 1.0
 * 
 * This class is the main part of tree algorithm -- one of the top-1
 * algorithm associated with Amazon Mechanical Turk API.
 * 
 * Notte:Amazon Mechanical Turk only supports Java SDK 1.5.0 
 *       (JDK 5) or later. The SDK is not 100% compatible with JRE 6.
 *       Therefore, the suggestion is to use Java SDK 1.5 instead of
 *       1.6 or 1.7.
 * 
 * Date: Sep 12, 2011
 */

package edu.ucsc.cs.mturk.lib.topone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import com.amazonaws.mturk.requester.HIT;
import com.amazonaws.mturk.requester.HITStatus;
import com.amazonaws.mturk.service.axis.RequesterService;
import com.amazonaws.mturk.util.PropertiesClientConfig;

/**
 * <p>This class is the implementation of tree algorithm. It simulates the 
 * process of the tree algorithm. Users build an object of the tree 
 * algorithm through this class, put questions into this object, and 
 * the object takes care the rest job. Finally, it returns the final answer 
 * of this running instance.</p>
 * 
 * <p>Since some operations, such as creating a HIT and getting answers from 
 * workers etc., should be customized by library users, we use callback 
 * routine to make it. Therefore, library users need to implement <i><b>
 * MyHit</b></i> interface, build an instance of the class and use this 
 * instance as a parameter in the constructor.</p>
 * 
 * <p>Also, generally there are two ways to enable the library to have the 
 * access to your Amazon Mechanical Turk account. The first one is to pass 
 * <tt>service</tt> of type 
 * <tt>import com.amazonaws.mturk.service.axis.RequesterService</tt> 
 * into the constructor. The second one is to locate your Amazon Mechanical 
 * Turk property file, which stores your <tt>access_key</tt>, 
 * <tt>secret_key</tt> and <tt>service_url</tt>, in the same directory of 
 * the source file and to pass the file name as a parameter in the 
 * constructor.</p>
 * 
 * <p>Finally, an instance of this class should be created in a Builder 
 * pattern. To create an instance of the algorithm, a static inner class 
 * Builder may be used like this:<br/>
 * <pre>TreeAlgorithm tree = new TreeAlgorithm.Builder(questions, myHit).
 * propertyFile("mturk.properties").inputSize(6).outputSize(2).
 * numberOfAssignments(5).isLogged(true).logName("Log.txt").
 * jobId("E9FG6X9LO").build();</pre> <br />
 * Note that you do not need to configure all the parameters. You can choose 
 * to configure only what you need. However, questions and myHit are still 
 * required.
 * </p>
 * 
 * @author Kerui Huang
 * @version 1.1
 *
 */
public class TreeAlgorithm {
    
    /* The inputs and output of the algorithm*/
    private final RequesterService service;	//MTurk service
    private final MyHit myHit;	// the object for callback
    private final ArrayList<Object> questions;
    private final int nInput;		// number of outputs of a HIT
    private final int nOutput;	// number of outputs of a HIT
    private final int nAssignment;	// number of assignments of a normal HIT
    private final int nTieAssignment;	// number of assignments of a tie-solving HIT 
    private final boolean isShuffled;	// shuffle the inputs
    private final boolean isLogged;	// whether generate log automatically
    private final String logName;	// the name of the log file
    private final String jobId;	// programmers can assign an ID to this job.
    private Object finalAnswer;
    
    /* The queues recording Hits and intermediate results. */
    private ArrayList<EntityHit> activeHitQueue;
    private ArrayList<Item> newItemQueue;
    private ArrayList<ArrayList<Item>> levelQueue;
    
    /* The arrays helping build a strict "tree". */
    private int[] tags;//records the current last serial number assigned.
    /*
     * The trigger array records the eligibility of each intermediate result to
     * be used for creating a new HIT.
     */
    private boolean[][] trigger;
    
    /*The indicator of the end of the two threads*/
    private boolean isNewItemQueueThreadDone;
    private boolean isActiveHitQueueThreadDone;
    
    /**
     * This class is for creating a tree algorithm in Builder pattern. 
     * 
     * @version 1.1
     * 
     * @author Kerui Huang
     *
     */
    public static class Builder {
	// Required parameters.
	private final MyHit myHit;
	private final ArrayList<Object> questions;
	
	// Optional parameters.
	private RequesterService service = new RequesterService(
		new PropertiesClientConfig(
			System.getProperty("user.dir") + 
			java.io.File.separator + 
			"mturk.properties"));;
	private int nInput = 6;
	private int nOutput = 2;
	private int nAssignment = 5;
	private int nTieAssignment = 1;
	private boolean isShuffled = true;
	private boolean isLogged = true;
	private String logName = "Tree_Algorithm_" + new Date().hashCode() + ".txt";
	private String jobId = null;
	
	/**
	 * Constructs a Builder with questions and myHit.
	 * @param questions the questions to be solved by workers.
	 * @param myHit the object which implements <i>MyHit</i> interface and 
	 * provides callback functions.
	 */
	public Builder(ArrayList<Object> questions, MyHit myHit) {
	    this.myHit = myHit;
	    this.questions = questions;
	}
	
	/**
	 * Set the service object of the library user.
	 * @param service the service object of the library user.
	 * @return the Builder object itself.
	 */
	public Builder service(RequesterService service) {
	    this.service = service;
	    return this;
	}
	
	/**
	 * Set the name of the the name of the MTurk property file.
	 * @param propertyFileName the name of the property file.
	 * @return the Builder object itself.
	 */
	public Builder propertyFile(String propertyFileName) {
	    this.service = new RequesterService(
			new PropertiesClientConfig(
				System.getProperty("user.dir") + 
				java.io.File.separator + 
				propertyFileName));
	    return this;
	}
	
	/**
	 * Set the number of inputs of a HIT.
	 * @param inputSize the number of inputs of a HIT.
	 * @return the Builder object itself.
	 */
	public Builder inputSize(int inputSize) {
	    this.nInput = inputSize;
	    return this;
	}
	
	/**
	 * Set the number of outputs of a HIT.
	 * @param outputSize the number of outputs of a HIT.
	 * @return the Builder object itself.
	 */
	public Builder outputSize(int outputSize) {
	    this.nOutput = outputSize;
	    return this;
	}
	
	/**
	 * Set the number of assignments for a normal HIT.
	 * @param numberOfAssignments the number of assignments for a normal 
	 * HIT.
	 * @return the Builder object itself.
	 */
	public Builder numberOfAssignments(int numberOfAssignments) {
	    this.nAssignment = numberOfAssignments;
	    return this;
	}
	
	/**
	 * Set the number of assignments for a tie-solving HIT.
	 * @param numberOfTieAssignments the number of assignments for a 
	 * tie-solving HIT.
	 * @return the Builder object itself.
	 */
	public Builder numberOfTieAssignments(int numberOfTieAssignments) {
	    this.nTieAssignment = numberOfTieAssignments;
	    return this;
	}
	
	/**
	 * Set whether the inputs are shuffled by the algorithm.
	 * @param isShuffled <tt>true</tt> if the inputs are shuffled by the algorithm.
	 * @return the Builder object itself.
	 */
	public Builder isShuffled(boolean isShuffled) {
	    this.isShuffled = isShuffled;
	    return this;
	}
	
	/**
	 * Set whether the algorithm records the computation process.
	 * @param isLogged <tt>true</tt> if the algorithm records the computation 
	 * process.
	 * @return the Builder object itself.
	 */
	public Builder isLogged(boolean isLogged) {
	    this.isLogged = isLogged;
	    return this;
	}
	
	/**
	 * Set the name of the log file.
	 * @param logName the name of the log file.
	 * @return the Builder object itself.
	 */
	public Builder logName(String logName) {
	    this.logName = logName;
	    return this;
	}
	
	/**
	 * Set the job ID for this algorithm instance.
	 * @param jobId the job ID for this algorithm instance.
	 * @return the Builder object itself.
	 */
	public Builder jobId(String jobId) {
	    this.jobId = jobId;
	    return this;
	}
	
	/**
	 * Create a new instance of TreeAlgorithm.
	 * @return the newly created instance of TreeAlgorithm.
	 */
	public TreeAlgorithm build() {
	    validateInitialization(questions, nInput, nOutput, 
		    nAssignment, nTieAssignment);
	    return new TreeAlgorithm(this);
	}
	
	/*
	 * This function validates the values of parameters input by library users.
	 */
	private void validateInitialization(ArrayList<Object> questions,
		int numberOfInputs,int numberOfOutputs, 
		int numberOfAssignments, int numberOfTieAssignments) {
	    if (questions.size() == 0) {
		throw new TreeAlgorithmException("The size of questions is 0." +
			" [questions.size() == 0]");
	    }
	    if (questions.size() < numberOfInputs) {
		throw new TreeAlgorithmException("The size of questions is" +
			" less than the number of inputs of a HIT." +
			" [questions.size() < numberOfInputs]");
	    }
	    if (questions.size() < numberOfOutputs) {
		throw new TreeAlgorithmException("The size of questions is" +
			" less than the number of outputs of a HIT." +
			" [questions.size() < numberOfOutputs]");
	    }
	    if (numberOfInputs < numberOfOutputs) {
		throw new TreeAlgorithmException("The number of inputs of a HIT" +
			" is less than the number of outputs of a HIT." +
			" [numberOfInputs < numberOfOutputs]");
	    }
	    if (numberOfInputs < 0) {
		throw new TreeAlgorithmException("The number of inputs of a HIT" +
			" is negative." +
			" [numberOfInputs < 0]");
	    }
	    if (numberOfOutputs < 0) {
		throw new TreeAlgorithmException("The number of outputs of a HIT" +
			" is negative." +
			" [numberOfOutputs < 0]");
	    }
	    if (numberOfAssignments < 1) {
		throw new TreeAlgorithmException("The number of assignments of " +
			"a normal HIT is less than 1." +
			" [numberOfAssignments < 1]");
	    }
	    if (numberOfTieAssignments < 1) {
		throw new TreeAlgorithmException("The number of assignments of " +
			"a tie-solving HIT is less than 1." +
			" [numberOfTieAssignments < 1]");
	    }
	}
	
    }
    
    private TreeAlgorithm(Builder builder) {
	this.service = builder.service;
	this.questions = builder.questions;
	this.myHit = builder.myHit;
	this.nInput = builder.nInput;
	this.nOutput = builder.nOutput;
	this.nAssignment = builder.nAssignment;
	this.nTieAssignment = builder.nTieAssignment;
	this.isShuffled = builder.isShuffled;
	this.isLogged = builder.isLogged;
	this.logName = builder.logName;
	this.jobId = builder.jobId;
	this.initEssential();
    }
    
    private void initEssential() {	
	this.activeHitQueue = new ArrayList<EntityHit>();
	this.newItemQueue = new ArrayList<Item>();
	this.levelQueue = new ArrayList<ArrayList<Item>>();
		
	this.tags = new int[this.questions.size()];
	for (int i = 0; i < this.tags.length; i++) {
	    this.tags[i] = 0;
	}
	this.trigger = new boolean[this.questions.size()][this.questions.size()];
	for (int i = 0; i < this.trigger.length; i++) {
	   
	    // the items with tag 0 at each level are all triggered
	    trigger[i][0] = true;
	    
	    for (int j = 1; j < this.trigger[i].length; j++) {
		trigger[i][j] = false;
	    }
	}
	
	this.isNewItemQueueThreadDone = false;
	this.isActiveHitQueueThreadDone = false;
    }
    
    /**
     * Start the algorithm. Please make sure you have all configurations done 
     * before starting the algorithm, since all configurations cannot be 
     * changed during the running process.
     */
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
	
	initActiveHitQueue();
		
	/*
	 * The CheckActiveHitQueueThread keeps checking the active HITs in the 
	 * queue. When it finds a HIT done, it asks for the answer of the HIT.
	 * After it gets answer, it hands results to the NewItemQueue. Then, 
	 * CheckNewItemQueueThread takes charge of the future work. 
	 * 
	 */
	new Thread(new CheckActiveHitQueueThread()).start();
	
	//allow enough time for checkNewItemQueueThread to start
	try {
	    Thread.sleep(1000*2);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
	
	/*
	 * The CheckNewItemQueueThread keeps checking the new items in the 
	 * NewItemQueue. It fetches the results in the NewItemQueue and puts
	 * them into their relevant levelQueue. Then, it checks whether there
	 * are ENOUGH ELIGIBLE items can be used for creating a new HIT.
	 */
	new Thread(new CheckNewItemQueueThread()).start();
    }

    /**
     * Indicate whether this instance is done.
     * 
     * @return <tt>true</tt> if the program is done.
     */
    public boolean isDone() {
	return isActiveHitQueueThreadDone;
    }

    /**
     * Return the final answer.
     * 
     * @return The final answer.
     */
    public Object getFinalAnswer() {
	return finalAnswer;
    }
    
    /*
     * This method creates new HITs based on the parameters, 
     * and keeps them in record -- the activeHitQueue.
     */
    private void initActiveHitQueue() {
	int i = 0;
	
	//Bug fixed
	for (i = 0; i + nInput <= questions.size(); i = i + nInput) {
	    
	    // Get the inputs for a HIT.
	    ArrayList<Object> inputs = new ArrayList<Object>();
	    for (int j = 0; j < nInput; j++) {
		inputs.add(questions.get(i+j));
	    }
	    
	    // Create a HIT with the inputs.
	    String hitId;
	    hitId = myHit.createMyHit(service, inputs, nOutput, nAssignment);
	    int tag = tags[0]++;
	    int level = 0;
	    
	    // Create a new HIT and put it in the queue.
	    activeHitQueue.add(new EntityHit(hitId, level, tag));
	    
	    if (isLogged) {
	        LogWriter.writeTreeCreateHitLog(service, hitId, level, tag, 
	    	                       nAssignment, nOutput, inputs, logName);
	    }
	    
	    
	    inputs.clear();
	}
		
	// Put the rest questions in the newItemQueue.
	for (; i < questions.size(); i++) {
	    newItemQueue.add(new Item(questions.get(i), 0, tags[0]));
	}
    }
    
    
	
    private void checkActiveHitQueue() {
	System.out.println("Checking activeHitQueue ...");

	if (!activeHitQueue.isEmpty()) {
	    
	    /*
	     * Iterate the activeHitQueue to search for the HITs
	     * which are done. 
	     */
	    for (int i = 0; i < activeHitQueue.size(); i++) {
		EntityHit entityHit = activeHitQueue.get(i);
		String hitId = entityHit.getHitId();
		HIT hit = service.getHIT(hitId);
		
		//If the hit is done, request for its answers.
		if (hit.getHITStatus() == HITStatus.Reviewable) {
		    
		    // Allow time to get answers prepared.
		    try {
			Thread.sleep(1000*2);
		    } catch (InterruptedException e) {
			e.printStackTrace();
		    }
		    
		    // Get the answers which have been checked and refined.
		    ArrayList<Object> answers = 
		    	AnswerProcessor.refineRawAnswers(
		    		myHit.getMyHitAnswers(service, hitId), 
		    		nOutput, service, myHit, nTieAssignment, 
		    		isLogged, logName, AnswerProcessor.TREE_ALGORITHM);
		    
		    // Put the new answers into the NewItemQueue.
		    int level = entityHit.getLevel() + 1;
		    int tag = entityHit.getTag();
		    for (int j = 0; j < answers.size(); j++) {
		        newItemQueue.add(
		    	    new Item(answers.get(j), level, tag));
		    }
		    
		    /*
		     * Write the answers to the log. This should be placed
		     * before dumpReviewedHit in case the answers are changed
		     * by other people accidentally.
		     */
		    if (isLogged) {
			LogWriter.writeGetAnswerLog(hitId, answers, logName);
		    }
		    
		    // Do the default operation on this HIT.
		    myHit.dumpPastHit(service, hitId);
		    
		    activeHitQueue.remove(i);
		    i--; // Move the pointer backward one position.
		}
	    }
	}
    }
	
    private void checkNewItemQueue() {
	System.out.println("Checking newItemQueue ...");
	
	while (!newItemQueue.isEmpty()) {
	    Item item = newItemQueue.get(0);
	    int level = item.getLevel();
	    int tag = item.getTag();
	    newItemQueue.remove(0);
	    
	    // If the target levelQueue hasn't been built up, create it.
	    while (levelQueue.size() < level + 1) {
		levelQueue.add(new ArrayList<Item>());
	    }
	    
	    // Put the item into its levelQueue.
	    levelQueue.get(level).add(item);
	    
	    try {
		Thread.sleep(1000);
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	    
	    /*
	     *  Trigger the items with next tag so that when they arrive  
	     *  we can create a HIT using them.
	     */
	    trigger[level][tag+1]=true;
	    
	    /*
	     *  When the newItemQueue is empty, update the levelQueue.
	     *  When the level of the next item is different from this one's, 
	     *  update the levelQueue of this item too.
	     */
	    if (newItemQueue.isEmpty()
		    || newItemQueue.get(0).getLevel() != level) {
		checkLevelQueue(level);
	    }
	}
	
	/*
	 * At last, check each levelQueue, so as not to miss some eligible
	 * items. For example, after updating the previous levelQueue, 
	 * the topmost levelQueue which was created in the process may have 
	 * enough items to create a new HIT.
	 */
	for (int i = 0; i < levelQueue.size(); i++) {
	    if (!levelQueue.get(i).isEmpty()) {
		checkLevelQueue(i);
	    }
	}
    }
	
    private void checkLevelQueue(int level) {
	System.out.println("Checking No." + level + " levelQueue ...");
	
	// If the levelQueue at this level is empty, return.
	if (levelQueue.get(level).isEmpty()) {
	    return;
	}
	
	// Pre-process the queue -- sort it!
	sortLevelQueueByTag(levelQueue.get(level));
	
	/*
	 * If the first item of this queue is triggered, we can create 
	 * a HIT as long as we have enough items. If not, even though
	 * other items come in, we cannot create a HIT with them, since
	 * this will break the structure of the "tree".
	 */
	Item firstItem = levelQueue.get(level).get(0);
	if (trigger[level][firstItem.getTag()]) {
	    
	    // If the first item is triggered, start to get items of this level.
	    while (!levelQueue.get(level).isEmpty()) {
		boolean canCreateAHit = false;
		int queueSize = levelQueue.get(level).size(); 
		
		/*
		 *  1st condition: If the amount of items meets the requested 
		 *  input amount of creating a HIT.
		 */
		if (queueSize >= nInput) {
		    canCreateAHit = true;
		}
		
		/*
		 * 2nd condition: If the order of the following items are 
		 * consecutive, we can use them to create a HIT. The 
		 * consecutiveness should be met if we want to build an exact 
		 * tree. The consecutiveness guarantees only the items from 
		 * the next "neighbor" HIT can be combined with the items from  
		 * this HIT to create a new HIT.
		 * 
		 * Bug fixed: i should start from 1.
		 */
		for (int i = 1; i < nInput && i < queueSize; i++) {
		    int preTag = levelQueue.get(level).get(i).getTag();
		    int nextTag = levelQueue.get(level).get(i-1).getTag();
		    if (preTag - nextTag > 1) {
			canCreateAHit = false;
			System.out.println("Order Wrong!");
			break;
		    }
		}
		
		/*
		 * Once we cannot create a HIT, we stop checking the rest items
		 * and jump out of the while loop, namely stop attempting 
		 * create a HIT with the items at this level.
		 */
		if (!canCreateAHit) {
		    break;
		} else {
		    
		    /*
		     * Fetch the eligible items for the new HIT, extract the 
		     * questions and put these questions into the inputs array. 
		     */
		    ArrayList<Object> inputs = new ArrayList<Object>();
		    for (int i = 0; i < nInput; i++) {
			inputs.add(levelQueue.get(level).get(0).getQuestion());
			levelQueue.get(level).remove(0);
		    }
		      
		    String hitId = myHit.createMyHit(
			    service, inputs, nOutput, nAssignment);
		    
		    // Update the tag of this level.
		    int tag = tags[level]++;
		    
		    // Put the new HIT in the activeHitQueue
		    activeHitQueue.add(new EntityHit(hitId, level, tag));
		    
		    //write to log
		    if (isLogged) {
			LogWriter.writeTreeCreateHitLog(service, hitId, level, tag,
				nAssignment, nOutput, inputs, logName);
		    }
		    
		    inputs.clear();
		}
	    }
	} else {
	    System.out.println("Not Triggered.");
	}
    }
    
    /*
     * If there are not enough eligible items to create a HIT 
     * at each level, the first round is done. But there are probably 
     * some remaining items at some levels. They cannot be used for 
     * creating HITs due to the limit of input number and short of 
     * enough items.  However, they should also be processed in the 
     * future.
     */
    private boolean isFirstRoundDone() {
	
	if (!activeHitQueue.isEmpty() || !newItemQueue.isEmpty()) {
	    return false;
	}
	
	// Check every level to see if its has enough items for a new HIT.
	for (int i = 0; i < levelQueue.size(); i++) {
	    if (levelQueue.get(i).size() >= nInput) {
		return false;
	    }
	}
	return true;
    }

    /*
     * If there are no items left at every level except the topmost, the 
     * second round which is for process the remaining items is done.
     */
    private boolean isSecondRoundDone() {
	
	if (!isFirstRoundDone()) {
	    return false;
	}
	
	for (int i = 0; i < levelQueue.size() - 1; i++) {
	    if (!levelQueue.get(i).isEmpty()) {
		return false;
	    }
	}
	return true;
    }

    
    private ArrayList<Item> sortLevelQueueByTag(ArrayList<Item> levelQueue) {
	for (int i = 0; i < levelQueue.size(); i++) {
	    for (int j = i; j < levelQueue.size(); j++) {
		if (levelQueue.get(i).getTag() > levelQueue.get(j).getTag()) {
		    Item temp = levelQueue.get(i);
		    levelQueue.set(i, levelQueue.get(j));
		    levelQueue.set(j, temp);
		}
	    }
	}
	return levelQueue;
    }
    
    private class CheckActiveHitQueueThread implements Runnable{
	public void run() {
	    while (!isNewItemQueueThreadDone) {
		checkActiveHitQueue();
		try {
		    Thread.sleep(1000*5);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
	    }
	    
	    String log = "";
	    log += "Thread: CheckActiveHitQueueThread is done.\n\n";
	    log += "The tree algorithm ended at " + 
	    		new Date().toString() + "\n\n";
	    log += "The final answer is: " + finalAnswer;
	    if (isLogged) {
		LogWriter.writeLog(log, logName);
	    }
	    System.out.println(log);
	    
	    isActiveHitQueueThreadDone = true;
	}
    }
    
    private class CheckNewItemQueueThread implements Runnable{
	public void run() {
	    while (!isSecondRoundDone()) {
		while (!isFirstRoundDone()) {
		    checkNewItemQueue();
		    try {
			Thread.sleep(1000*5);
		    } catch (InterruptedException e) {
			e.printStackTrace();
		    }
		}
		
		/*
		 * Process the remaining items at each level, except
		 * the topmost level.
		 */
		for (int i = 0; i < levelQueue.size()-1; i++) {
		    
		    /*
		     * If this levelQueue is not empty, move the items
		     *  to the upper level, and check that level.
		     */
		    if (!levelQueue.get(i).isEmpty()) {
			
			while (!levelQueue.get(i).isEmpty()) {
			    Item item = levelQueue.get(i).get(0);
			    levelQueue.get(i).remove(0);							
			    
			    Object question = item.getQuestion();
			    int level = item.getLevel() + 1;
							
			    //Bug fixed.
			    int tag = 0;
			    if (levelQueue.get(level).size() == 0) {
				
				/* 
				 * If no items exist at this level, 
				 * set the tag 0. However, when it goes up to 
				 * the upper level, it will be evaluated again!
				 */
				tag = -2;
			    } else {
				
				/*
				 * Get the tag of the last element at the 
				 * higher level.
				 */
				int queueSize = levelQueue.get(level).size();
				tag = levelQueue.get(level).
					get(queueSize -1).getTag();
			    }
			    
			    levelQueue.get(level).add(
				    new Item(question, level, tag));
			    
			    if (isLogged) {
				String log = "";
				log += "Move [" + item.getQuestion() + 
//					", " + item.getLevel() + 
//					", " + item.getTag() + 
					"] to level " + level + "\n\n";
				LogWriter.writeLog(log, logName);
				System.out.println(log);
			    }

			}
			
			/*
			 * If the levelQueue at this level is not empty, use 
			 * break to break the for loop, then the program 
			 * goes to outer while loop which checks 
			 * isSecondRoundDown. This can guarantee if there are 
			 * new HITs coming up after the first round, it will 
			 * still be processed.
			 */
			break;
		    }
		}		
	    }
	    
//	    checkNewItemQueue();
			
	    /*
	     * If there are more than one answers at the topmost level, 
	     * we should create the last HIT for the final answer. 
	     * If there is only one answer, return it as the final answer.
	     */
	    if (levelQueue.get(levelQueue.size()-1).size() > 1) {
		
		// Get all elements at the top level.
		int num = levelQueue.get(levelQueue.size()-1).size();
		ArrayList<Object> inputs = new ArrayList<Object>();
		for (int i = 0; i < num; i++) {
		    inputs.add(levelQueue.get(levelQueue.size()-1).
			    get(0).getQuestion());
		    levelQueue.get(levelQueue.size()-1).remove(0);
		}
		
		String hitId = myHit.createMyHit(service, 
		    inputs, 1, nAssignment);
		
		if (isLogged) {
		    LogWriter.writeTreeCreateHitLog(service, hitId, 
			    levelQueue.size()-1, tags[levelQueue.size()-1], 
			    nAssignment, 1, inputs, logName);   
		}
		
		inputs.clear();
		
		// Wait until the hit is done.
		HIT hit = service.getHIT(hitId);
		while (hit.getHITStatus() != HITStatus.Reviewable) {
		    try {
			Thread.sleep(1000*5);
		    } catch (InterruptedException e) {
			e.printStackTrace();
		    }
		    hit = service.getHIT(hitId);
		}
		
		// Allow enough time to get the answers.
		try {
		    Thread.sleep(1000*2);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
		
		ArrayList<Object> rawAnswers = myHit.getMyHitAnswers(
			service, hitId);
		ArrayList<Object> answers = AnswerProcessor.refineRawAnswers(
			rawAnswers, 1, service, myHit, nTieAssignment, 
			isLogged, logName, AnswerProcessor.TREE_ALGORITHM);
		finalAnswer = answers.get(0);
		
		if (isLogged) {
		    LogWriter.writeGetAnswerLog(hitId, answers, logName);
		}
		
		String info = "Thread: CheckNewItemQueueThread is done.\n\n";
		LogWriter.writeLog(info, logName);
		System.out.println(info);
		
		myHit.dumpPastHit(service, hitId);
		
		isNewItemQueueThreadDone = true;
	    } else {
		
		// Bug fixed.
		finalAnswer = levelQueue.get(levelQueue.size()-1).get(0).getQuestion();
		
		String info = "Thread: CheckNewItemQueueThread is done.\n\n";
		LogWriter.writeLog(info, logName);
		System.out.println(info);
		
		isNewItemQueueThreadDone = true;
	    }
	}
    }
}	
