package edu.ucsc.cs.mturk.lib.topone;

import java.net.*;
import java.util.ArrayList;
import java.util.Date;
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
	
	/* Create the server.*/
	ServerSocket ss = new ServerSocket(serverPort);
	
	
	String info = "TopOneServer has been started.\n";
	info += "+--------------------------+---------------------------+\n";
	info += "| Algorithm                | " + args[0] + "\n";
	info += "+--------------------------+---------------------------+\n";
	info += "| Server Listening Port    | " + args[1] + "\n";
	info += "+--------------------------+---------------------------+\n";
	info += "| Client Listening Port    | " + args[2] + "\n";
	info += "+--------------------------+---------------------------+\n";
	info += "| Client IP Address        | " + args[3] + "\n";
	info += "+--------------------------+---------------------------+\n";
	info += "| Start Time               |" + new Date().toString() + "\n";
	info += "+--------------------------+---------------------------+\n\n";
	System.out.println(info);
	
	while(true){
	    System.out.println("TreeServer is listening ...");
	    Socket s = ss.accept();
	    new Thread(new ServerThread(s)).start();
	}
    }
}