package edu.ucsc.cs.mturk.lib.topone;

import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.*;
import java.util.*;

public class TopOneServer {
    private static int serverPort;
    private static int clientPort;
    private static String clientIp;
    private static String jobId;
//    protected static int myPort = 11024;
//    protected static int hisPort = 11025;
//    protected static String hisIP = "stardance.cse.ucsc.edu";
//    protected static int serverID = (new Date()).hashCode();//for log
//    protected static int threadID = 0;

    /*
     * 1st parameter - the type of algorithm, "tree" or "bubble"
     * 2nd parameter - the port of the server side
     * 3rd parameter - the port of the client side
     * 4th parameter - the IP of the client side
     * 5th parameter - the question string
     */
    public static void main(String[] args) throws Exception{
	
	/*
	 * Validate the 1st parameter which is supposed to be the type 
	 * of algorithm.
	 */
	if (!args[0].equals("tree")
		|| !args[0].equals("bubble")) {
	    throw new TopOneServerException("The first parameter must" +
	    		" be \"tree\" or \"bubble\".");
	}
	
	/*
	 * Validate the 2nd parameter which is supposed to be the port
	 * of the server side.
	 */
	try {
	    serverPort = Integer.parseInt(args[1]);
	} catch (NumberFormatException e) {
	    throw new TopOneServerException("The server port must " +
	    		"be an integer.");
	}
	if (serverPort < 1 || serverPort > 65535) {
	    throw new TopOneServerException("The server port must " +
	    		"be between 1 and 65535.");
	}
	
	/*
	 * Validate the 3rd parameter which is supposed to be the port
	 * of the client side.
	 */
	try {
	    clientPort = Integer.parseInt(args[2]);
	} catch (NumberFormatException e) {
	    throw new TopOneServerException("The client port must " +
	    		"be an integer.");
	}
	if (clientPort < 1 || clientPort > 65535) {
	    throw new TopOneServerException("The client port must " +
	    		"be between 1 and 65535.");
	}
	if (clientPort == serverPort) {
	    throw new TopOneServerException("The client port must " +
	    		"be different from the server port.");
	}
	
	
	
	
	
	
	
	
	ServerSocket ss = new ServerSocket(myPort);
	System.out.println("TreeServer is started at " + (new Date()).toString());
	while(true){
	    System.out.println("TreeServer is listening ...");
	    Socket s = ss.accept();
	    new Thread(new ServerThread(s)).start();
	}
    }
}

class ServerThread implements Runnable{
    Socket s = null;
    String id = null;
    BufferedReader br = null;
    PrintStream ps = null;
    ServerThread(Socket s) throws IOException{
	this.s = s;
	this.br = new BufferedReader(new InputStreamReader(s.getInputStream()));
	this.ps = new PrintStream(s.getOutputStream());
    }
    private static void parseToMap(HashMap<String, String> hm, String str){
	String[] key_value = str.split("&");
	for(String kv: key_value){
	    String[] s = kv.split("=");
	    hm.put(s[0], s[1]);
	}
    }
    public void run(){
	try{
	    //display connection established
	    System.out.println("Connection established from: "+s.getInetAddress().getHostName()+":"+s.getLocalPort() );
	    
	    //block and wait, then read the content
	    String param = br.readLine();
	    System.out.println("The string read from socket is: " + param);
	    
	    //parse the string
	    HashMap<String, String> hm = new HashMap<String, String>();
	    parseToMap(hm, param);
	    
	    //initialize the input questions
	    ArrayList<Object> inputs = new ArrayList<Object>();
	    for(int i = 0; i < Integer.parseInt(hm.get("qnumber").toString()); i++){
		System.out.println("add to input: " + hm.get("q"+i));
		inputs.add(hm.get("q"+i));//get the inputs/questions
	    }
	    
	    //return confirmation and close the socket
	    System.out.println("Returned jobid=" + hm.get("jobid").toString());
	    ps.println("jobid=" + hm.get("jobid").toString());
	    ps.flush();
	    ps.close();
	    br.close();
	    s.close();
	    System.out.println("Initial sockect closed.");
	    
	    //start and initialize the algorithm
//		TreeAlgorithm alg = new TreeAlgorithm(inputs);
//			alg.setImgFile(hm.get("imgfile").toString());
//			alg.setJobID(hm.get("jobid").toString());
//			alg.run();
			
			//wait until the algorithm is done
//			while(!alg.isOver()){
//				try {
//					Thread.sleep(1000*5);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
			
			//get a new socket to return the final answer
			Socket skt = new Socket(TreeServer.hisIP, TreeServer.hisPort);
			PrintStream returnPs = new PrintStream(skt.getOutputStream());
//			String returnAnswerString = "qtype=sendAnswer&" + 
//					"jobid="+alg.getJobID() + "&" + 
//					"answer=" + alg.getFinalAnswer().toString();
//			returnPs.println(returnAnswerString);
//			returnPs.flush();
//			System.out.println("Returned string is: "+returnAnswerString);
			
			//waiting for the feedback
			BufferedReader feedbackBr = new BufferedReader(new InputStreamReader(skt.getInputStream()));
			String feedback = feedbackBr.readLine();
			System.out.println("The feedback info: " + feedback);
			
			//close the socket
			returnPs.close();
			feedbackBr.close();
			skt.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
}
