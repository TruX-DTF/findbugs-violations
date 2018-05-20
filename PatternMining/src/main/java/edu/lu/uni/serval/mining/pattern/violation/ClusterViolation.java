package edu.lu.uni.serval.mining.pattern.violation;

import java.io.File;

import edu.lu.uni.serval.cluster.XMeansCluster;
import edu.lu.uni.serval.config.Configuration;
import weka.core.EuclideanDistance;

public class ClusterViolation {

	public void clusterFixedViolations() {
		String fixedViolationClusterInputs = Configuration.ROOT_PATH + "Violations_tokens/fixedViolations/";
		clusterViolations(fixedViolationClusterInputs);
		ClusterResults cr = new ClusterResults();
		cr.analyzeFixedViolations();
	}
	
	public void clusterUnfixedViolations() {
		String unfixedViolationClusterInputs = Configuration.ROOT_PATH + "Violations_tokens/unfixedViolations/";
		clusterViolations(unfixedViolationClusterInputs);
		ClusterResults cr = new ClusterResults();
		cr.analyzeUnfixedViolations();
	}
	
	private void clusterViolations(String clusterInputs) {
		File[] violationTypesFolders = new File(clusterInputs).listFiles();
		for (File violationTypesFolder : violationTypesFolders) {
			if (violationTypesFolder.isDirectory()) {
				String filePath = violationTypesFolder.getPath();
				cluster(filePath + "/clusterInput.arff", filePath + "/clusterOutput.list");
			}
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
