/**
 * This is the configuration file where we set some parameters for our experiment.
 */

package TreeAlg;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.amazonaws.mturk.dataschema.QuestionFormAnswers;
import com.amazonaws.mturk.dataschema.QuestionFormAnswersType;
import com.amazonaws.mturk.requester.Assignment;
import com.amazonaws.mturk.requester.HIT;
import com.amazonaws.mturk.service.axis.RequesterService;
import com.amazonaws.mturk.util.PropertiesClientConfig;

public class Configuration {
	
	protected static RequesterService service = new RequesterService(new PropertiesClientConfig(System.getProperty("user.dir") + java.io.File.separator + "mturk.properties"));
	
	protected static int inputNum = 3;//the actual number of questions is inputNum + 1, the additional one is "NONE OF ABOVE" 
	protected static int outputNum = 1;//the number of required answers
	
	protected static int assignmentNum = 2;//the number of required assignments
	protected static int tieAssignmentNum = 1;//the number of required assignments for tie-solving HIT
	
	protected static String logFile = 
			System.getProperty("user.dir") + java.io.File.separator + "log.txt";//where the log file stored
	
	protected static String noneOfAbove = "-1";
	
	
	/**
	 * 
	 * @param questions the list of questions
	 * @param assignmentNum how many assignments for this HIT (tie-solving HITs and normal HITs may have different number of assignments)
	 * @param outputNum how many outputs this HIT has.
	 * @return
	 */
	protected static String createHIT(ArrayList<Object> questions, int assignmentNum, int outputNum, String imgFile)throws IOException{
		//write your own method for creating a HIT here, and return the HIT's ID, here is an example
		
		//socket
		Socket s = new Socket("stardance.cse.ucsc.edu", 11025);
		PrintStream ps = new PrintStream(s.getOutputStream());
		
		
		
		String question = "";
		int i = 0;
		for(i = 0; i < questions.size(); i++){
			question += "&q" + i + "=" + questions.get(i).toString();
		}
		question += "&q" + i + "=" + noneOfAbove;
		String request = "qtype=createHIT&" + 
						"assignmentNum=" + assignmentNum + "&" +
						"outputNum=" + outputNum + "&" +
						"imgfile=" + imgFile + "&" + 
						"qnumber=" + (questions.size()+1) +
						question;
		ps.println(request);
		ps.flush();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
		String hitID = "";
		
		hitID = br.readLine();
		System.out.println("get the hit id: "+hitID);
		s.close();
		
		//write log
		String info = "Operation: Create a HIT\n";
		info += "\tThe string read from socket: " + hitID + "\n"; 
		info += "\tCreate HIT " + hitID + " at " + (new Date()).toString() + "\n";
		info += "\t" + question + "\n";
		writeLog(info, Configuration.logFile);
		System.out.println(info);
		
		
		return hitID;
//		while((hitID = br.readLine()) != null){}//waiting the server close the socket	
//		return hitID;
	}
	
	/**
	 * Initially, ArrayList is for the case when there are more than one answers.  But it seems you only have one
	 * answers, so just put this only one element in the ArrayList.
	 * 
	 * @param hitID
	 * @return the list of answers
	 */
	protected static ArrayList<Object> getAnswers(String hitid) throws IOException {
		//socket
		Socket s = new Socket("stardance.cse.ucsc.edu", 11025);
		PrintStream ps = new PrintStream(s.getOutputStream());
		String request = "qtype=getAnswer&" + 
						"hitID=" + hitid;
		ps.println(request);
		ps.flush();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
		String rawAnswers = "";
		rawAnswers = br.readLine();
		System.out.println("get rawAnswers: " + rawAnswers);
		s.close();
		
		
		HashMap hm = new HashMap();
		parseToMap(hm, rawAnswers); 
		
		ArrayList<Object> answers = new ArrayList<Object>();
		for(int i = 0; i < Integer.parseInt(hm.get("anumber").toString()); i++){
			answers.add(hm.get("a"+i));
		}
		
		//write log
		String info = "Operation: Get answer\n";
		info += "\t" + "The string read from socket is: " + rawAnswers + "\n";
		info += "\t" + "The answers of HIT " + hitid + " are: ";
		for(int i= 0; i < answers.size(); i++){
			info += answers.get(i) + ", ";
		}
		info += "\n";
		writeLog(info, Configuration.logFile);
		System.out.println(info);
		
		return answers;
	}
	
	
	private static void parseToMap(HashMap hm, String str){
		String[] key_value = str.split("&");
		for(String kv: key_value){
			String[] s = kv.split("=");
			hm.put(s[0], s[1]);
		}
	}
	
	
//	protected static String CallCGI(String url,String encoded) throws IOException{
//		URL CGIurl = new URL(url);
//		
//		URLConnection c = CGIurl.openConnection();
//		c.setDoOutput(true);
//		c.setUseCaches(false);
//		c.setRequestProperty("content-type","application/x-www-form-urlencoded");
//		DataOutputStream out = new DataOutputStream(c.getOutputStream());
//		out.writeBytes(encoded);//write these parameters into the standard IO
//		out.flush();
//		out.close();
//		
//		BufferedReader in = new BufferedReader(new InputStreamReader(c.getInputStream()));
//		
//		String ret="";
//		String aLine;
//		while ((aLine = in.readLine()) != null) {
//	   // 	data from the CGI
//	   //	System.out.println(aLine);
//			ret += aLine;
//		}
//		
//		return ret;
//	}
	
	
	protected static void operationAfterReviewable(String hitID){
		service.disableHIT(hitID);
	}
	
	@SuppressWarnings("unused")
	private static void writeLog(String log, String file){
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
}