package org.shanbo.spseg;


import java.util.ArrayList;
import java.util.List;




import love.cq.domain.Forest;
import love.cq.library.Library;
import love.cq.splitWord.GetWord;

public class SpSegmenter {

	final static String DICT_STRING = "main2012.dic";

	static Forest forest;

	static {
		try {
			forest  = Library.makeForest(SpSegmenter.class.getResourceAsStream(DICT_STRING));
		} catch (Exception e) {
			throw new RuntimeException("fill to load default dict");
		}
	}


	private int currentIdx = 0;
	private Term currentTerm = null;
	private char[] sentence;
	private List<Long> offsets ;
	private int chars ;
	private boolean[] isPrefix  ;
	private boolean[] isSuffix;

	public SpSegmenter(String content){
		if (content == null){
			return;
		}
		this.sentence = new char[content.length()];
		isPrefix = new boolean[sentence.length + 1];
		isSuffix  = new boolean[sentence.length + 1];
		this.offsets =   new ArrayList<Long>();
		this.seg(content);
	}

	/**
	 * TODO
	 * @return
	 */
	public Term next(){
		if (this.sentence == null){
			return null;
		}
		if (currentTerm == null){
			currentTerm = new Term(sentence, 0 , 0);
		}
		if (currentIdx >= offsets.size()){
			return null;
		}
		boolean ok = isLegal(currentIdx);
		while( ok == false && currentIdx < chars){
			currentIdx += 1;
			ok = isLegal(currentIdx);
		}
		
		int s = (int)((offsets.get(currentIdx) & 0xffffffff00000000l)>> 32);
		int e = (int)(offsets.get(currentIdx) & 0xffffffffl);
		currentTerm.move(s, e);
		currentIdx += 1;
		return currentTerm;
	}

	private boolean isLegal(int i){

		int s = (int)((offsets.get(i) & 0xffffffff00000000l)>> 32);
		int e = (int)(offsets.get(i) & 0xffffffffl);
		if (i < chars){
			if (isPrefix[s] && isPrefix[e] == false ){
				return false;
			}
			if (isSuffix[s] == false && isSuffix[e] ){
				return false;
			}
		}
		return true;
		

	}


	public synchronized void addWord(String word){
		Library.insertWord(forest, word);
	}

	public synchronized void removeWord(String word){
		Library.removeWord(forest, word);
	}


	private int[] transformSentence(String sent, char[] newsent){
		int[] charTypes =  new int[newsent.length];
		for(int i = 0 ; i < sent.length(); i++){
			char ch =sent.charAt(i);
			if (ch >= 0x4E00 && ch <= 0x9FA5)
				charTypes[i] = StringNode.HANZI ;
			else if ((ch >= 0x0041 && ch <= 0x005A) ){
				charTypes[i] = StringNode.LETTER ;
				ch += 0x0020;
			}else if(ch >= 0x0061 && ch <= 0x007A)
				charTypes[i] = StringNode.LETTER ;
			else if (ch >= 0x0030 && ch <= 0x0039)
				charTypes[i] = StringNode.NUMBER ;
			else if (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n' || ch == '　'){
				charTypes[i] = StringNode.SPACELIKE ;	
				ch = ' ';
			}
			// Punctuation Marks
			else if ((ch >= 0x0021 && ch <= 0x00BB) || (ch >= 0x2010 && ch <= 0x2642)
					|| (ch >= 0x3001 && ch <= 0x301E))
				charTypes[i] = StringNode.DELIMITER ;

			// Full-Width range
			else if ((ch >= 0xFF21 && ch <= 0xFF3A) ){
				charTypes[i] = StringNode.LETTER ;
				ch -= 0xFEE0 ;
				ch += 0x0020;
			}else if(ch >= 0xFF41 && ch <= 0xFF5A){
				charTypes[i] = StringNode.LETTER ;
				ch -= 0xFEE0 ; 
			}else if (ch >= 0xFF10 && ch <= 0xFF19){
				charTypes[i] = StringNode.NUMBER ;
				ch -= 0xFEE0 ;
			}else if (ch >= 0xFE30 && ch <= 0xFF63){
				if (ch> 65280&& ch< 65375)
					ch -= 0xFEE0 ;
				charTypes[i] = StringNode.DELIMITER ;
			}else{
				if (ch> 65280&& ch< 65375)
					ch -= 0xFEE0 ;
				charTypes[i] = StringNode.DELIMITER ;
			}
			newsent[i] = ch;
		}
		return charTypes;
	}

