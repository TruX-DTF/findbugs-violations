package edu.lu.uni.serval.mining.pattern.violation;

public class PatternMiner {

	public static void main(String[] args) {
		// Select data for pattern mining.
		DataSelecter ds = new DataSelecter();
		ds.selectData();
		
		// Embed tokens of violation code with Word2Vec.
		TokenEmbedder te = new TokenEmbedder();
		te.embed();
		
		// Learn feature of violation code with CNNs.
		FeatureLearner fl = new FeatureLearner();
		fl.learn();
		
		// Cluster violation code to mine violation patterns.
		ClusterViolation cv = new ClusterViolation();
		cv.clusterFixedViolations();
		cv.clusterUnfixedViolations();
	}

}
