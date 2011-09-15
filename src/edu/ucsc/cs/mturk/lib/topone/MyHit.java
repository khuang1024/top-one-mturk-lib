/*
 * This Interface is used for callback function definition.
 */
package edu.ucsc.cs.mturk.lib.topone;

import java.util.ArrayList;

import com.amazonaws.mturk.service.axis.RequesterService;

/**
 * <p>This interface is provided for callback implementation. 
 * Library users must implement this interface so as to 
 * make use of the library.</p>
 * 
 * <p>During the procedure of processing questions, the algorithm needs 
 * to create HITs, retrieve answers of HITs and deal with the HITs which 
 * have returned its answers. However, these operations should be 
 * customized by library users and provided to the algorithm.</p> 
 * 
 * <p>Therefore, library users need to implement all the three 
 * abstract functions and pass an instance of the implementation 
 * class into the algorithm's constructor, so that the algorithm is 
 * able to call corresponding functions to create HITs, get answers 
 * and operate past HITs.</p> 
 * 
 * @author Kerui Huang
 *
 */
public interface MyHit {
    
    /**
     * Create a HIT
     * 
     * @param service the service object of the library user.
     * @param inputs the inputs/questions for the new HIT.
     * @param numberOfOutputs the number of required answers of the new HIT. 
     * @param numberOfAssignments the number of assignments of the new HIT.
     * @return the new HIT's ID assigned by Amazon Mechanical Turk.
     */
    public String createMyHit(RequesterService service, 
	    ArrayList<Object> inputs, int numberOfOutputs, 
	    int numberOfAssignments);
    /*
     * The createMyHit function above will not cause synchronized problem. The reason 
     * is that there is only one thread (the checkNewItemQueueThread) 
     * out there calling this function to create a HIT. 
     */
    
    
    /**
     * Get the answers of the HIT.
     * 
     * @param service the service object of the library user.
     * @param hitId the ID of the HIT
     * @return An ArrayList of the answers of the HIT.
     */
    public ArrayList<Object> getMyHitAnswers(RequesterService service,
	    String hitId);
    
    /**
     * The default operation on the HIT from which we have already got 
     * the answers.
     * 
     * @param service the service object of the library user.
     * @param hitId the ID of the HIT
     */
    public void dumpPastHit(RequesterService service, String hitId);
}
