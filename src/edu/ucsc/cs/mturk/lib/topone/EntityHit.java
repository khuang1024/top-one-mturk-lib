package edu.ucsc.cs.mturk.lib.topone;

class EntityHit {
	private String hitId;
	private int level;
	private int tag;
	
	EntityHit(String hitId, int level, int tag){
		this.hitId = hitId;
		this.level = level;
		this.tag = tag;
	}
	
	protected String getHitId(){
		return this.hitId;
	}
	
	protected int getLevel(){
		return this.level;
	}
	
	protected int getTag(){
		return this.tag;
	}
}
