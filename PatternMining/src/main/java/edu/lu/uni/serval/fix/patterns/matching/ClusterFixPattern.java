package edu.lu.uni.serval.fix.patterns.matching;

import java.io.File;
import java.io.IOException;
import java.util.List;

import edu.lu.uni.serval.config.Configuration;
import edu.lu.uni.serval.mining.pattern.violation.ClusterViolation;
import edu.lu.uni.serval.mining.utils.ClusterAnalyser;
import edu.lu.uni.serval.mining.utils.DataPreparation;
import edu.lu.uni.serval.utils.FileHelper;

public class ClusterFixPattern {
	
	public void cluster() throws IOException {
		String outputTypePath = Configuration.ROOT_PATH + "RQ3/UnfixedViolations/";
		File[] violationTypes = new File(outputTypePath).listFiles();
		for (File file : violationTypes) {
			if (file.isDirectory()) {
				String filePath = file.getPath();
//				if (!filePath.endsWith("SE_NO_SERIALVERSIONID")) continue;
				String inputData = filePath + "/LearnedFeatures/1_CNNoutput.csv";
				List<String> features = DataPreparation.readStringList(inputData);
				int subSize = 33495;//TODO
				StringBuilder subFeatures = new StringBuilder();
				for (int index = 0; index < subSize; index ++) {
					subFeatures.append(features.get(index) + "\n");
				}
				FileHelper.outputToFile(filePath + "/FixPatterns/PatternFeatures.list", subFeatures, false);
				subFeatures.setLength(0);
				
				int size = features.size();
				for (int index = subSize; index < size; index ++) {
					subFeatures.append(features.get(index) + "\n");
				}
				System.out.println(file.getName() + (size - subSize));
				FileHelper.outputToFile(filePath + "/LearnedFeatures.list", subFeatures, false);
				
				// prepare cluster data.
				String clusterInputFile = filePath + "/clusterInput.arff";
				String clusterResults = filePath + "/clusterOutput.list";
				
				StringBuilder clusterInput = new StringBuilder();
				clusterInput.append("@relation Data_for_clustering\n\n");
				for (int i = 0; i < 300; i ++) {
					clusterInput.append("@attribute attribute_" + i + " numeric\n");
				}
				clusterInput.append("\n@data\n");
				clusterInput.append(subFeatures.toString().replaceAll(", ", ",") + "\n");
				FileHelper.outputToFile(clusterInputFile, clusterInput, false);
				
				new ClusterViolation().cluster(clusterInputFile, clusterResults);
				
				
				ClusterAnalyser analyser = new ClusterAnalyser();
				analyser.readClusterResults(clusterResults);
				analyser.clusterPatchSourceCode(filePath + "/SelectedSourceCode.list", 
						Configuration.ROOT_PATH + "RQ3_1/" + file.getName() + "/ClusterSourceCode/", "##Source_Code:");
				analyser.clusterBuggyCodeFeatures(filePath + "/LearnedFeatures.list", filePath + "/");
			}
		}
	}

}
