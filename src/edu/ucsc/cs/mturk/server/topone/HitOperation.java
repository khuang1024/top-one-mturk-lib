package edu.ucsc.cs.mturk.server.topone;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

class HitOperation {
    
    /**
     * 
     * @param inputs
     * @param nOutput
     * @param nAssignment
     * @param clientIp
     * @param clientPort
     * @param jobId
     * @return
     * @throws IOException 
     * @throws UnknownHostException 
     */
    static String createHit(ArrayList<Object> inputs, int nOutput, 
	    int nAssignment, String clientIp, int clientPort, String jobId)
		    throws UnknownHostException, IOException {
	Socket s = new Socket(clientIp, clientPort);
	PrintStream ps = new PrintStream(s.getOutputStream());

	String request = "";
	request += "type=createHit&" +
		  "nOutput=" + nOutput + "&" +
		  "nAssignment=" + nAssignment + "&" +
		  "jobId=" + jobId + "&" + 
		  "qnum=" + (inputs.size());
	for(int i = 0; i < inputs.size(); i++){
		request += "&q" + i + "=" + inputs.get(i).toString();
	}
	
	/*
	 * An example of a request string is:
	 * type=createHit&nOutput=2&nAssignment=3&jobId=1831723&
	 * qnum=3&q0=3&q1=13&q2=7
	 */
	ps.println(request);
	ps.flush();

	BufferedReader br = new BufferedReader(
		new InputStreamReader(s.getInputStream()));
	String hitID = br.readLine();
	return hitID;
    }
    
    static ArrayList<Object> getAnswer(String hitId, String clientIp, 
	    int clientPort, String jobId)
		    throws UnknownHostException, IOException {
	
	//socket
	Socket s = new Socket(clientIp, clientPort);
	PrintStream ps = new PrintStream(s.getOutputStream());
	BufferedReader br = new BufferedReader(
		new InputStreamReader(s.getInputStream()));
	
	String request = "";
	request += "type=getAnswer&" + 
		   "hitId=" + hitId + "&" +
		   "jobId=" + jobId;
	
	/*
	 * An example of request string is:
	 * type=getAnswer&hitId=JASDH7SD9S9&jobId=1831723
	 */
	ps.println(request);
	ps.flush();

	String returnedString = br.readLine();
	s.close();
	
	/*
	 * An example of returned string is:
	 * anum=2&a0=3&a1=7
	 */
	System.out.println("Returned string: " + ": " + returnedString);
	HashMap<String, String> hm = StringParser.parseToMap(returnedString);
	ArrayList<Object> rawAnswers = StringParser.extractAnswers(hm);
	
	
	return rawAnswers;
    }

}
