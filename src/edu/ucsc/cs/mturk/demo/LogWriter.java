package edu.ucsc.cs.mturk.demo;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

class LogWriter {
    static synchronized void writeLog(String logContent, String logName) {
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
}
