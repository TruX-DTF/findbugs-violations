package edu.lu.uni.serval.mining.fix.pattern;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.lu.uni.serval.config.Configuration;
import edu.lu.uni.serval.mining.utils.ClusterAnalyser;
import edu.lu.uni.serval.mining.utils.CommonPatterns;
import edu.lu.uni.serval.utils.FileHelper;

/**
 * Analyze cluster results to obtain common fix patterns.
 * 
 * @author kui.liu
 *
 */
public class ClusterResult {

	public void analyze() {
		boolean isViolation = true;
		if (isViolation) {
			String clusterResults = Configuration.ROOT_PATH + "Alarms_Patches/";
			FileHelper.deleteDirectory(clusterResults);
			
			Map<String, Integer> alarmTypes = new HashMap<>();
			alarmTypes = new FixPatternCluster().readAlarmTypes(Configuration.ALARMS + "AlarmTypesAmount.list");
			
			for (Map.Entry<String, Integer> entry : alarmTypes.entrySet()) {
				String alarmTypePath = entry.getKey() + "/";
				// analyze cluster results.
				ClusterAnalyser analyser = new ClusterAnalyser();
				analyser.readClusterResults(Configuration.ALARMS + alarmTypePath + "clusterOutput.list");
				analyser.clusterPatchSourceCode(Configuration.ALARMS + alarmTypePath + "patchSourceCode.list", 
						clusterResults + alarmTypePath + "ClusteredPatches/", Configuration.PATCH_SIGNAL);
				analyser.clusterBuggyCodeTokens(Configuration.ALARMS + alarmTypePath + "tokens.list", 
						clusterResults + alarmTypePath); 
				analyser.clusterBuggyCodeFeatures(Configuration.ALARMS + alarmTypePath + "sourceCodeFeatures.list", 
						clusterResults + alarmTypePath);
			}
		} else {
			String clusteredPatches = Configuration.CLUSTERED_PATCHES_FILE;
			String clusteredBuggyTokens = Configuration.CLUSTERED_TOKENSS_FILE;
			FileHelper.deleteDirectory(clusteredPatches);
			FileHelper.deleteDirectory(clusteredBuggyTokens);
			
			// analyze cluster results.
			ClusterAnalyser analyser = new ClusterAnalyser();
			analyser.readClusterResults();
			analyser.clusterPatchSourceCode();
			analyser.clusterBuggyCodeTokens();  // the results will be used to compute similarity with target java code to localize bugs.
		
			List<Integer> clusterResults = analyser.getClusterResults();
			
			// Common patterns.
			CommonPatterns commonPatterns = new CommonPatterns();
			// <Integer, Integer>: <ClusterNum, Label for supervised learning>
			Map<Integer, Integer> commonClustersMappingLabel = commonPatterns.identifyCommonPatterns(clusterResults);
			String clusterMappingLabel = "Label : ClusterNum\n";
			for (Map.Entry<Integer, Integer> entry : commonClustersMappingLabel.entrySet()) {
				clusterMappingLabel += entry.getValue() + " : " + entry.getKey() + "\n";
			}
			FileHelper.outputToFile(Configuration.CLUSTERNUMBER_LABEL_MAP, clusterMappingLabel, false);
	
			int totalNumberOfTrainingData = commonPatterns.getTotalNumberofTrainingData();
			FileHelper.outputToFile(Configuration.NUMBER_OF_TRAINING_DATA, "" + totalNumberOfTrainingData, false);
		}
	}

}
