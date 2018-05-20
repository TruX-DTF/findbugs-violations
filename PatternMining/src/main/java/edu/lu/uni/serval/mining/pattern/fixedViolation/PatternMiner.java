package edu.lu.uni.serval.mining.pattern.fixedViolation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import edu.lu.uni.serval.cluster.XMeansCluster;
import edu.lu.uni.serval.config.Configuration;
import edu.lu.uni.serval.mining.utils.ClusterAnalyser;
import edu.lu.uni.serval.mining.utils.DataPreparation;
import edu.lu.uni.serval.utils.FileHelper;
import weka.core.EuclideanDistance;

/**
 * Prepare data for clustering.
 * 
 * @author kui.liu
 *
 */
public class PatternMiner {
	
	public static void main(String[] args) {
		DataPreparer dp = new DataPreparer();
		try {
			dp.prepare();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		TokenEmbedder te = new TokenEmbedder();
		te.embed();
		
		FeatureLearner fl = new FeatureLearner();
		fl.learn();
		
		PatternMiner pm = new PatternMiner();
		pm.mine();
	}

	public void mine() {
		List<String> finishedTypes = readTypes();
		String filePath = Configuration.ROOT_PATH + "fixedViolations/Types/";
		File[] typeFiles = new File(filePath).listFiles();
		for (File typeFile : typeFiles) {
			if (typeFile.isDirectory()) {
				
				if (finishedTypes.contains(typeFile.getName())) continue;
				
				String path = typeFile.getPath() + "/";
				String FeatureFileName = path + "LearnedFeatures/1_CNNoutput.csv";
				
				StringBuilder clusterInput = new StringBuilder();
				clusterInput.append("@relation Data_for_clustering\n\n");
				for (int i = 0; i < 300; i ++) {
					clusterInput.append("@attribute attribute_" + i + " numeric\n");
				}
				clusterInput.append("\n@data\n");
				clusterInput.append(FileHelper.readFile(FeatureFileName).replaceAll(", ", ",").replaceAll("\\[", "").replaceAll("\\]", "") + "\n");
				FileHelper.outputToFile(path + "clusterInput.arff", clusterInput, false);
				
				
				String input = path + "clusterInput.arff";
				String output = path + "clusterOutput.list";
				cluster(input, output);
				
				ClusterAnalyser analyser = new ClusterAnalyser();
				analyser.readClusterResults(output);
				analyser.clusterPatchSourceCode(path + "SourceCode.list", 
						Configuration.ROOT_PATH + "fixedViolationsClusters/" + typeFile.getName() + "/", "##Source_Code:");
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
			cluster.setMaxKMeans(200);
			cluster.setMaxKMeansForChildren(200);
			
			/*
			 * The values of the below 3 parameters should be set by developers.
			 */
			cluster.setSeed(500);
			cluster.setMaxNumClusters(200);
			cluster.setMinNumClusters(1);
			
			// X-means clustering is beginning.
			cluster.cluster(inputFile, outputFile);
			// X-means clustering is finished.
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List<String> readTypes() {
		List<String> types = new ArrayList<>();
		File[] typeFiles = new File(Configuration.ROOT_PATH + "fixedViolationsClusters/").listFiles();
		for (File typeFile : typeFiles) {
			if (typeFile.isDirectory()) {
				types.add(typeFile.getName());
			}
		}
		return types;
	}

	public void prepareData() throws IOException {
		// Configuration.ROOT_PATH + "fixedViolations/LearnedFeatures/" +
		// i_CNNOutput.csv
		// Configuration.ROOT_PATH +
		// "fixedViolations/ClusterInput/clusterInput.arff"
		String featureFileName = Configuration.ROOT_PATH + "fixedViolations/LearnedFeatures/1_CNNoutput.csv";
		List<String> features = DataPreparation.readStringList(featureFileName);

		Map<String, StringBuilder> featuresMap = new HashMap<>();
		Map<String, StringBuilder> sourceCodeMap = new HashMap<>();

		FileInputStream fis = new FileInputStream(
				Configuration.ROOT_PATH + "fixedViolations/selectedData/SelectedSourceCode.list");
		Scanner scanner = new Scanner(fis);

		int index = -1;
		String singleViolation = "";
		boolean isViolationType = false;
		String violationType = "";
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.startsWith("##Source_Code:")) {
				isViolationType = true;
				if (index >= 0) {
					if (featuresMap.containsKey(violationType)) {
						featuresMap.get(violationType).append(features.get(index)).append("\n");
						sourceCodeMap.get(violationType).append(singleViolation);// .append("\n");
					} else {
						StringBuilder feature = new StringBuilder();
						feature.append(features.get(index)).append("\n");
						featuresMap.put(violationType, feature);
						StringBuilder builder = new StringBuilder();
						builder.append(singleViolation);
						sourceCodeMap.put(violationType, builder);
					}
				}
				singleViolation = "";
				index++;
			} else if (isViolationType) {
				violationType = line;
				isViolationType = false;
			}
			singleViolation += line + "\n";
		}

		if (featuresMap.containsKey(violationType)) {
			featuresMap.get(violationType).append(features.get(index)).append("\n");
			sourceCodeMap.get(violationType).append(singleViolation);// .append("\n");
		} else {
			StringBuilder feature = new StringBuilder();
			feature.append(features.get(index)).append("\n");
			featuresMap.put(violationType, feature);
			StringBuilder builder = new StringBuilder();
			builder.append(singleViolation);
			sourceCodeMap.put(violationType, builder);
		}

		scanner.close();
		fis.close();

		for (Map.Entry<String, StringBuilder> entry : featuresMap.entrySet()) {
			String type = entry.getKey();
			String path = Configuration.ROOT_PATH + "fixedViolations/Types/" + type;
			FileHelper.outputToFile(path + "/SourceCode.list", sourceCodeMap.get(type), false);

			StringBuilder clusterInput = new StringBuilder();
			clusterInput.append("@relation Data_for_clustering\n\n");
			for (int i = 0; i < 300; i++) {
				clusterInput.append("@attribute attribute_" + i + " numeric\n");
			}
			clusterInput.append("\n@data\n");
			clusterInput.append(entry.getValue().toString().replaceAll(", ", ",") + "\n");
			FileHelper.outputToFile(path + "/clusterInput.arff", clusterInput, false);
		}
	}

}
