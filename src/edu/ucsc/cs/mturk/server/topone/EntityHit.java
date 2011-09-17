package edu.ucsc.cs.mturk.server.topone;

class EntityHit {
    private String hitId;
    private int level;
    private int tag;
    
    EntityHit(String hitId, int level, int tag) {
	this.hitId = hitId;
	this.level = level;
	this.tag = tag;
    }
    
    String getHitId() {
	return hitId;
    }
    
    int getLevel() {
	return level;
    }
    
    int getTag() {
	return tag;
    }
}
