package edu.ucsc.cs.mturk.server.topone;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

class StringParser {
    
    // Suppress default constructor for noninstantiability.
    private StringParser() {
	throw new AssertionError();
    }
    
    /* Parse the request string.*/
    static HashMap<String, String> parseToMap(String str) {
	HashMap<String, String> hm = new HashMap<String, String>();
	String[] key_value = str.split("&");
	for(String kv: key_value){
	    String[] s = kv.split("=");
	    if (s.length > 2) {
		throw new TopOneServerException("Illegal request string." +
				"Please check the format of request string " +
				"you sent. It must be in the CGI style.");
	    }
	    if (s.length != 1) {
		hm.put(s[0], s[1]);
	    }
	}
	return hm;
    }
    
    /* Extract the questions.*/
    static ArrayList<Object> extractQuestions(
	    HashMap<String, String> hm) {
	return extractString(hm, "q");
    }
    
    /* Extract the questions.*/
    static ArrayList<Object> extractAnswers(
	    HashMap<String, String> hm) {
	return extractString(hm, "a");
    }
    
    /* Extract the number of inputs of a HIT.*/
    static int extractNumberOfInputs(HashMap<String, String> hm) {
	return extractNumber(hm, "nInput");
    }
    
    /* Extract the number of outputs of a HIT.*/
    static int extractNumberOfOutputs(HashMap<String, String> hm) {
	return extractNumber(hm, "nOutput");
    }
    
    /* Extract the number of assignments of a normal HIT.*/
    static int extractNumberOfAssignments(HashMap<String, String> hm) {
	return extractNumber(hm, "nAssignment");
    }
    
    /* Extract the number of assignments of a tie-solving HIT.*/
    static int extractNumberOfTieAssignments(HashMap<String, String> hm) {
	return extractNumber(hm, "nTieAssignment");
    }
    
    /* Extract isShuffled. If it is omitted, return true.*/
    static boolean extractIsShuffled(HashMap<String, String> hm) {
	return extractBoolean(hm, "isShffuled");
    }
    
    /* Extract isLogged. If it is omitted, return true.*/
    static boolean extractisLogged(HashMap<String, String> hm) {
	return extractBoolean(hm, "isLogged");
    }
    
    /* Extract the jobId.*/
    static String extractJobId(HashMap<String, String> hm) {
	String jobId;
	if (hm.get("jobId") == null) {
	    jobId = Integer.toString(new Date().hashCode());
	} else {
	    jobId = hm.get("jobId");
	}
	return jobId;
    }
    
    /* Extract an integer by given key.*/
    static private int extractNumber(HashMap<String, String> hm, 
	    String numberName) {
	int number = 0;
	
	// Check if the HashMap contains "numberName".
	if (hm.get(numberName) == null) {
	    throw new TopOneServerException("You must indicate " + 
		    numberName + ".");
	}

	// Check if numberName is a number.
	try {
	    number = Integer.parseInt(hm.get(numberName));
	} catch (NumberFormatException e) {
	    throw new TopOneServerException(numberName + 
		    " must be an integer.");
	}
	return number;
    }
    static private ArrayList<Object> extractString(HashMap<String, String> hm, String qa) {
	ArrayList<Object> extractedString = new ArrayList<Object>();
	
	if ((!qa.equals("q")) && (!qa.equals("a"))) {
	    throw new TopOneServerException ("Question or Answer not found!" +
	    		" The parameters related to question must have prefix q." +
	    		" The parameters related to answer must have prefix a.");
	}
	
	// Extract qnum/anum.
	int num = extractNumber(hm, qa+"num");
	
	// Check if qnum/anum is greater than 0.
	if (num <= 0) {
	    throw new TopOneServerException(qa + "num must be greater than 0.");
	}
	
	
	// Parse the questions/answers.
	for (int i = 0; i < num; i++) {
	    if (hm.get(qa + i) == null) {
		throw new TopOneServerException(qa + i + " is not found.");
	    } else {
		extractedString.add(hm.get(qa + i));
	    }
	}
	return extractedString;
    }
    
    /* Extract a boolean.*/
    static private boolean extractBoolean(HashMap<String, String> hm, 
	    String booleanName) {
	if (hm.get(booleanName) == null) {
	    return true;
	} else if (hm.get(booleanName).equals("true")) {
	    return true;
	} else if (hm.get(booleanName).equals("false")) {
	    return false;
	} else {
	    throw new TopOneServerException(booleanName + " must be true or " +
	    		"false or omitted.");
	}
    }
}
