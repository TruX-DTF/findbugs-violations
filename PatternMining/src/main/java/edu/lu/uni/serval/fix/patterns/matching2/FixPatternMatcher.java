package edu.lu.uni.serval.fix.patterns.matching2;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.lu.uni.serval.config.Configuration;
import edu.lu.uni.serval.utils.DistanceCalculator;
import edu.lu.uni.serval.utils.FileHelper;
import edu.lu.uni.serval.utils.MapSorter;

/**
 * Match fixed violation patterns in the same violation type.
 * 
 * @author kui.liu
 *
 */
public class FixPatternMatcher {

	public void match() throws IOException {
		String fixedViolationPatterns = Configuration.ROOT_PATH + "Violations/";
		File[] fixedviolationTypeFolders = new File(fixedViolationPatterns).listFiles();
		Map<String, List<Double[]>> fixedViolationFeatures = new HashMap<>(); // <Violation Type, Features>
		for (File file : fixedviolationTypeFolders) {
			if (file.isDirectory()) {
				String fileName = file.getPath() + "/sourceCodeFeatures.list";
				List<Double[]> features = readFeatures(fileName);
				fixedViolationFeatures.put(file.getName(), features);
			}
		}
		
		String unfixedViolations = Configuration.ROOT_PATH + "Violations_tokens/unfixedViolations/";
		File[] violationPatchFolders = new File(unfixedViolations).listFiles();
		for (File file : violationPatchFolders) {
			if (file.isDirectory()) {
				String violationType = file.getName();
				String path = file.getPath() + "/ClusterCentroids/";
				List<File> centroidFiles = FileHelper.getAllFilesInCurrentDiectory(path, ".list");
				
				for (File centroidFile : centroidFiles) {
					Double[] centroid = readCentroid(centroidFile);
					
					// Compute similarity in the same violation type.
					if (fixedViolationFeatures.containsKey(violationType)) {
						List<Double[]> matchedFixedViolationFeatures = fixedViolationFeatures.get(violationType);
						Map<Integer, Double> similarities = new HashMap<>();
						for (int i = 0, size = matchedFixedViolationFeatures.size(); i < size; i ++) {
							Double[] fixedViolationFeature = matchedFixedViolationFeatures.get(i);
							if (fixedViolationFeature == null) {
								similarities.put(i + 1, 0d);
							} else {
								Double similarity = Math.abs(computeSimilarity(centroid, fixedViolationFeature));
								similarities.put(i + 1, similarity);
							}
						}
						
						MapSorter<Integer, Double> mapSorter = new MapSorter<Integer, Double>();
						Map<Integer, Double> sortedSimilarities = mapSorter.sortByValueDescending(similarities);
						
						List<Integer> fixPatternIndexes = new ArrayList<>();
						double similarity = 0;
						int num = 0;
						for (Map.Entry<Integer, Double> entry : sortedSimilarities.entrySet()) {
							if (entry.getValue().equals(Double.NaN)) continue;
							if (entry.getValue() == similarity) {
								continue;
							}
							fixPatternIndexes.add(entry.getKey());
							similarity = entry.getValue(); 
							num ++;
							if (num == 10) break;
						}
						
						String matchedFixPattern = readFixPattern(violationType, fixPatternIndexes);
						String outputFileName = centroidFile.getPath().replace("ClusterCentroids", "MatchedFixPattern1").replace("Centroid", "FixPattern");
						FileHelper.outputToFile(outputFileName, matchedFixPattern, false);
						
					}
					
					// Compute similarity in the whold data set.
					String matchedFixPattern = computSimilarityWithAllData(centroid, fixedViolationFeatures, violationType);
					String outputFileName = centroidFile.getPath().replace("ClusterCentroids", "MatchedFixPattern2").replace("Centroid", "FixPattern");
					FileHelper.outputToFile(outputFileName, matchedFixPattern, false);
				}
				
				
			}
		}
	}

