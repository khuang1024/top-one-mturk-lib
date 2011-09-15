package edu.ucsc.cs.mturk.lib.topone;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import com.amazonaws.mturk.requester.HIT;
import com.amazonaws.mturk.service.axis.RequesterService;

class LogWriter {
    
    /**
     * Create or reset the log file with provided file name.
     * 
     * @param fileName The FULL name of the log file, including its path.
     */
    static void createOrResetLog(String logName) {
	FileOutputStream fos = null;
	try {
		fos = new FileOutputStream(logName);
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} finally {
	    try {
		fos.close();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }
    
    /**
     * Write the log into the log file.
     * 
     * @param log The content to be recorded.
     * @param fileName The FULL name of the log file, including its path.
     */
    static void writeLog(String log, String logName) {
	BufferedWriter bw = null;
	try{
	    bw = new BufferedWriter(new FileWriter(logName, true));
	    bw.write(log);
	    bw.newLine();
	    bw.flush();
	} catch (IOException e) {
	    e.printStackTrace();
	} finally {
	    try {
		bw.close();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }
    
    static void writeTreeCreateHitLog(RequesterService service, 
	    String hitId, int level, int tag, int numberOfAssignments, 
	    int numberOfOutputs, ArrayList<Object> inputs, String logName) {
	
	HIT hit = service.getHIT(hitId);
	
	String log = "";
	log += "A new HIT is created.\n";
	log += " Level:\t" + level + "\n";
	log += " Tag:\t" + tag + "\n";
	log += " ID:\t" + hitId + "\n";
	log += " Group ID:\t" + hit.getHITTypeId() + "\n";
	log += " Number of questions(inputs):\t" + inputs.size() + "\n";
	log += " Number of requested assignments:\t" + numberOfAssignments + "\n";
	log += " Number of requested answers:\t" + numberOfOutputs + "\n";
	log += " Questions:\n";
	for (int j = 0; j < inputs.size(); j++) {
	    log += "  " + inputs.get(j).toString() + "\n";
	}
	log += " Time:\t" + (new Date()).toString() + "\n";
	log += " URL:\t" + service.getWebsiteURL() + "/mturk/preview?groupId=" + hit.getHITTypeId()+"\n\n";
	writeLog(log, logName);
	System.out.println(log);
    }
    
    static void writeBubbleCreateHitLog(RequesterService service, 
	    String hitId, int numberOfAssignments, 
	    int numberOfOutputs, ArrayList<Object> inputs, String logName) {
	
	HIT hit = service.getHIT(hitId);
	
	String log = "";
	log += "A new HIT is created.\n";
	log += " ID:\t" + hitId + "\n";
	log += " Group ID:\t" + hit.getHITTypeId() + "\n";
	log += " Number of questions(inputs):\t" + inputs.size() + "\n";
	log += " Number of requested assignments:\t" + numberOfAssignments + "\n";
	log += " Number of requested answers:\t" + numberOfOutputs + "\n";
	log += " Questions:\n";
	for (int j = 0; j < inputs.size(); j++) {
	    log += "  " + inputs.get(j).toString() + "\n";
	}
	log += " Time:\t" + (new Date()).toString() + "\n";
	log += " URL:\t" + service.getWebsiteURL() + "/mturk/preview?groupId=" + hit.getHITTypeId()+"\n\n";
	writeLog(log, logName);
	System.out.println(log);
    }
    
    static void writeGetAnswerLog(String hitId, ArrayList<Object> answers, String logName) {
	String log = "A result is received.\n";
	log += " HIT: " + hitId + "\n";
	log += " The answers are:\n";
	for (int j = 0; j < answers.size(); j++) {
	    log += "  "+answers.get(j) + "\n";
	}
	log += "\n";
	writeLog(log, logName);
	System.out.println(log);
    }
}
