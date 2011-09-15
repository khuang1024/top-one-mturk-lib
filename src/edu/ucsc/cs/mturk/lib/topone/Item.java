package edu.ucsc.cs.mturk.lib.topone;

class Item {
    private Object question;
    private int level;
    private int tag;
    
    Item(Object question, int level, int tag) {
	this.question = question;
	this.level = level;
	this.tag = tag;
    }
    
    Object getQuestion() {
	return question;
    }
    
    int getLevel() {
	return level;
    }
    
    int getTag() {
	return tag;
    }
    
    void setTag(int newTag) {
	tag = newTag;
    }
}
