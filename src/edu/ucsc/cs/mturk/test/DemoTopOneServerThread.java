package edu.ucsc.cs.mturk.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import com.amazonaws.mturk.service.axis.RequesterService;
import com.amazonaws.mturk.util.PropertiesClientConfig;

class DemoTopOneServerThread implements Runnable{
    Socket s;
    BufferedReader br;
    PrintStream ps;
    RequesterService service;
    boolean isDone;
    
    DemoTopOneServerThread(Socket s, String propertyFileName) {
	this.s = s;
	try {
	    this.br = new BufferedReader(
	    	new InputStreamReader(this.s.getInputStream()));
	} catch (IOException e) {
    	    new RuntimeException("Cannot create BufferedReader" +
    	    		"for DemoTopOneServerThread.");
	}
	try {
	    this.ps = new PrintStream(s.getOutputStream());
	} catch (IOException e) {
	    new RuntimeException("Cannot create PrintStream" +
	    		"for DemoTopOneServerThread.");
	}
	this.service = new RequesterService(
		new PropertiesClientConfig(System.getProperty("user.dir") +
			java.io.File.separator + propertyFileName));
    }
    public void run() {
	
	String request = null;
	try {
	    request = br.readLine();
	} catch (IOException e) {
	    new RuntimeException("Cannot use BufferedReader to read line " +
	    		"in DemoTopOneServerThread.");
	}
	
	HashMap<String, String> hm = StringParser.parseToMap(request);
	String type = StringParser.extractType(hm);
	if (type.equals("createHit")) {
	    int nOutput = StringParser.extractNumberOfOutputs(hm);
	    int nAssignment = StringParser.extractNumberOfAssignments(hm);
	    ArrayList<Object> inputs = StringParser.extractQuestions(hm);
	    DemoHit demoHit = new DemoHit();
	    
	    ps.println(demoHit.createMyHit(service, inputs, nOutput, nAssignment));
	    ps.close();
//	    s.close();
	    
	} else if (type.equals("getAnswer")) {
	    DemoHit demoHit = new DemoHit();
	    ArrayList<Object> answers = demoHit.getMyHitAnswers(service, 
		    StringParser.extractHitId(hm));
	    
	    String returnedAnswer = "";
	    returnedAnswer += "anum=" + Integer.toString(answers.size());
	    for (int i = 0; i < answers.size(); i++) {
		returnedAnswer += "&a" + i + "=" + answers.get(i).toString();
	    }
	    
	    ps.println(returnedAnswer);
	    ps.close();
//	    s.close();
	    
	} else if (type.equals("returnFinalAnswer")) {
	    String finalAnswer = StringParser.extractFinalAnswer(hm);
	    
	    // Some code for dealing with the final answer.
	    System.out.println(finalAnswer);
	}
    }
}
