package edu.ucsc.cs.mturk.server.topone;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import com.amazonaws.mturk.requester.HIT;
import com.amazonaws.mturk.requester.HITStatus;
import com.amazonaws.mturk.service.axis.RequesterService;

class AnswerProcessor {
    static final int TREE_ALGORITHM = 0;
    static final int BUBBLE_ALGORITHM = 1;
    
    /*
     * The answers must:
     * 1. be as many as the requested number of outputs.
     * 2. not have same answers.
     * Therefore, we have to refine the raw answers to make it 
     * meet these requirements.
     * 
     * Here should be an exception check, make sure nOutput 
     * is less than the size of rawAnswers!
     */
    static ArrayList<Object> refineRawAnswers(
	    ArrayList<Object> rawAnswers, RequesterService service, 
	    int nOutput, int nTieAssignment,
	    boolean isLogged, String jobId, int algorithm,
	    String clientIp, int clientPort) {
	ArrayList<Object> answers = new ArrayList<Object>();
	
	// If we happen to have the exact amount of answers we want, return them.
	if (rawAnswers.size() == nOutput) {
	    return rawAnswers;
	}
	
	if (rawAnswers.size() < nOutput) {
	    throw new TreeAlgorithmException("Too few answers returned." +
	    		"The number of total returned answers " +
	    		"is " + rawAnswers.size() + ". The " +
	    		"number of required answers is " + nOutput + ".");
	}
	
	/*
	 * Initialize the content and count array which record the answers 
	 * returned and its amount.
	 */
	Object[] contents = new Object[rawAnswers.size()];
	int[] counts = new int[rawAnswers.size()];
	//NOTE: Here might be exception
	for (int i = 0; i < rawAnswers.size(); i++) {
	    int j = 0;
	    while (contents[j] != null && 
		    !contents[j].equals(rawAnswers.get(i))) {
		j++;
	    }
	    if (contents[j] == null) {
		contents[j] = rawAnswers.get(i);
	    }
	    counts[j]++;
	}
		
	/*
	 * Sort the contents by counts.
	 */
	for (int i = 0; i < contents.length; i++) {
	    for (int j = i; j < contents.length; j++) {
		if (counts[i] < counts[j]) {
		    Object tempContent = contents[i];
		    int tempCount = counts[i];
		    contents[i] = contents[j];
		    counts[i] = counts[j];
		    contents[j] = tempContent;
		    counts[j] = tempCount;
		}
	    }
	}
	
	if (counts[nOutput - 1] < 1) {
	    throw new TreeAlgorithmException("Too many same answers" +
	    		" returned by a HIT.");
	}

	// Check if there is a tie.
	if (counts[nOutput-1] == counts[nOutput] 
		&& contents[nOutput-1] != null) {
	    answers = solveTie(contents, counts, nOutput,
		    service, nTieAssignment, isLogged,
		    jobId, algorithm, clientIp, clientPort); // Solve tie.
	} else {
	    for (int i = 0; i < nOutput; i++) {
		answers.add(contents[i]);
	    }
	}
	return answers;
    }
	
    /*
     * When a tie comes up, we have to solve it. A tie may happen when 
     * there are some answers have the same votes. Therefore, it is too many.
     * We have to ask additional workers to solve the tie.
     */
    private static ArrayList<Object> solveTie(Object[] content, 
	    int[] count, int nOutput, RequesterService service,
	    int nTieAssignment, boolean isLogged,
	    String jobId, int algorithm, String clientIp,
	    int clientPort) {
	ArrayList<Object> inputs = new ArrayList<Object>();
	ArrayList<Object> outputs = new ArrayList<Object>();
	
	
	// Evaluate outputs with the answers which don't cause the tie.
	int i = 0;
	while (i < count.length && count[i] != count[nOutput-1]) {
	    outputs.add(content[i]);
	    i++;
	}	
	
	// Evaluate inputs with the tie answers.
	while (i < count.length && count[i] == count[nOutput-1]) {
	    inputs.add(content[i]);
	    i++;
	}
	
	if (isLogged) {
	    String log = "A tie casued by the following answers:\n";
	    for (int j = 0; j < inputs.size(); j++) {
		log += "" + inputs.get(j).toString() + "\n";
	    }
	    log += "\n";
	    LogWriter.writeLog(log, jobId);
	    System.out.println(log);
	}
	
	// Create a new tie-solving HIT with the tie answers.
	int nOutputOfTie = nOutput-outputs.size();
	String hitId = null;
	try {
	    hitId = HitOperation.createHit(inputs, nOutputOfTie, 
	    	nTieAssignment, clientIp, clientPort, jobId);
	} catch (UnknownHostException e1) {
	    throw new TopOneServerException("UnknownHostException." +
	    		"Please check the IP and port of client socket.");
	} catch (IOException e1) {
	    throw new TopOneServerException("IOException." +
	    		"Please check the IP and port of client socket.");
	} 
//	String hitId = myHit.createMyHit(
//	    	service, inputs, nOutputOfTie, nTieAssignment);
	
	if (isLogged) {
	    if (algorithm == AnswerProcessor.TREE_ALGORITHM) {
		LogWriter.writeTreeCreateHitLog(service, hitId, -1, -1, 
			    nTieAssignment, nOutputOfTie, inputs, jobId);
	    } else if (algorithm == AnswerProcessor.BUBBLE_ALGORITHM) {
		LogWriter.writeBubbleCreateHitLog(service, hitId,
			    nTieAssignment, nOutputOfTie, inputs, jobId);
	    }
	    
	}
		
	inputs.clear();
	
	// Wait until the tie-solving HIT's answers come out.
	HIT hit = service.getHIT(hitId);
	while (hit.getHITStatus() != HITStatus.Reviewable) {
	    try {
		Thread.sleep(1000*3);
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	    hit = service.getHIT(hitId); // Refresh the HIT
	}
	
	// Get the answer of the HIT.
	ArrayList<Object> answers = null;
	try {
	    answers = refineRawAnswers(
	    	HitOperation.getAnswer(hitId, clientIp, clientPort, jobId),
	    	service, nOutputOfTie, nTieAssignment, isLogged, jobId,
	    	algorithm, clientIp, clientPort);
	} catch (UnknownHostException e) {
	    throw new TopOneServerException("UnknownHostException." +
	    		"Please check the IP and port of client socket.");
	} catch (IOException e) {
	    throw new TopOneServerException("IOException." +
	    		"Please check the IP and port of client socket.");
	}
	
//	ArrayList<Object> rawAnswersOfTie = 
//		myHit.getMyHitAnswers(service, hitId);
//	ArrayList<Object> answersOfTie = 
//		refineRawAnswers(rawAnswersOfTie, nOutputOfTie, service,
//			    myHit, nTieAssignment, isLogged, jobId, algorithm);
	
	// Write to log.
	if (isLogged) {
	    LogWriter.writeGetAnswerLog(hitId, answers, jobId);
	}
	
	// Do the default operation on this HIT.
//	myHit.dumpPastHit(service, hitId);
		    
	// Append the new answers to outputs.
	for (int j = 0; j < answers.size(); j++) {
	    outputs.add(answers.get(j));
	}

	return outputs;
    }
}
