package edu.ucsc.cs.mturk.server.topone;

@SuppressWarnings("serial")
public class TopOneServerException extends RuntimeException{
    public TopOneServerException() {}
    
    public TopOneServerException(String e) {
	super(e);
    }
}
