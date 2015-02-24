package org.shanbo.spseg.lucene;

import java.io.Reader;
import java.util.Map;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeSource.AttributeFactory;

public class SpTokenizerFactory  extends TokenizerFactory {

	public SpTokenizerFactory(Map<String, String> args) {
		super(args);
	}

	public Tokenizer create(AttributeFactory attributeFactory, Reader in) { // 会多次被调用
		return new SpTokenizer(in);
	}
	
}
