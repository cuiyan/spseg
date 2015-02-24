package org.shanbo.spseg.test;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.shanbo.spseg.lucene.SpTokenizerFactory;

public class TestSpTokenizer {

	public static void main(String[] args) throws IOException {
		StringReader s = new StringReader("原来inwar在出卖客户隐身");
		
		Tokenizer create = new SpTokenizerFactory(Collections.EMPTY_MAP).create(s);
		create.reset();
		while (create.incrementToken()){
			CharTermAttribute attribute = create.getAttribute(CharTermAttribute.class);
			System.out.println(attribute.toString());
		}
	}

}
