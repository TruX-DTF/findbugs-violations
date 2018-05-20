package edu.lu.uni.serval.cluster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.lu.uni.serval.utils.FileHelper;
import weka.clusterers.XMeans;
import weka.core.ChebyshevDistance;
import weka.core.DistanceFunction;
import weka.core.Instances;

/**
 * X-means cluster.
 * 
 * @author kui.liu
 *
 */
public class XMeansCluster {

	private static Logger log = LoggerFactory.getLogger(XMeansCluster.class);
	
	/**
	 * Default values of parameters for X-means cluster algorithm.
	 * 
	 * More information: http://weka.sourceforge.net/doc.packages/XMeans/weka/clusterers/XMeans.html
	 */
	private DistanceFunction distanceF = new ChebyshevDistance();//new FilteredDistance();//new MinkowskiDistance();//new ManhattanDistance();//new EuclideanDistance();
	private boolean useKDTree = true;
	private int maxNumIterations = 10000;
	private int maxKMeans= 1000;
	private int maxKMeansForChildren = 1000;
	private int maxNumClusters = 2;
	private int minNumClusters = 2;
	private int seed = 1;
	
	public static void main(String[] args) throws Exception {
		String featureFile = "../OUTPUT/CNN_extracted_feature/embedded_data/10_feature_seprated_ast_node_name_and_raw_tokenSIZE=50.csv";
		String arffFile = "../OUTPUT/XMeans_results/input.arff";
		String clusterResults = "../OUTPUT/XMeans_results/feature_seprated_ast_node_name_and_raw_tokenSIZE=50.csv";
		XMeansCluster cluster = new XMeansCluster();
		DataPreparer.prepareData(featureFile, arffFile);
		cluster.cluster(arffFile, clusterResults);
	}

	public void cluster(String inputDataFile, String outputResultsFile) throws Exception {
		XMeans xmeans = new XMeans();
		/*
		 * Sets the distance value between true and false of binary attributes.
		 */
		//xmeans.setBinValue(1);
		
		/*
		 * cutoff factor, takes the given percentage of the splitted centroids
		 * if none of the children win
		 */
		// xmeans.setCutOffFactor(0);
		// xmeans.setDebug(true); //Set debugging mode.
		// xmeans.setDebugLevel(0);// Sets the debug level. debug level = 0, means no output
		// xmeans.setDebugVectorsFile(null);//Only used for debugging reasons.
		
		/*
		 *  default weka.core.EuclideanDistance: new EuclideanDistance()
		 */
		xmeans.setDistanceF(distanceF); 
		// xmeans.setDoNotCheckCapabilities(true);
		// xmeans.setInputCenterFile(null);
		// xmeans.setKDTree(new KDTree());
		xmeans.setUseKDTree(useKDTree);
		xmeans.setMaxIterations(maxNumIterations);
		xmeans.setMaxKMeans(maxKMeans);
		xmeans.setMaxKMeansForChildren(maxKMeansForChildren);
		xmeans.setMaxNumClusters(maxNumClusters);
		xmeans.setMinNumClusters(minNumClusters);
		// xmeans.setOptions(null);
		// xmeans.setOutputCenterFile(null);
		xmeans.setSeed(seed);

		log.info("Read data...");
		BufferedReader datafile = readDataFile(inputDataFile);
		if (datafile != null) {
			Instances data = new Instances(datafile);
			log.info("XMeans Clustering is beginning...");
			xmeans.buildClusterer(data);

			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < data.numInstances(); i++) {
				int clusterNum = xmeans.clusterInstance(data.instance(i));
				log.info("Instance " + i + " -> Cluster " + clusterNum + " \n");
				builder.append(clusterNum + "\n");
			}
			
			File file = new File(outputResultsFile);
			if (file.exists()) file.delete();
			FileHelper.outputToFile(outputResultsFile, builder, false);
			log.info("Clusters: " + xmeans.numberOfClusters());
		}
	}
	
	private BufferedReader readDataFile(String filename) {
		BufferedReader inputReader = null;
		try {
			if (filename.endsWith(".arff")) {
				inputReader = new BufferedReader(new FileReader(filename));
			} else {
				log.error(filename + " is a wrong file! Input file must be .arff file!");
			}
		} catch (FileNotFoundException ex) {
			log.error("File not found: " + filename);
		}
		return inputReader;
	}

	public void setDistanceF(DistanceFunction distanceF) {
		this.distanceF = distanceF;
	}

	public void setUseKDTree(boolean useKDTree) {
		this.useKDTree = useKDTree;
	}

	public void setMaxNumberOfIterations(int maxNumberOfIterations) {
		this.maxNumIterations = maxNumberOfIterations;
	}

	public void setMaxKMeans(int maxKMeans) {
		this.maxKMeans = maxKMeans;
	}

	public void setMaxKMeansForChildren(int maxKMeansForChildren) {
		this.maxKMeansForChildren = maxKMeansForChildren;
	}

	public void setMaxNumClusters(int maxNumClusters) {
		this.maxNumClusters = maxNumClusters;
	}

	public void setMinNumClusters(int minNumClusters) {
		this.minNumClusters = minNumClusters;
	}

	public void setSeed(int seed) {
		this.seed = seed;
	}

}
