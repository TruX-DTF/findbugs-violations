package edu.lu.uni.serval.defects4j;

import java.io.IOException;

public class MainProcess {

	public static void main(String[] args) {
		try {
			new DataPreparer().prepare();
			
			new TokenEmbedder().embed();
			
			new FeatureLearner().learn();
			
			new Feature().assign();
			new Feature().separateFeature();
			
			new FixPatternMatcher().match();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