	/**
	 * Compute the similarities with all fixed violation patterns.
	 * 
	 * @param centroid
	 * @param fixedViolationFeatures
	 * @return
	 * @throws IOException 
	 * @throws NumberFormatException 
	 */
	private String computSimilarityWithAllData(Double[] centroid,
			Map<String, List<Double[]>> fixedViolationFeatures, String currentViolationType) throws NumberFormatException, IOException {
		Map<String, Double> similarities = new HashMap<>();
		
		for (Map.Entry<String, List<Double[]>> entry : fixedViolationFeatures.entrySet()) {
			String violationType = entry.getKey();
			if (currentViolationType.equals(violationType)) continue;
			List<Double[]> features = entry.getValue();
			Map<Integer, Double> similarity = computeSimilarityInOneViolationType(centroid, features);
			for (Map.Entry<Integer, Double> ent : similarity.entrySet()) {
				violationType += "@@" + ent.getKey();
				similarities.put(violationType, ent.getValue());
			}
		}
		
		MapSorter<String, Double> mapSorter = new MapSorter<String, Double>();
		Map<String, Double> sortedSimilarities = mapSorter.sortByValueDescending(similarities);
		
		List<String> fixPatternIndexes = new ArrayList<>();
		double similarity = -1d;
		int num = 0;
		for (Map.Entry<String, Double> entry : sortedSimilarities.entrySet()) {
			if (entry.getValue().equals(Double.NaN)) continue;
			if (entry.getValue() == similarity && similarity != 0d) {
				continue;
			}
			fixPatternIndexes.add(entry.getKey());
			similarity = entry.getValue(); 
			num ++;
			if (num == 10) break;
		}
		
		String matchedPatterns = "";
		for (int i = 0; i < 10; i ++) {
			String[] indexes = fixPatternIndexes.get(i).split("@@");
			matchedPatterns += readFixPattern(indexes[0], Integer.parseInt(indexes[1])) + "\n";
		}
		
		return matchedPatterns;
	}

	private Map<Integer, Double> computeSimilarityInOneViolationType(Double[] centroid, List<Double[]> features) {
		Map<Integer, Double> similarities = new HashMap<>();
		for (int i = 0, size = features.size(); i < size; i ++) {
			Double[] fixedViolationFeature = features.get(i);
			if (fixedViolationFeature == null) {
				similarities.put(i + 1, 0d);
			} else {
				Double similarity = Math.abs(computeSimilarity(centroid, fixedViolationFeature));
				similarities.put(i + 1, similarity);
			}
		}
		
		MapSorter<Integer, Double> mapSorter = new MapSorter<Integer, Double>();
		Map<Integer, Double> sortedSimilarities = mapSorter.sortByValueDescending(similarities);
		
		Map<Integer, Double> highestSimilarity = new HashMap<>();
		for (Map.Entry<Integer, Double> entry : sortedSimilarities.entrySet()) {
			if (entry.getValue().equals(Double.NaN)) continue;
			highestSimilarity.put(entry.getKey(), entry.getValue());
			break;
		}
		return highestSimilarity;
	}

	private String readFixPattern(String violationType, int fixPatternIndex) throws IOException {
		String matchedPatternFile = Configuration.ROOT_PATH + "Violations/" + violationType + "/patchSourceCode.list";
		String content = FileHelper.readFile(matchedPatternFile);
		BufferedReader reader = new BufferedReader(new StringReader(content));
		String line = null;
		String pattern = "";
		int index = 0;
		while ((line = reader.readLine()) != null) {
			if (line.equals(Configuration.PATCH_SIGNAL)) {
				if (fixPatternIndex ==index) {
					break;
				}
				index ++;
				pattern = "";
			}
			pattern += line + "\n";
		}
		
		reader.close();
		return pattern;
	}

	private String readFixPattern(String violationType, List<Integer> fixPatternIndexes) throws IOException {
		String matchedPatternFile = Configuration.ROOT_PATH + "Violations/" + violationType + "/patchSourceCode.list";
		String content = FileHelper.readFile(matchedPatternFile);
		BufferedReader reader = new BufferedReader(new StringReader(content));
		String line = null;
		String pattern = "";
		int index = 0;
		int counter = 0;
		String fixPatterns = "";
		while ((line = reader.readLine()) != null) {
			if (line.equals(Configuration.PATCH_SIGNAL)) {
				if (fixPatternIndexes.contains(index)) {
					fixPatterns += pattern + "\n";
					counter ++;
					if (counter == 10) break;
				}
				index ++;
				pattern = "";
			}
			pattern += line + "\n";
		}
		
		reader.close();
		return fixPatterns;
	}

	private Double computeSimilarity(Double[] feature, Double[] trainingFeature) {
		Double similarity = new DistanceCalculator().cosineSimilarityDistance(trainingFeature, feature);
		return similarity;
	}
	
	private List<Double[]> readFeatures(String fileName) throws IOException {
		List<Double[]> features = new ArrayList<>();
		String content = FileHelper.readFile(fileName);
		BufferedReader reader = new BufferedReader(new StringReader(content));
		String line = null;
		while ((line = reader.readLine()) != null) {
			if ("".equals(line)) {
				features.add(null);
			} else {
				Double[] feature = doubleParseFeature(line);
				features.add(feature);
			}
		}
		reader.close();
		return features;
	}

	private Double[] readCentroid(File centroidFile) {
		String content = FileHelper.readFile(centroidFile);
		Double[] centroid = doubleParseFeature(content);
		return centroid;
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
