package org.shanbo.spseg;

import java.util.ArrayList;
import java.util.List;

public class StringNode{

	public static final int LETTER = 0;
	public static final int HANZI = 1;
	public static final int NUMBER = 2;
	public static final int DELIMITER = 3;
	public static final int SPACELIKE = 4;
	public static final int OTHER = 5;
	
	public char ch;

	
	public int start; //a pharse node shares the same start and end
	public int end;
	public boolean isPhrase = false;
	public int charType = 1; //for segtoken, letter = 0, hanzi = 1, digit = 2, delimiter = 3, spacelike = 4, other = 5
//	public StringNode(String original, int i){
//		ch = original.charAt(i);
//	}

	public StringNode(char c, int start, int end){
		ch = c;
		this.start = start;
		this.end = end;
		if (start +1 < end)
			isPhrase = true;
	}
	
	public StringNode(char c, int start, int end, int chartype){
		ch = c;
		this.start = start;
		this.end = end;
		if (start +1 < end)
			isPhrase = true;
		this.setCharType(chartype);
	}
	
	public void setCharType(int chartype){
		this.charType = chartype;
	}
	
	
	
	public static List<StringNode> cutSentence(String original, int start, int end, int chartype){
		List<StringNode> out = new ArrayList<StringNode>(end- start);
		for(int i = start ; i < end; i++){
			char c =original.charAt(i);
			if (c >= 0xFF10)
				c -= 0xFEE0;
			if (c >= 0x0041 && c<= 0x005A)
				c += 0x0020;
			StringNode sn = new StringNode(c, start, end);
			sn.setCharType(chartype);
			out.add(sn);
		}
		return out;
	}
	
	@Deprecated
	public static List<StringNode> cutSentence(char[] newSentence, int start, int end, int chartype){
		List<StringNode> out = new ArrayList<StringNode>(end- start);
		for(int i = start ; i < end; i++){
			StringNode sn = new StringNode(newSentence[i], start, end);
			sn.setCharType(chartype);
			out.add(sn);
		}
		return out;
	}
	
	public String toString(){
		return String.valueOf(ch) + ":"+charType+"["+start+","+end+"]";
	}
	
}