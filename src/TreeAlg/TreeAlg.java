/**
 * Tag starts from 0
 * Level starts from 0
 * use inputNum to indicate how many inputs a HIT has
 * use outputNum to indicate how many outputs a HIT has
 * use questions to indicate the content of questions a HIT wants to ask
 * the level of a HIT is the current level of the active HIT
 * the level of an Item is the current level where it is supposed to used for creating a HIT (but maybe it cannot be used for creating a HIT because of insufficient)
 * the tag of a HIT is the tag of the active HIT at its running level
 * the tag of an Item is the tag of the Item at its left-over level
 */

package TreeAlg;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import com.amazonaws.mturk.requester.HIT;
import com.amazonaws.mturk.requester.HITStatus;

public class TreeAlg {
	//basic data structure used in the algorithm
	private ArrayList<MyHit> activeHITQueue;
	private ArrayList<Item> newItemQueue;//the new items produced by HITs and going to be the inputs
	private ArrayList<ArrayList<Item>> levelQueue;//the level queue which stores the items
	private int[] tag;
	private boolean[][] trigger;
	
	//inputs of the task
	private ArrayList<Object> questions;
	
	private boolean checkNewItemQueueThreadDone=false;
	private boolean checkActiveHITQueueThreadDone=false;
	
	//answers of the task
	private Object finalAnswer;
	
	//exclusively for Carl****************************************
	private String imgFile;
	public void setImgFile(String imgFile){
		this.imgFile = imgFile;
	}
	public String getImgFile(){
		return this.imgFile;
	}
	private String jobID;
	public void setJobID(String jobID){
		this.jobID = jobID;
	}
	public String getJobID(){
		return this.jobID;
	}
	//******************************************************

	/**
	 *  
	 * @param questions this is the array list which stores the urls of images we want to post
	 */
	@SuppressWarnings("unchecked")
	public TreeAlg(ArrayList<Object> questions){		
		this.activeHITQueue = new ArrayList<MyHit>();
		this.newItemQueue = new ArrayList<Item>();
		this.levelQueue = new ArrayList<ArrayList<Item>>();
		
		this.questions = (ArrayList<Object>) questions.clone();
		
		this.tag = new int[this.questions.size()];
		for(@SuppressWarnings("unused") int i:this.tag){i = 0;}
		this.trigger = new boolean[this.questions.size()][this.questions.size()];
		for(int i = 0; i < trigger.length; i++){
			trigger[i][0]=true;//by default, the items with tag 0 at each level are all triggered
			for(int j = 1; j < trigger[i].length; j++){
				trigger[i][j]=false;
			}
		}
//		this.resetLog(Configuration.logFile);
		this.writeLog("-----------------------------new instance of tree algorithm----------------------------------", Configuration.logFile);
		
		this.finalAnswer = null;
	}
	
	/**
	 * Call this function to run the algorithm
	 */
	public void run(){
		String log = "";
		log += "Number of active HITs in this account: " + Configuration.service.getTotalNumHITsInAccount() + "\n";
		log += "Start time: " + (new Date()).toString() + "\n";
		this.writeLog(log, Configuration.logFile);
		System.out.println(log);
		
		//create the first group of HITs as the "leaves" of the tree
		this.initActiveHITQueue();
		
		//create two threads, one is checking the active HITs, and one is checking the queues
		Runnable cahqt = new CheckActiveHITQueueThread();
		Runnable cniqt = new CheckNewItemQueueThread();
		Thread checkActiveHITQueueThread = new Thread(cahqt);
		Thread checkNewItemQueueThread = new Thread(cniqt);
		checkActiveHITQueueThread.start();
		try{
			Thread.sleep(1000*2);
		}catch(InterruptedException e){}
		checkNewItemQueueThread.start();
	}
	
	/**
	 * 
	 * @return true if the whole algorithm is done
	 */
	public boolean isOver(){
		return this.checkActiveHITQueueThreadDone;
	}
	
