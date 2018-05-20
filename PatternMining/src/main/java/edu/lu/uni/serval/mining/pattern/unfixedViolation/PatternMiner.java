package edu.lu.uni.serval.mining.pattern.unfixedViolation;

public class PatternMiner {

	public static void main(String[] args) {
		// Prepare data.
		DataPreparer dp = new DataPreparer();
		dp.prepare();
		
		// Embed tokens.
		TokenEmbedder te = new TokenEmbedder();
		te.embed();
		
		// Learn feature.
		FeatureLearner fl = new FeatureLearner();
		fl.learn();
		
		// Cluster violations.
		ClusterViolations vc = new ClusterViolations();
		vc.cluster();
	}

}
