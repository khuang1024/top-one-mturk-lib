package edu.ucsc.cs.mturk.demo;

import java.io.IOException;
import java.io.PrintStream;
//import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class DemoTopOneServer {
    
    // DEBUG
    private static final String algorithmServerIp = "127.0.01";
    // DEBUG
    
    private static final int algorithmServerPort = 50000;
    private static final int clientServerPort = 50001;
    private static final String propertyFileName = "mturk.properties";

    /*
     * Run this class to demo the server.
     */
    public static void main(String[] args) 
	    throws NumberFormatException, UnknownHostException, IOException {

	/*
	 * initSocket is used for create the initialization string which invokes
	 * the server to create an instance of corresponding algorithm and run
	 * the instance. This instance will be running all the time until it 
	 * reaches the final answer.
	 */
	Socket initSocket = new Socket(algorithmServerIp, algorithmServerPort);
	PrintStream ps = new PrintStream(initSocket.getOutputStream());
	
	StringBuilder initRequestBuilder = new StringBuilder(170);
	initRequestBuilder.append("qnum=19&");
	initRequestBuilder.append("q0=1&");
	initRequestBuilder.append("q1=2&");
	initRequestBuilder.append("q2=3&");
	initRequestBuilder.append("q3=4&");
	initRequestBuilder.append("q4=5&");
	initRequestBuilder.append("q5=6&");
	initRequestBuilder.append("q6=7&");
	initRequestBuilder.append("q7=8&");
	initRequestBuilder.append("q8=9&");
	initRequestBuilder.append("q9=10&");
	initRequestBuilder.append("q10=11&");
	initRequestBuilder.append("q11=12&");
	initRequestBuilder.append("q12=13&");
	initRequestBuilder.append("q13=14&");
	initRequestBuilder.append("q14=15&");
	initRequestBuilder.append("q15=16&");
	initRequestBuilder.append("q16=17&");
	initRequestBuilder.append("q17=18&");
	initRequestBuilder.append("q18=19&");
	initRequestBuilder.append("nInput=3&");
	initRequestBuilder.append("nOutput=2&");
	initRequestBuilder.append("nAssignment=2&");
	initRequestBuilder.append("nTieAssignment=1");
	
	/*
	 * Write the initial request to server to start an instance 
	 * of algorithm.
	 */
	ps.println(initRequestBuilder.toString());
	ps.close();
	
	
	/*
	 * Open a "server" on client side to process the requests
	 * from server, such as creating HITs and getting answers.
	 */
	ServerSocket ss = new ServerSocket(clientServerPort);
	while (true) {
//	    System.out.println("Client is listening ...");
	    
	    // DEBUG
//	    InetAddress localHost = InetAddress.getLocalHost();
//	    System.out.println("IP=" + localHost.getHostAddress());
//	    System.out.println("Port=" + clientServerPort);
	    // DEBUG
	    
	    Socket s = ss.accept();
	    new Thread(new DemoTopOneServerThread(s, propertyFileName)).start();
	}
	
    }

}
