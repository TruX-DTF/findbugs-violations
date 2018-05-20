package edu.lu.uni.serval.defects4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import edu.lu.uni.serval.config.Configuration;
import edu.lu.uni.serval.livestudy.Violation;
import edu.lu.uni.serval.utils.DistanceCalculator;
import edu.lu.uni.serval.utils.FileHelper;
import edu.lu.uni.serval.utils.MapSorter;

/**
 * Match top 10 most similar fix patterns for each violation from inner-type and cross-type respectively.
 * 
 * @author kui.liu
 *
 */
public class FixPatternMatcher {

	public void match() throws IOException {
		
		String fixedPatterns = Configuration.ROOT_PATH + "RQ3_2/FixPatterns/";
		File[] fixedTypeFolders = new File(fixedPatterns).listFiles();
		Map<String, List<Double[]>> fixedViolationFeatures = new HashMap<>(); // <Alarm Type, Features>
		int a = 0;
		for (File file : fixedTypeFolders) {
			if (file.isDirectory()) {
				String fileName = file.getPath() + "/sourceCodeFeatures.list";
				List<Double[]> features = readFeatures(fileName);
				a += features.size();
				fixedViolationFeatures.put(file.getName(), features);
			}
		}
		System.out.println("Fixed Features: " + a);
		
		String bugFeaturesFile = Configuration.ROOT_PATH + "RQ3_2/BugsInfo/LearnedFeatures.list";
		List<Violation> bugs = readVioltions(Configuration.ROOT_PATH + "RQ3_2/BugsInfo/buggySourceCode.list");
		System.out.println("Bugs: " + bugs.size());
		List<Double[]> features = readFeatures(bugFeaturesFile);
		System.out.println("Features: " + features.size());
		
		for (int i = 0, size = bugs.size(); i < size; i ++) {
			Violation violation = bugs.get(i);
			String violationType = violation.getType();
			Double[] feature = features.get(i);
			
			// Compute similarity in the same alarm type.
			if (fixedViolationFeatures.containsKey(violationType)) {
				List<Double[]> matchedFixedViolationFeatures = fixedViolationFeatures.get(violationType);
				Map<Integer, Double> similarities = new HashMap<>();
				for (int j = 0, size2 = matchedFixedViolationFeatures.size(); j < size2; j ++) {
					Double[] fixedViolationFeature = matchedFixedViolationFeatures.get(j);
					if (fixedViolationFeature == null) {
						similarities.put(j, 0d);
					} else {
						Double similarity = Math.abs(computeSimilarity(feature, fixedViolationFeature));
						similarities.put(j, similarity);
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
				FileHelper.outputToFile(Configuration.ROOT_PATH + "RQ3_2/BugsInfo/" + i + "/InnerTypeFixPatterns.list", matchedFixPattern, false);
			} else {
				System.err.println(i);
			}
			
			// Compute similarity in the whole data set.
			String matchedFixPattern = computSimilarityWithAllData(feature, fixedViolationFeatures, violationType);
			FileHelper.outputToFile(Configuration.ROOT_PATH + "RQ3_2/BugsInfo/" + i + "/CrossTypeFixPatterns.list", matchedFixPattern, false);
		}
	}

	private List<Violation> readVioltions(String bugsInfoFile) throws IOException {
		List<Violation> violations = new ArrayList<>();
		FileInputStream fis = new FileInputStream(bugsInfoFile);
		Scanner scanner = new Scanner(fis);
		
		String singleBug = "";
		String type = "";
		boolean isType = false;
		
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if ("##Source_Code:".equals(line)) {
				isType = true;
				// Create a new Violation
				if (!"".equals(singleBug)) {
					Violation v = new Violation(type, "", "", 0, 0, singleBug);
					violations.add(v);
					singleBug = "";
				}
			} else if (isType) {
				type = line;
				isType = false;
			}
			singleBug += line + "\n";
		}
		scanner.close();
		fis.close();
		
		if (!"".equals(singleBug)) {
			Violation v = new Violation(type, "", "", 0, 0, singleBug);
			violations.add(v);
			singleBug = "";
		}
		
		return violations;
	}

	/**
	 * Compute the similarities with all fixed alarm patterns.
	 * 
	 * @param centroid
	 * @param fixedAlarmFeatures
	 * @return
	 * @throws IOException 
	 * @throws NumberFormatException 
	 */
	private String computSimilarityWithAllData(Double[] centroid,
			Map<String, List<Double[]>> fixedAlarmFeatures, String currentAlarmType) throws NumberFormatException, IOException {
		Map<String, Double> similarities = new HashMap<>();
		
		for (Map.Entry<String, List<Double[]>> entry : fixedAlarmFeatures.entrySet()) {
			String alarmType = entry.getKey();
			if (currentAlarmType.equals(alarmType)) continue;
			List<Double[]> features = entry.getValue();
			Map<Integer, Double> similarity = computeSimilarityInOneAlarmType(centroid, features);
			for (Map.Entry<Integer, Double> ent : similarity.entrySet()) {
				alarmType += "@@" + ent.getKey();
				similarities.put(alarmType, ent.getValue());
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

	private Map<Integer, Double> computeSimilarityInOneAlarmType(Double[] centroid, List<Double[]> features) {
		Map<Integer, Double> similarities = new HashMap<>();
		for (int i = 0, size = features.size(); i < size; i ++) {
			Double[] fixedAlarmFeature = features.get(i);
			if (fixedAlarmFeature == null) {
				similarities.put(i + 1, 0d);
			} else {
				Double similarity = Math.abs(computeSimilarity(centroid, fixedAlarmFeature));
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

	private String readFixPattern(String alarmType, int fixPatternIndex) throws IOException {
		String matchedPatternFile = Configuration.ROOT_PATH + "RQ3_2/FixPatterns/" + alarmType + "/Patches.list";
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

	private String readFixPattern(String alarmType, List<Integer> fixPatternIndexes) throws IOException {
		String matchedPatternFile = Configuration.ROOT_PATH + "RQ3_2/FixPatterns/" + alarmType + "/Patches.list";
		String content = FileHelper.readFile(matchedPatternFile);
		BufferedReader reader = new BufferedReader(new StringReader(content));
		String line = null;
		String pattern = "";
		int index = -1;
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