	/**
	 * 
	 * @return the final answer (only one) that returned by the algorithm
	 */
	public Object getFinalAnswer(){
		return this.finalAnswer;
	}
	
	/**
	 * Create the first group of HITs as the "leaves" of the tree. The remaining questions which are not enough
	 * for creating a HIT will be put into the newItemQueue with level=0, tag=last_HIT's_tag+1
	 */
	private void initActiveHITQueue(){
		int i = 0;
		//bug
		for(i = 0; i + Configuration.inputNum <= this.questions.size(); i = i + Configuration.inputNum){
			
			//get the inputs for a HIT
			ArrayList<Object> inputs = new ArrayList<Object>();
			for(int j = 0; j < Configuration.inputNum; j++){
				inputs.add(this.questions.get(i+j));
			}
			
			//copy the inputs for log, since users may change inputs in the createHIT operation
			@SuppressWarnings("unchecked")
			ArrayList<Object> inputsForLog = (ArrayList<Object>) inputs.clone();
			
			//initialize all the info needed for creating a HIT, actually the HIT has been created
			String hitID;
			try {
				hitID = Configuration.createHIT(inputs, Configuration.assignmentNum, Configuration.outputNum, this.imgFile);
				int tag = this.tag[0]++;
				int level = 0;
				
				//create a MyHit and put it in the queue
				this.activeHITQueue.add(new MyHit(hitID, level, tag));
				
				//write to log
				this.writeCreateHITLog(hitID, level, tag, Configuration.assignmentNum, Configuration.outputNum, inputsForLog);
				
				//guarantee the inputs is empty, even though java has gc. 
				inputs.clear();
				inputsForLog.clear();
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Creating HIT failed. Check CGI.");
			}
		}
		
		//put the rest questions in the newItemQueue
		for(; i < this.questions.size(); i++){
			this.newItemQueue.add(new Item(this.questions.get(i), 0, this.tag[0]));
		}
		
//		//this is only for log
//		String log = "";
//		log += "\nnewItemQueue has the following items now (" + (new Date()).toString() + "): question, level, tag\n";
//		for(i = 0; i < this.newItemQueue.size(); i++){
//			Item item = this.newItemQueue.get(i); 
//			log += item.getQuestion().toString() + ", " + item.getLevel() + ", " + item.getTag() + "\n";
//		}
//		log += "\n";
//		this.writeLog(log, Configuration.logFile);
//		System.out.println(log);
	}
	
	
	private void checkActiveHITQueue(){
		
		System.out.println("checking activeHITQueue at "+ (new Date()).toString()+"...");
		
		//when the activeHITQueue is not empty
		if(!this.activeHITQueue.isEmpty()){
			
			//iterate each active HIT in the queue, and see if it is done
			for(int i = 0; i < this.activeHITQueue.size(); i++){
				
				MyHit myHit = this.activeHITQueue.get(i);
				String hitID = myHit.getHitID();
				HIT hit = Configuration.service.getHIT(hitID);
				
				//if the hit is done
				if(hit.getHITStatus() == HITStatus.Reviewable){
					
					//pause for a while, allow enough time to pass results NOTE
					try {
						Thread.sleep(1000*2);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					//get the answers
					ArrayList<Object> rawAnswers;
					try {
						rawAnswers = Configuration.getAnswers(hitID);
						ArrayList<Object> answers = this.filterAnswers(rawAnswers, Configuration.outputNum);
						int level = myHit.getLevel() + 1;
						int tag = myHit.getTag();
						
						//add new answers to the newItemQueue
						for(int j = 0; j < answers.size(); j++){
							this.newItemQueue.add(new Item(answers.get(j), level, tag));
						}
						
//						//debug
//						String debug = "";
//						for(int j = 0; j < answers.size(); j++){
//							debug += "Put the answer[" + answers.get(j) + "," + level + "," + tag + "] into the newItemQueue\n";
//						}
//						System.out.println(debug);
//						writeLog(debug, Configuration.logFile);
						
						//write to log
						this.writeGetAnswerLog(hitID, answers);
						
						//do the default operation on this reviewable HIT
						Configuration.operationAfterReviewable(hitID);
						
						//update the activeHITQueue and disable the HIT
						this.activeHITQueue.remove(i);
						i--;
					} catch (IOException e) {
						e.printStackTrace();
						System.err.println("Getting answers failed. Check CGI.");
					}
				}
			}
		}
	}
	
	private void checkNewItemQueue(){
		System.out.println("checking newItemQueue at "+ (new Date()).toString()+"...");
		
		while(!this.newItemQueue.isEmpty()){
			
			//get the current item and its info
			Item item = this.newItemQueue.get(0);
			int level = item.getLevel();
			int tag = item.getTag();
			
			//remove the current item in newItemQueue
			this.newItemQueue.remove(0);
			
			//when there is no queue for that level, create it
			while(this.levelQueue.size() < level +1){
				this.levelQueue.add(new ArrayList<Item>());
			}
			
			//put it into the right levelQueue
			this.levelQueue.get(level).add(item);
			
			//debug
			String debug = "Put the answer [" + item.getQuestion() +
					"," + item.getLevel() +
					"," + item.getTag() +"] to levelQueue (" + level + ")\n";
			System.out.println(debug);
			writeLog(debug, Configuration.logFile);
			
			//activate the items with next tag, so that when they arrive, we can create a HIT with them
			this.trigger[level][tag+1]=true;
		
			//when the newItemQueue is empty, update the levelQueue
			if(this.newItemQueue.isEmpty()){
				this.checkLevelQueue(level);
			}
			
			//when the level of the next item is different from this one, update the levelQueue too
			else if (this.newItemQueue.get(0).getLevel() != level){
				this.checkLevelQueue(level);
			}
		}
	
		//even though newItemQueue is empty, we still have to check the entire levelQueue, there may be some new items to be processed
		//for example, the top level may have items to create a HIT
		for(int i = 0; i < this.levelQueue.size(); i++){
			if(!this.levelQueue.get(i).isEmpty()){
				this.checkLevelQueue(i);
			}
		}
	}
	
	private void checkLevelQueue(int level) {
		System.out.println("checking levelQueue at "+ (new Date()).toString()+"...");
		
		//first, if the levelQueue at this level is empty, return
		if(this.levelQueue.get(level).isEmpty())
			return;
	
		//firts, we have to re-sort the queue
		this.sortLevelQueueByTag(this.levelQueue.get(level));
		
		//get the first item in the queue and see if it is triggered
		Item firstItem = this.levelQueue.get(level).get(0);
		
		//if it is triggered, we can create a HIT as long as we have enough items
		if(this.trigger[level][firstItem.getTag()]){
			while(!this.levelQueue.get(level).isEmpty()){
				boolean canCreateAHIT = false;
				
//				//if the this.levelQueue.get(level).size() >= Configuration.inputNum, we may be able to create a HIT
//				//check the "None of above"
//				this.checkNoneOfAbove(level);
			
				
				if(this.levelQueue.get(level).size() >= Configuration.inputNum){
					canCreateAHIT = true;
				}
				
				//check if the items we have are in sequence, and we have enough
				for(int i = 0; i < Configuration.inputNum && i < this.levelQueue.get(level).size(); i++){
					if(i != 0 && this.levelQueue.get(level).get(i).getTag() - this.levelQueue.get(level).get(i-1).getTag() > 1){
						canCreateAHIT = false;
						System.out.println("Order Wrong!");
						break;
					}
				}
				
				//if the items cannot meet the "order" requirement, we don't create a HIT
				if(!canCreateAHIT){
					break;
				}
				else{
					
					//get the items for the upcoming HIT, and update the levelQueue
					ArrayList<Object> inputs = new ArrayList<Object>();
					for(int i = 0; i < Configuration.inputNum; i++){
						inputs.add(this.levelQueue.get(level).get(0).getQuestion());
						this.levelQueue.get(level).remove(0);
					}
					
					
					
					//create the new HIT and put it into the activeHITQueue
					try {
						//modifymodifymodifymodify***********************************************************************************************
						if(this.areAllNoneOfAbove(inputs)){
							//move elements
//							//debug
//							String debug = "";
//							debug += "levelQueue size = "+levelQueue.size() + "\n";
//							debug += "level + 1 = " + (level+1);
//							levelQueue.get(level+1);
							newItemQueue.add(new Item(Configuration.noneOfAbove, level+1, this.tag[level]++));
							
//							while(levelQueue.size() < level+1){
//								levelQueue.add(new ArrayList<Item>());
//							}
//							levelQueue.get(level+1).add(new Item(Configuration.noneOfAbove, level+1, this.tag[level]++));
						}
						else{
							//copy the inputs for log, since users may change inputs in the createHIT operation
							@SuppressWarnings("unchecked")
							ArrayList<Object> inputsForLog = (ArrayList<Object>) inputs.clone();
							
							String hitID = Configuration.createHIT(inputs, Configuration.assignmentNum, Configuration.outputNum, this.imgFile);
							int tag = this.tag[level]++;
							
							//put the new HIT in the queue
							this.activeHITQueue.add(new MyHit(hitID, level, tag));
							
							//write to log
							this.writeCreateHITLog(hitID, level, tag, Configuration.assignmentNum, Configuration.outputNum, inputsForLog);
							
							//
							inputs.clear();
							inputsForLog.clear();
						}
						
					} catch (IOException e) {
						e.printStackTrace();
						System.err.println("Creating HIT failed. Check CGI.");
					}
					
				}
			}
		}
		else{
			System.out.println("Not Triggered.");
		}
	}
	
	
	private void checkNoneOfAbove(int level){
		for(int i = 0; i < this.levelQueue.get(level).size(); i++){
			if(this.levelQueue.get(level).get(i).getQuestion().equals(Configuration.noneOfAbove)){
				this.levelQueue.get(level).remove(i);
				i--;
			}
		}
	}
	
	private boolean areAllNoneOfAbove(ArrayList<Object> inputs){
		boolean areAllNoneOfAbove = true;
		for(int i = 0; i < inputs.size(); i++){
			if (inputs.get(i).equals(Configuration.noneOfAbove)){
				inputs.remove(i);
				i--;
			}
			else{
				areAllNoneOfAbove = false;
			}
		}
		return areAllNoneOfAbove;
	}
	
	
	//activeHITQueue is empty
	//newItemQueue is empty
	//other levelQueue except the topmost one have more than or equal f*g
	private boolean firstRoundDone(){
		if(!this.activeHITQueue.isEmpty() || !this.newItemQueue.isEmpty())
			return false;
		for(int i = 0; i < this.levelQueue.size(); i++){
			if(this.levelQueue.get(i).size() >= Configuration.inputNum){
				return false;
			}
		}
		return true;
	}
	
	
	
	
	private boolean secondRoundDone(){
		if(!this.firstRoundDone()){
			return false;
		}

		for(int i = 0; i < this.levelQueue.size() - 1; i++){
			if(!this.levelQueue.get(i).isEmpty()){
				return false;
			}
		}
		
		return true;
	}
	
	
	private void resetLog(String file){
		FileOutputStream f;
		try {
			f = new FileOutputStream(file);
			f.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void writeLog(String log, String file){
		BufferedWriter bw = null;
		try{
			bw = new BufferedWriter(new FileWriter(file, true));
			bw.write(log);
			bw.newLine();
			bw.flush();
		}catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	private void writeCreateHITLog(String hitID, int level, int tag, int assignmentNum, int answerNum, ArrayList<Object> inputs){
		//this is only for log
		HIT hit = Configuration.service.getHIT(hitID);
		String log = "";
		log += "\nCreate a new HIT\n";
		log += "HIT's profile:\n";
		log += "\tLevel: " + level + "\n";
		log += "\tTag: " + tag + "\n";
		log += "\tID: " + hitID + "\n";
		log += "\tGroup ID: " + hit.getHITTypeId() + "\n";
		log += "\tNumber of Assignments: " + assignmentNum + "\n";
		log += "\tNumber of Answers: " + answerNum + "\n";
		log += "\tQuestions:\n";
		for(int j = 0; j < inputs.size(); j++){log += "\t\t" + inputs.get(j).toString() + "\n";}
		log += "\tTime: " + (new Date()).toString() + "\n";
		log += "\n" + Configuration.service.getWebsiteURL() + "/mturk/preview?groupId=" + hit.getHITTypeId()+"\n\n";
//		log += "\tURL: " + Configuration.service.getWebsiteURL() + "/mturk/preview?groupId=" + hit.getHITTypeId()+"\n\n";
		this.writeLog(log, Configuration.logFile);
		System.out.println(log);
	}
	
	private void writeGetAnswerLog(String hitID, ArrayList<Object> answers){
		String log = "\nThe answers for HIT(" + hitID + "): " + (new Date()).toString() + "\n";
		for(int j = 0; j < answers.size(); j++){
			log += answers.get(j) + "\n";
		}
//		log += "\nnewItemQueue has the following items now (" + (new Date()).toString() + "): question, level, tag\n";
//		for(int i = 0; i < this.newItemQueue.size(); i++){
//			Item item = this.newItemQueue.get(i); 
//			log += item.getQuestion().toString() + ", " + item.getLevel() + ", " + item.getTag() + "\n";
//		}
		log += "\n";
		this.writeLog(log, Configuration.logFile);
		System.out.println(log);
	}
	
	
	private ArrayList<Item> sortLevelQueueByTag(ArrayList<Item> al){
		for(int i = 0; i < al.size(); i++){
			for(int j = i; j < al.size(); j++){
				if(al.get(i).getTag() > al.get(j).getTag()){
					Item temp = al.get(i);
					al.set(i, al.get(j));
					al.set(j, temp);
				}
			}
		}
		return al;
	}
	
	/**
	 * NOTE: rawAnswers.size() MUST be greater than or equal to outputNum
	 * 
	 * @param rawAnswers the rawAnswers which are needed to be filtered
	 * @param outputNum how many answers needed
	 * @return
	 */
	private ArrayList<Object> filterAnswers(ArrayList<Object> rawAnswers, int outputNum){
		ArrayList<Object> answers = new ArrayList<Object>();
		
		if(rawAnswers.size() == outputNum){
			return rawAnswers;
		}
		
		//prevent error
		if(rawAnswers.size() < outputNum){
			String errorLog = "\nError: rawAnswers.size() < outputNum\n\n";
			this.writeLog(errorLog, Configuration.logFile);
			System.out.println(errorLog);
			return rawAnswers;
		}
		
		//initialize the content and count array
		Object[] content = new Object[rawAnswers.size()];
		int[] count = new int[rawAnswers.size()];
		//NOTE: Here might be exception
		for(int i = 0; i < rawAnswers.size(); i++){
			int j = 0;
			while(content[j] != null && !content[j].equals(rawAnswers.get(i))){
				j++;
			}
			if(content[j] == null){
				content[j] = rawAnswers.get(i);
			}
			count[j]++;
		}
		
//		//count how many different answers
//		int answerNum = 0;
//		while(content[answerNum] != null && answerNum < content.length){
//			answerNum++;
//		}
//		
//		//prevent error
//		if(answerNum < outputNum){
//			String errorLog = "\nError: answerNum < outputNum\n\n";
//			this.writeLog(errorLog, Configuration.logFile);
//			System.out.println(errorLog);
//		}
		
		//sort content and count
		for(int i = 0; i < content.length; i++){
			for(int j = i; j < content.length; j++){
				if(count[i] < count[j]){
					Object tempContent = content[i];
					int tempCount = count[i];
					content[i] = content[j];
					count[i] = count[j];
					content[j] = tempContent;
					count[j] = tempCount;
				}
			}
		}
		
		//prevent error
		if(content[outputNum-1] == null){
			String errorLog = "\nError: content[outputNum-1] == null\n\n";
			this.writeLog(errorLog, Configuration.logFile);
			System.out.println(errorLog);
		}
		
		//when there is a tie. NOTE:
		if(/*content.length > outputNum &&*/ count[outputNum-1] == count[outputNum] && content[outputNum-1] != null){
			answers = this.solveTie(content, count, outputNum);
		}
		else{
			//when there is no tie
			for(int i = 0; i < outputNum; i++){
				answers.add(content[i]);
			}
		}
		
		return answers;
	}
	
	//
	private ArrayList<Object> solveTie(Object[] content, int[] count, int outputNum){
		ArrayList<Object> inputs = new ArrayList<Object>();
		ArrayList<Object> outputs = new ArrayList<Object>();
		
		//evaluate outputs with the answers which are not the reason for the tie
		int i = 0;
		while(i < count.length && count[i] != count[outputNum-1]){
			outputs.add(content[i]);
			i++;
		}
		
		//evaluate inputs with the tie answers
		while(i < count.length && count[i] == count[outputNum-1]){
			inputs.add(content[i]);
			i++;
		}
		
		//this is only for log
		String log = "A tie casued by the following answers:\n";
		for(int j = 0; j < inputs.size(); j++){
			log += inputs.get(j).toString() + "\n";
		}
		log += "\n";
		this.writeLog(log, Configuration.logFile);
		
		//copy the inputs for log, since users may change inputs in the createHIT operation
		@SuppressWarnings("unchecked")
		ArrayList<Object> inputsForLog = (ArrayList<Object>) inputs.clone();
		
		
		//create a new tie-solving HIT with the tie answers
		int outputNumForTie = outputNum-outputs.size();
		try {
			String hitID = Configuration.createHIT(inputs, Configuration.tieAssignmentNum, outputNumForTie, this.imgFile);
			//write to log
			this.writeCreateHITLog(hitID, -1, -1, Configuration.tieAssignmentNum, outputNumForTie, inputsForLog);
			
			//
			inputs.clear();
			inputsForLog.clear();
			
			//wait until the tie-solving HIT's answers come out
			HIT hit = Configuration.service.getHIT(hitID);
			while(hit.getHITStatus() != HITStatus.Reviewable){
				try {
					Thread.sleep(1000*3);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				hit = Configuration.service.getHIT(hitID);
			}
			
			
			ArrayList<Object> additionalRawAnswers = Configuration.getAnswers(hitID);
			ArrayList<Object> additionalAnswers = this.filterAnswers(additionalRawAnswers, outputNumForTie);
			
			//write to log
			this.writeGetAnswerLog(hitID, additionalAnswers);
			
			//do the default operation on this reviewable HIT
			Configuration.operationAfterReviewable(hitID);
			
			//prevent error
			if(additionalAnswers.size() != outputNumForTie){
				String errorLog = "\nError: additionalAnswers.size() != outputNumForTie\n\n";
				this.writeLog(errorLog, Configuration.logFile);
				System.out.println(errorLog);
			}
			
			//append the new answers to outputs
			for(int j = 0; j < additionalAnswers.size(); j++){
				outputs.add(additionalAnswers.get(j));
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Creating HIT or getting answers failed. Check CGI.");
		}
		
		return outputs;
	}
	
	
	//using inner class to implement multi-thread
	private class CheckActiveHITQueueThread implements Runnable{
		public void run() {

			while(!checkNewItemQueueThreadDone){
				checkActiveHITQueue();
				try {
					Thread.sleep(1000*5);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			System.out.println("Thread: CheckActiveHITQueueThread is done.\n");
			
			//this is only for log
			String log = "";
			log += "\nNumber of active HITs in this account: " + Configuration.service.getTotalNumHITsInAccount() + "\n";
			log += "End time: " + (new Date()).toString() + "\n";
			writeLog(log, Configuration.logFile);
			System.out.println(log);
			
			checkActiveHITQueueThreadDone = true;
		}
	}
	
	
	
	
	//using inner class to implement multi-thread
	private class CheckNewItemQueueThread implements Runnable{
		public void run() {
			while(!secondRoundDone()){
				while(!firstRoundDone()){
					checkNewItemQueue();
					try {
						Thread.sleep(1000*5);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				//deal with the remaining items at each level, except the top level
				for(int i = 0; i < levelQueue.size()-1; i++){
					
					//if this levelQueue is not empty, move the items to the upper level, and check that level
					if(!levelQueue.get(i).isEmpty()){
						
						//debug
						String debug = "levelQueue "+i+"'s size is "+levelQueue.get(i).size()+".\n It has:\n";
						for(int j = 0; j < levelQueue.get(i).size(); j++){
							debug += levelQueue.get(i).get(j).getQuestion() + "\n";
						}
						debug += "\n";
						writeLog(debug, Configuration.logFile);
						System.out.println(debug);
//						if(levelQueue.get(i).size() < Configuration.inputNum && i == levelQueue.size()-1)
						
						
						//may move the items at the top level to a upper level
//						while(levelQueue.size() <= i + 1){
//							levelQueue.add(new ArrayList<Item>());
//						}
						
						//if there is not enough items and its upper (i+1) level is not the top level (levelQueue.size() - 1), move items to the upper level
						String log = "\n";
						while(!levelQueue.get(i).isEmpty()){
							Item item = levelQueue.get(i).get(0);
							levelQueue.get(i).remove(0);							
							
							Object question = item.getQuestion();
							int level = item.getLevel() + 1;
							//debug**********
//							String debug2 = "";
//							debug2 += "Current level is: " + level + "\n";
//							debug2 += "Current level's size is: " + levelQueue.get(level).size() + "\n";
//							debug2 += "They are:\n";
//							System.out.println(debug2);
//							writeLog(debug2, Configuration.logFile);
//							for(int j = 0; j < levelQueue.get(level).size(); j++){
////								debug2 = "\t" + levelQueue.get(level).get(j).getQuestion() + ",";
////								System.out.print(debug2);
////								writeLog(debug2, Configuration.logFile);
////								debug2 = levelQueue.get(level).get(j).getLevel()+",";
////								System.out.print(debug2);
////								writeLog(debug2, Configuration.logFile);
////								debug2 = levelQueue.get(level).get(j).getTag()+"\n";
////								System.out.print(debug2);
////								writeLog(debug2, Configuration.logFile);
//								debug2 = "[" + levelQueue.get(level).get(j).getQuestion() +
//										"," + levelQueue.get(level).get(j).getLevel() +
//										"," + levelQueue.get(level).get(j).getTag() + "]\n";
//							}
//							System.out.println(debug2);
//							writeLog(debug2, Configuration.logFile);
							//****************
							
							//big bug!!
							int tag = 0;
							if(levelQueue.get(level).size() == 0){
								tag = -2;//if this level has no item, set the tag 0. Don't worry, when it goes up to the upper level, it will be evaluated again!
							}
							else{
								tag = levelQueue.get(level).get(levelQueue.get(level).size()-1).getTag();//get the tag of the last element at the upper level
							}
							
							levelQueue.get(level).add(new Item(question, level, tag));
							
							log += "Move (" + item.getQuestion() + ", " + item.getLevel() + ", " + item.getTag() + ") to level " + level + "\n";
							log += "New profile of this item:" + question + ", " + level + ", " + tag + "\n";
						}
						log += "\n";
						writeLog(log, Configuration.logFile);
						System.out.println(log);
						
						////NOTE: maybe we can just wait for this HIT
						//if we use break, another case may happen:
						//when the new HIT is done, the new results move to the upper level, and the 
						//upper level's remaining items plus the new reuslts are enough, they will create another HIT
						//but this might not be a problem
						break;//must have this break, since we have to wait until the new HIT (if there is any) is done
					}
				}
				
			}
			
			
//			checkNewItemQueue();//MUST make sure the newly-moved items have been moved into the levelQueue! Otherwise, the levelQueue is empty! 
			
			
			//add an extra HIT to get only one answer//
			if(levelQueue.get(levelQueue.size()-1).size() > 1){
				int num = levelQueue.get(levelQueue.size()-1).size();//get all elements at the top level
				ArrayList<Object> inputs = new ArrayList<Object>();
				for(int i = 0; i < num; i++){
					inputs.add(levelQueue.get(levelQueue.size()-1).get(0).getQuestion());
					levelQueue.get(levelQueue.size()-1).remove(0);
				}
				
				//copy the inputs for log, since users may change inputs in the createHIT operation
				@SuppressWarnings("unchecked")
				ArrayList<Object> inputsForLog = (ArrayList<Object>) inputs.clone();
				
				//DEBUG
				for(int j = 0; j < levelQueue.size(); j++){
					System.out.println("Level "+ j + "  has:\n");
					for (int k = 0; k < levelQueue.get(j).size(); k++){
						System.out.println(levelQueue.get(j).get(k).getQuestion());
					}
				}
				
				//modifymodifymodifymodifymodifymodify**********************************************************8
				if(areAllNoneOfAbove(inputs)){
					finalAnswer = Configuration.noneOfAbove;
					System.out.println("Thread: CheckNewItemQueueThread is done.\n");
					checkNewItemQueueThreadDone = true;
				}
				else{
					String hitID;
					try {
						hitID = Configuration.createHIT(inputs, Configuration.assignmentNum, 1, imgFile);
						//write to log
						writeCreateHITLog(hitID, levelQueue.size()-1, tag[levelQueue.size()-1], Configuration.assignmentNum, 1, inputsForLog);
						
						//
						inputs.clear();
						inputsForLog.clear();
						
						//wait until the hit is done
						HIT hit = Configuration.service.getHIT(hitID);
						while(hit.getHITStatus() != HITStatus.Reviewable){
							try {
								Thread.sleep(1000*10);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							hit = Configuration.service.getHIT(hitID);
						}
						
						//allow enough time to get the answers
						try {
							Thread.sleep(1000*2);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
						ArrayList<Object> rawAnswers = Configuration.getAnswers(hitID);
						ArrayList<Object> answers = filterAnswers(rawAnswers, 1);
						
						//
						finalAnswer = answers.get(0);
						
						//write to log
						writeGetAnswerLog(hitID, answers);
						
						//do the default operation on this reviewable HIT
						Configuration.operationAfterReviewable(hitID);
					} catch (IOException e1) {
						e1.printStackTrace();
						System.err.println("Creating HIT or getting answers failed. Check CGI.");
					}
				}
				
				System.out.println("Thread: CheckNewItemQueueThread is done.\n");
				checkNewItemQueueThreadDone = true;
			}
			else{
				//bug
				finalAnswer = levelQueue.get(levelQueue.size()-1).get(0).getQuestion();

				System.out.println("Thread: CheckNewItemQueueThread is done.\n");
				checkNewItemQueueThreadDone = true;
			}
		}
	}
	
	public static void main(String[] args){
		ArrayList<Object> desc = new ArrayList<Object>();
		desc.add("1.This is a boat.");
		desc.add("2.This is a cat.");
		desc.add("3.This is a dog.");
		desc.add("4.This is a flower.");
		desc.add("5.This is a car.");
		desc.add("6.This is a man.");
		desc.add("7.This is a desk.");
		desc.add("8.This is a student.");
		desc.add("9.This is a teacher.");
		desc.add("10.This is a boy.");
		desc.add("11.This is a girl.");
		desc.add("12.This is a building.");
		desc.add("13.This is a banana.");
		
		TreeAlg alg = new TreeAlg(desc);
		alg.run();
	}
}
