package edu.ucsc.cs.mturk.server.topone;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

class TopOneServerThread implements Runnable {
    
    // Connection parameters.
    private final Socket s;
    private final int clientPort;
    private final String clientIp;
    private final String algorithmType;
    private String jobId;
    
    // HIT constructor parameters.
    private ArrayList<Object> questions;
    private int nInput;
    private int nOutput;
    private int nAssignment;
    private int nTieAssignment;
    private boolean isShuffled;
    private boolean isLogged;
    private final String propertyFileName;
    
    // IO operation.
    private final BufferedReader br;
//    private final PrintStream ps;
    
    TopOneServerThread(Socket s, String algorithmType, int clientPort, 
	    String clientIp, String propertyFileName) 
		    throws IOException {
	this.s = s;
	this.algorithmType = algorithmType;
	this.clientPort = clientPort;
	this.clientIp = clientIp;
	
	this.propertyFileName = propertyFileName;
	
	this.br = new BufferedReader(new InputStreamReader(this.s.getInputStream()));
//	this.ps = new PrintStream(s.getOutputStream());
	
	this.jobId = Integer.toString(new Date().hashCode());
    }
    
    //
    public void run() {
	    // Display connection info.
	    String info = "A new connection is established with: \n";
	    info += "+---------------+-------------------------------+\n";
	    info += "|      IP       | " + clientIp + "\n";
	    info += "+---------------+-------------------------------+\n";
	    info += "|      Port     | " + clientPort + "\n";
	    info += "+---------------+-------------------------------+\n";
	    info += "|     Time      | " + new Date().toString() + "\n";
	    info += "+---------------+-------------------------------+\n\n";
	    
	    // Block and wait for request string.
	    String request;
	    try {
		request = br.readLine();
	    } catch (IOException e1) {
		throw new TopOneServerException("Cannot read line " +
				"from client socket.");
	    }
	    
	    // Close the socket.
	    try {
		br.close();
	    } catch (IOException e2) {
		throw new TopOneServerException("Cannot close the " +
			"BufferedReader for receiving initial request " +
			"from client.");
	    }
//	    try {
//		s.close();
//	    } catch (IOException e2) {
//		throw new TopOneServerException("Cannot close socket" +
//			" for receiving initial request from client.");
//	    }
	    System.out.println("Initial sockect closed.");
	    
	    /*
	     * Parse all the parameters.
	     * An example:
	     * qnum=19&
	     * q0=1&
	     * q1=2&
	     * q2=3&
	     * q3=4&
	     * q4=5&
	     * q5=6&
	     * q6=7&
	     * q7=8&
	     * q8=9&
	     * q9=10&
	     * q10=11&
	     * q11=12&
	     * q12=13&
	     * q13=14&
	     * q14=15&
	     * q15=16&
	     * q16=17&
	     * q17=18&
	     * q18=19&
	     * nInput=3&
	     * nOutput=2&
	     * nAssignment=2&
	     * nTieAssignment=1&
	     * isShuffled=true&	// can be omitted
	     * isLogged=true&	// can be omitted
	     * jobId=HIU7QWE92	// can be omitted
	     * \n		// don't forget \n
	     */
	    HashMap<String, String> hm = StringParser.parseToMap(request);
	    questions = StringParser.extractQuestions(hm);
	    nInput = StringParser.extractNumberOfInputs(hm);
	    nOutput = StringParser.extractNumberOfOutputs(hm);
	    nAssignment = StringParser.extractNumberOfAssignments(hm);
	    nTieAssignment = StringParser.extractNumberOfTieAssignments(hm);
	    isShuffled = StringParser.extractIsShuffled(hm);
	    isLogged = StringParser.extractisLogged(hm);
	    jobId = StringParser.extractJobId(hm);
	    
	    // Output the info to stdout and write to log whose name is jobId.
	    info += "Request string received from client: " + request + "\n\n";
	    System.out.println(info);
	    if (isLogged) {
		LogWriter.writeLog(info, jobId);
	    }
	    
	    // Create a new instance of an algorithm.
	    Algorithm algorithm = null;
	    if (algorithmType.equals("tree")) {
		algorithm = new TreeAlgorithm(questions, nInput, 
			nOutput, nAssignment, nTieAssignment, isShuffled, 
			isLogged, jobId, clientIp, clientPort, 
			propertyFileName);
	    } else {
		algorithm = new  BubbleAlgorithm(questions, nInput, 
			nOutput, nAssignment, nTieAssignment, isShuffled, 
			isLogged, jobId, clientIp, clientPort, 
			propertyFileName);
	    }
	    
	    // Start the instance of algorithm.
	    algorithm.start();
	    
	    // Wait until the algorithm is done.
	    while (!algorithm.isDone()) {
		try {
		    Thread.sleep(1000*5);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
	    }
	    
	    // Fetch the final answer.
	    String finalAnswer = algorithm.getFinalAnswer().toString();
	    
	    Socket sForFinalAnswer = null;
	    try {
		sForFinalAnswer = new Socket(clientIp, clientPort);
	    } catch (UnknownHostException e) {
		throw new TopOneServerException("Cannot create the socket for " +
			"returning the final answer. Please check the " +
			"IP and port of client.");
	    } catch (IOException e) {
		throw new TopOneServerException("Cannot create the socket for " +
			"returning the final answer. Please check the " +
			"IP and port of client.");
	    }
	    PrintStream psForFinalAnswer;
	    try {
		psForFinalAnswer = new PrintStream(
		    sForFinalAnswer.getOutputStream());
	    } catch (IOException e) {
		throw new TopOneServerException("Cannot create the " +
			"PrinStream for returning the final answer. " +
			"Please check the IP and port of client.");
	    }
	    
	    /*
	     * An example:
	     * type=returnFinalAnswer&finalAnswer=6
	     */
	    String finalAnswerString = "";
	    finalAnswerString += "type=returnFinalAnswer&";
	    finalAnswerString += "finalAnswer=" + finalAnswer;
	    
	    psForFinalAnswer.println(finalAnswerString);
	    psForFinalAnswer.flush();
	    psForFinalAnswer.close();
//	    try {
//		sForFinalAnswer.close();
//	    } catch (IOException e) {
//		throw new TopOneServerException("Cannot close the socket for " +
//			"returning the final answer.");
//	    }
    }
}
