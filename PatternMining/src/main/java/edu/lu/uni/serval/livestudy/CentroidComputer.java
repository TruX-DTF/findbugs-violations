package edu.lu.uni.serval.livestudy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import edu.lu.uni.serval.config.Configuration;
import edu.lu.uni.serval.fix.patterns.matching2.Centroid;
import edu.lu.uni.serval.utils.FileHelper;

/**
 * Compute the centroid of each cluster of fixed violations.
 * 
 * @author kui.liu
 *
 */
public class CentroidComputer {

	public void compute() {
		/**
		 * indexes of selected violations from mining input.
		 * 
		 */
		String unfixedAlarms = Configuration.ROOT_PATH + "Alarms_tokens/unfixedAlarms/";
		File[] unfixedAlarmsFolders = new File(unfixedAlarms).listFiles();
		for (File file : unfixedAlarmsFolders) {// Violation Type
			if (file.isDirectory()) {
				String path = file.getPath() + "/ClusteredFeatures/";
				List<File> featureFiles = FileHelper.getAllFilesInCurrentDiectory(path, ".list");
				
				for (File featureFile : featureFiles) {// Clusters
					String fileName = featureFile.getPath();
					List<Double[]> features = readFeatures(fileName);
					Double[] centroid = new Centroid().computerCentroid(features);
					
					String outputFile = fileName.replace("ClusteredFeatures", "ClusterCentroids").replace("Features", "Centroid");
					String centroidStr = "";
					for (int i = 0; i < 299; i ++) {
						centroidStr += centroid[i] + ", ";
					}
					centroidStr += centroid[299];
					FileHelper.outputToFile(outputFile, centroidStr, false);
				}
			}
		}
	}
	
	public List<Double[]> readFeatures(String inputFile) {
		List<Double[]> list = new ArrayList<>();
		FileInputStream fis = null;
		Scanner scanner = null;
		try {
			fis = new FileInputStream(inputFile);
			scanner = new Scanner(fis);
			while(scanner.hasNextLine()) {
				String line = scanner.nextLine();
				Double[] features = doubleParseFeature(line);
				list.add(features);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				scanner.close();
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return list;
	}
	
	private Double[] doubleParseFeature(String feature) {
		String[] features = feature.split(", ");
		int length = features.length;
		Double[] doubleFeatures = new Double[length];
		for (int i = 0; i < length; i ++) {
			doubleFeatures[i] = Double.parseDouble(features[i]);
		}
		return doubleFeatures;
	}
}
