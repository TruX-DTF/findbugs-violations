package edu.lu.uni.serval.mining.pattern.violation;

import java.io.File;

import edu.lu.uni.serval.config.Configuration;
import edu.lu.uni.serval.mining.utils.ClusterAnalyser;

public class ClusterResults {
	
	public void analyzeFixedViolations() {
		String fixedViolationPath = Configuration.ROOT_PATH + "Violations_tokens/fixedViolations/";
		analyzeClusterResults(fixedViolationPath);
	}
	
	public void analyzeUnfixedViolations() {
		String unfixedViolationPath = Configuration.ROOT_PATH + "Violations_tokens/unfixedViolations/";
		analyzeClusterResults(unfixedViolationPath);
	}

	private void analyzeClusterResults(String violationPath) {
		File[] violationFolders = new File(violationPath).listFiles();
		
		for (File file : violationFolders) {
			if (file.isDirectory()) {
				String path = file.getPath();
				ClusterAnalyser analyser = new ClusterAnalyser();
				analyser.readClusterResults(path + "/clusterOutput.list");
				analyser.clusterPatchSourceCode(path + "/violationsInfo.list", 
						path + "/clusteredViolations/", "VIOLATION###");
				analyser.clusterBuggyCodeTokens(path + "/violationsTokens.list",  path + "/"); 	
				analyser.clusterBuggyCodeFeatures(path + "/violationFeatures.list",  path + "/"); // features
			}
		}
	}

}
