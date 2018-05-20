package edu.lu.uni.serval.fix.patterns.matching2;

import java.io.IOException;

public class MainProcess {

	public static void main(String[] args) {
		try {
			new DataPreparer().prepare();
			
			new TokenEmbedder().embed();
			
			new FeatureLearner().learn();
			
			new Feature().separateFeature();
			
			new DataIndexes().index();
			
			new CentroidComputer().compute();
			
			new FixPatternMatcher().match();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
