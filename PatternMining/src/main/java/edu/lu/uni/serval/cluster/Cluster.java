package edu.lu.uni.serval.cluster;

import edu.lu.uni.serval.config.Configuration;
import weka.core.EuclideanDistance;

/**
 * Cluster features with X-means clustering algorithm.
 * 
 * @author kui.liu
 *
 */
public class Cluster {

	public void cluster() {
		String arffFile = Configuration.CLUSTER_INPUT;
		String clusterResults = Configuration.CLUSTER_OUTPUT;
		
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
			cluster.cluster(arffFile, clusterResults);
			// X-means clustering is finished.
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void cluster(int amount, String dataPath, String featureFileName) {
		String featureFile = dataPath + featureFileName;
		String arffFile = dataPath + "clusterInput.arff";
		String clusterOutput = dataPath + "clusterOutput.list";
		DataPreparer.prepareData(featureFile, arffFile);
		
		XMeansCluster cluster = new XMeansCluster();
		try {
			/*
			 * The below 5 parameters have default values.
			 */
			cluster.setDistanceF(new EuclideanDistance());
			cluster.setUseKDTree(true);
			cluster.setMaxNumberOfIterations(1000);
			// The below 2 parameters are recommended to be the same.
			cluster.setMaxKMeans(amount / 2);
			cluster.setMaxKMeansForChildren(amount / 2);
			
			/*
			 * The values of the below 3 parameters should be set by developers.
			 */
			cluster.setSeed(100);
			cluster.setMaxNumClusters(amount);
			cluster.setMinNumClusters(1);
			
			// X-means clustering is beginning.
			cluster.cluster(arffFile, clusterOutput);
			// X-means clustering is finished.
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
