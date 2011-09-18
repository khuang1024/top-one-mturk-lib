package edu.ucsc.cs.mturk.server.topone;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class TopOneServer {
    private static String algorithmType;
    private static int serverPort;
    private static int clientPort;
    private static String clientIp;
    private static String propertyFileName;

    /*
     * 1st parameter - the type of algorithm, "tree" or "bubble"
     * 2nd parameter - the port of the server side
     * 3rd parameter - the port of the client side
     * 4th parameter - the IP of the client side
     */
    public static void main(String[] args) throws Exception{
	
	/*
	 * validate the number of arguments
	 */
	if (args.length != 5) {
	    throw new TopOneServerException("The number of arguments" +
	    		" appended after TopOneServer.jar" +
	    		" must be exactly 5.");
	}
	
	/*
	 * Validate the 1st parameter which is supposed to be the type 
	 * of algorithm.
	 */
	if (!args[0].equals("tree")
		&& !args[0].equals("bubble")) {
	    throw new TopOneServerException("The first parameter must" +
	    		" be \"tree\" or \"bubble\".");
	} else {
	    algorithmType = args[0];
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
	
	// Evaluate the clientIp
	clientIp = args[3];
	
	// Evaluate the propertyFileName
	propertyFileName = args[4];
	
	/* Create the server.*/
	ServerSocket ss = new ServerSocket(serverPort);
	
	String info = "TopOneServer has been started.\n";
	info += "+--------------------------+---------------------------+\n";
	info += "| Algorithm                | " + algorithmType + "\n";
	info += "+--------------------------+---------------------------+\n";
	info += "| Server Listening Port    | " + serverPort + "\n";
	info += "+--------------------------+---------------------------+\n";
	info += "| Client Listening Port    | " + clientPort + "\n";
	info += "+--------------------------+---------------------------+\n";
	info += "| Client IP Address        | " + clientIp + "\n";
	info += "+--------------------------+---------------------------+\n";
	info += "| MTurk Property File      | " + propertyFileName + "\n";
	info += "+--------------------------+---------------------------+\n";
	info += "| Start Time               |" + new Date().toString() + "\n";
	info += "+--------------------------+---------------------------+\n\n";
	System.out.println(info);
	LogWriter.writeLog(info, "TopOneServer.txt");
	
	while(true){
	    System.out.println("TreeServer is listening ...");
	    Socket s = ss.accept();
	    new Thread(new TopOneServerThread(s, algorithmType, clientPort,
		    clientIp, propertyFileName)).start();
	}
    }
}