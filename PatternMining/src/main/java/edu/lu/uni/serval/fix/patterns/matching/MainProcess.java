package edu.lu.uni.serval.fix.patterns.matching;

import java.io.IOException;

public class MainProcess {

	public static void main(String[] args) {
		DataPreparer dp = new DataPreparer();
		try {
			dp.prepare();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		TokenEmbedder te = new TokenEmbedder();
		try {
			te.embed();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		FeatureLearner fl = new FeatureLearner();
		fl.learn();
		
		Feature f = new Feature();
		try {
			f.separateFeatures();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		ClusterFixPattern cfp = new ClusterFixPattern();
		try {
			cfp.cluster();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		CentroidComputer cc = new CentroidComputer();
		cc.compute();
		
		FixPatternMatcher fpm = new FixPatternMatcher();
		try {
			fpm.match();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
