package edu.ucsc.cs.mturk.test;

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
    static void writeLog(String logContent, String logName) {
	BufferedWriter bw = null;
	try{
	    bw = new BufferedWriter(new FileWriter(logName, true));
	    bw.write(logContent);
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
    
    static void writeCreateHitLog(RequesterService service, 
	    String hitId, int level, int tag, int numberOfAssignments, 
	    int numberOfOutputs, ArrayList<Object> inputs, String logName) {
	
	HIT hit = service.getHIT(hitId);
	
	String logContent = "";
	logContent += "A new HIT is created.\n";
	logContent += " Level:\t" + level + "\n";
	logContent += " Tag:\t" + tag + "\n";
	logContent += " ID:\t" + hitId + "\n";
	logContent += " Group ID:\t" + hit.getHITTypeId() + "\n";
	logContent += " Number of questions(inputs):\t" + inputs.size();
	logContent += " Number of requested assignments:\t" + numberOfAssignments + "\n";
	logContent += " Number of requested answers:\t" + numberOfOutputs + "\n";
	logContent += " Questions:\n";
	for (int j = 0; j < inputs.size(); j++) {
	    logContent += "  " + inputs.get(j).toString() + "\n";
	}
	logContent += " Time:\t" + (new Date()).toString() + "\n";
	logContent += " URL:\t" + service.getWebsiteURL() + "/mturk/preview?groupId=" + hit.getHITTypeId()+"\n\n";
	writeLog(logContent, logName);
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