	private Long makeOffset(int i, int i1){
		long c = i1;
		c |= (((long)i) << 32);
		return new Long(c);
	}

	private void seg(String content){
		//		char[] newSentence = new char[content.length()];
		int[] charTypes = transformSentence(content, sentence);
		
		boolean keepAppend = false;
		int offsetEnd = -1;
		int offsetStart = -1;
		for(int i = 0 ; i < sentence.length;){
//			char ch = sentence[i];
			int ctype = charTypes[i];
			if (ctype == StringNode.SPACELIKE){
				//space_like char

				int j = i + 1;
				while (j < sentence.length && charTypes[j] == StringNode.SPACELIKE)
					j++;
				i = j;
//				if (keepAppend == true){
//					offsets.add( makeOffset(offsetStart, offsetEnd));				
//				}
//				keepAppend = false;
//				offsetStart = i;
//				offsetEnd = i;
			}else if (ctype == StringNode.HANZI){
				//hanzi

				if (keepAppend == true){
					offsets.add( makeOffset(offsetStart, offsetEnd));
				}
				offsets.add(makeOffset(i, i+1));
				i++;
//				keepAppend = false;
//				offsetStart = i;
//				offsetEnd = i;
			}else if (ctype == StringNode.LETTER){
				//letter , Full-Width has been transformed
				int j = i + 1;
				while(j < sentence.length && charTypes[j] == StringNode.LETTER  )
					j++;
				offsets.add(makeOffset(i, j));
				i = j;
//				if(keepAppend == true){
//					offsetEnd = j;
//				}
//				keepAppend = true;
			}else if (ctype == StringNode.NUMBER){
				//digits , Full-Width has been transformed
				int j = i + 1;
				while(j < sentence.length && charTypes[j]  == StringNode.NUMBER ){
					j++;
				}
				offsets.add(makeOffset(i, j));
				i = j;
//				if(keepAppend == true){
//					offsetEnd = j;
//				}
//				keepAppend = true;
			}
			else if (ctype == StringNode.DELIMITER){
				//delimiter , whether full-width or not
				++i;
//				if(keepAppend == true){
//					offsetEnd = i-1;
//				}
			}else{
				//other
				offsets.add( makeOffset(i, i+1));
				++i;
//				if (keepAppend == true){
//					offsets.add( makeOffset(offsetStart, offsetEnd));
//				}
//				keepAppend = false;
//				offsetStart = i;
//				offsetEnd = i;
			}
		}
		if(keepAppend == true && offsetStart < offsetEnd){
			offsets.add( makeOffset(offsetStart, offsetEnd));
		}

		chars = offsets.size();
		GetWord udg = forest.getWord(content);

		String temp= null;
		while ((temp = udg.getAllWords()) != null){
			int start = udg.getStart();
			int end = udg.getEnd();
//			System.out.println(temp + "\t\t" + udg.offe + "\t\t" + udg.getEnd());
			isPrefix[start] = true;
			isSuffix[end] = true;
			offsets.add(makeOffset(start, end));
		}
	}

	public static void main(String[] args) {
		SpSegmenter spSegmenter = new SpSegmenter("原来inwar在出卖客户隐身");
		Term t = spSegmenter.next();
		while(t!= null){
			System.out.println(t.toString());
			t = spSegmenter.next();
		}
	}
}
