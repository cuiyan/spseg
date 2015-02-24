package org.shanbo.spseg;

public class Term {
	
	static final int TYPE_CN = 1;
	static final int TYPE_NUM = 2;
	static final int TYPE_EN = 4;
	static final int TYPE_PHRASE= 8;
	static final int TYPE_UNKNOWN= 9;
	
	int start;
	int end;
	int type = TYPE_UNKNOWN;
	final char[] content;
	 
	
	public static final int ASCII = 0;
	public static final int CN = 1;
	public static final int OTHER = 5;
	
	Term(char[] original , int start, int end){
		this.move(start, end);
		this.content = original;
	}
	
	Term(char[] original , int start, int end, int type){
		this(original, start, end);
		this.type = type;
	}
	
	
	void move(int start, int end){
		this.start = start;
		this.end = end;
	}
	
	
	public String getTerm(){
		return new String(content, start, end-start);
	}
	
	public int getOffsetBegin(){
		return start;
	}
	
	public int getOffsetEnd(){
		return end;
	}
	
	public String getType(){
		switch(this.type){
			case TYPE_CN:
				return "CN";
			case TYPE_EN:
				return "EN";
			case TYPE_NUM:
				return "NUM";
			case TYPE_PHRASE:
				return "PHRASE";
			default:
				return "UNKNOWN";
		}
			
	}
	
	
	public String toString(){
		StringBuilder builder = new StringBuilder();
		for(int i = start; i < end; i++){
			builder.append(content[i] );
		}
		builder.append("\t").append(start).append("-").append(end);
		return builder.toString();
	}
}
