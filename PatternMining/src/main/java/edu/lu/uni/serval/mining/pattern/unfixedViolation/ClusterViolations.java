package edu.lu.uni.serval.mining.pattern.unfixedViolation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.lu.uni.serval.cluster.XMeansCluster;
import edu.lu.uni.serval.config.Configuration;
import edu.lu.uni.serval.mining.utils.ClusterAnalyser;
import edu.lu.uni.serval.utils.FileHelper;
import weka.core.EuclideanDistance;

/**
 * Cluster violations.
 * 
 * @author kui.liu
 *
 */
public class ClusterViolations {

	public void cluster() {
		// Configuration.ROOT_PATH + "UnfixedViolations/" + #violationType# + "/LearnedFeatures/" + i_CNNOutput.csv
		// Configuration.ROOT_PATH + "UnfixedViolations/" + #violationType# + "/ClusterInput/clusterInput.arff"
		File clusteredTypesFile = new File(Configuration.ROOT_PATH + "ClusterResults_UnFixed/");
		File[] clusteredTypes = clusteredTypesFile.listFiles();
		List<String> types = new ArrayList<>();
		for (File file : clusteredTypes) {
			types.add(file.getName());
		}
		File violationTypesFile = new File(Configuration.ROOT_PATH + "UnfixedViolations/");
		File[] violationTypes = violationTypesFile.listFiles();
		for (File violationType : violationTypes) {
			String type = violationType.getName();
			String filePath = violationType.getPath();
			
			String FeatureFileName = filePath + "/LearnedFeatures/20_CNNInput.csv";
			
			StringBuilder clusterInput = new StringBuilder();
			clusterInput.append("@relation Data_for_clustering\n\n");
			for (int i = 0; i < 300; i ++) {
				clusterInput.append("@attribute attribute_" + i + " numeric\n");
			}
			clusterInput.append("\n@data\n");
			clusterInput.append(FileHelper.readFile(FeatureFileName).replaceAll(", ", ",") + "\n");
			FileHelper.outputToFile(filePath + "/ClusterInput/clusterInput.arff", clusterInput, false);
			
			
			String input = filePath + "/ClusterInput/clusterInput.arff";
			String output = filePath + "/ClusterOutput/clusterOutput.list";
			cluster(input, output);
			
			String clusterResults = filePath + "/ClusterOutput/clusterOutput.list";
			ClusterAnalyser analyser = new ClusterAnalyser();
			analyser.readClusterResults(clusterResults);
			analyser.clusterPatchSourceCode(filePath + "/selectedData/SelectedSourceCode.list", 
					"../FPM_Violations/ClusterResults_UnFixed/" + type + "/", "##Source_Code:");
		}
	}

	public void cluster(String inputFile, String outputFile) {
		XMeansCluster cluster = new XMeansCluster();
		try {
			/*
			 * The below 5 parameters have default values.
			 */
			cluster.setDistanceF(new EuclideanDistance());
			cluster.setUseKDTree(true);
			cluster.setMaxNumberOfIterations(1000);
			// The below 2 parameters are recommended to be the same.
			cluster.setMaxKMeans(500);
			cluster.setMaxKMeansForChildren(500);
			
			/*
			 * The values of the below 3 parameters should be set by developers.
			 */
			cluster.setSeed(500);
			cluster.setMaxNumClusters(500);
			cluster.setMinNumClusters(1);
			
			// X-means clustering is beginning.
			cluster.cluster(inputFile, outputFile);
			// X-means clustering is finished.
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
