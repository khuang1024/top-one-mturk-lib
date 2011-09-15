package edu.ucsc.cs.mturk.test;

import java.util.ArrayList;
import java.util.Date;

import com.amazonaws.mturk.dataschema.QuestionFormAnswers;
import com.amazonaws.mturk.dataschema.QuestionFormAnswersType;
import com.amazonaws.mturk.requester.Assignment;
import com.amazonaws.mturk.requester.HIT;
import com.amazonaws.mturk.service.axis.RequesterService;

import edu.ucsc.cs.mturk.lib.topone.MyHit;

/*
 * This is the class implements MyHit interface. One must create 
 * such kind of a class and pass an object of that class into the 
 * TreeAlgorithm's constructor as the parameter "MyHit". 
 */
public class TestHit implements MyHit {
    public String createMyHit(RequesterService service, 
	    ArrayList<Object> inputs, int numberOfOutputs, 
	    int numberOfAssignments) {
	
	/* Some parameters used for initializing MTurk HIT.*/
	String title = "Select best description";
	String description = "Please selec the best description.";
	String keywords = "selection, description";
	Question question = new Question(inputs, numberOfOutputs);
	double reward = 0.01;
	long assignmentDurationInSeconds = 60 * 30; // 30 minutes
	long autoApprovalDelayInSeconds = 60 ; // 1 minute
	long lifetimeInSeconds = 60 * 60 * 24 * 7; // 1 week
	HIT hit = service.createHIT(null, title, description, keywords, 
		question.getQuestion(), reward, assignmentDurationInSeconds, 
		autoApprovalDelayInSeconds, lifetimeInSeconds, 
		numberOfAssignments, null, null, null);
	return hit.getHITId();
    }
    
    public ArrayList<Object> getMyHitAnswers(RequesterService service,
	    String hitId) {
	Assignment[] assignments = service.getAllAssignmentsForHIT(hitId);
	ArrayList<Object> rawAnswers = new ArrayList<Object>();
	
	for (Assignment assignment : assignments) {
		String log = assignment.getWorkerId()+" had the following" +
				" answers for HIT("+assignment.getHITId()+
				"):  "+(new Date()).toString()+"\n";
		
		// Interpret the XML and parse answers out.
		String answerXML = assignment.getAnswer();
		QuestionFormAnswers qfa = 
			RequesterService.parseAnswers(answerXML);
		@SuppressWarnings("unchecked")
		ArrayList<QuestionFormAnswersType.AnswerType> answers = 
			(ArrayList<QuestionFormAnswersType.AnswerType>) qfa.
				getAnswer();
		for (QuestionFormAnswersType.AnswerType answer : answers) {
			String assignmentId = assignment.getAssignmentId();
			String answerValues = RequesterService.
				getAnswerValue(assignmentId, answer);
			String[] rawAnswerValues = null;
			if (answerValues != null) {
				 rawAnswerValues = answerValues.split("\\|");
			}
			for (String ans: rawAnswerValues) {
				if(ans.startsWith("desc_identifier:")){
					rawAnswers.add(ans.substring(16));
					log += ans.substring(16)+"\n";
				}
			}
		}
		log += "---------\n\n";
		LogWriter.writeLog(log, "detail.txt");
	}
	return rawAnswers;
    }
    
    public void dumpPastHit(RequesterService service, String hitId) {
	service.disableHIT(hitId);
    }
}
