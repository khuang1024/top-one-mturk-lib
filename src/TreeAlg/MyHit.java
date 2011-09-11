package TreeAlg;

public class MyHit {
	private String hitID;
	private int level;
	private int tag;
	/**
	 * A special HIT exclusively for the TREE algorithm 
	 * @param hitID
	 * @param level
	 * @param tag
	 */
	MyHit(String hitID, int level, int tag){
		this.hitID = hitID;
		this.level = level;
		this.tag = tag;
	}
	
	protected String getHitID(){
		return this.hitID;
	}
	
	protected int getLevel(){
		return this.level;
	}
	
	protected int getTag(){
		return this.tag;
	}
}
