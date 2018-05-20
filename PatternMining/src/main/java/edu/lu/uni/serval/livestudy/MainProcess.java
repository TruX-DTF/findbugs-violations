package edu.lu.uni.serval.livestudy;

import java.io.IOException;

public class MainProcess {

	public static void main(String[] args) {
		try {
			new DataPreparer().prepare();
			
			new TokenEmbedder().embed();
			
			new FeatureLearner().learner();
			
			new Feature().assign();
			
			new CentroidComputer().compute();
			
			new FixPatternMatcher().match();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
