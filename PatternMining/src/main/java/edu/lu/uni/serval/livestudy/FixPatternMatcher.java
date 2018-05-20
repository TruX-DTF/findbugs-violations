package edu.lu.uni.serval.livestudy;

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
		
		String fixedPatterns = Configuration.ROOT_PATH + "LiveStudy/FixPatterns/";
		File[] fixedTypeFolders = new File(fixedPatterns).listFiles();
		Map<String, List<Double[]>> fixedViolationFeatures = new HashMap<>(); // <Alarm Type, Features>
		for (File file : fixedTypeFolders) {
			if (file.isDirectory()) {
				String fileName = file.getPath() + "/sourceCodeFeatures.list";
				List<Double[]> features = readFeatures(fileName);
				fixedViolationFeatures.put(file.getName(), features);
			}
		}
		
		String unfixedViolationFeaturesFile = Configuration.ROOT_PATH + "LiveStudy/BugsInfo/LearnedFeatures.list";
		List<Violation> unfixedViolations = readVioltions(Configuration.ROOT_PATH + "LiveStudy/BugsInfo/selectedBugsInfo.list");
		System.out.println(unfixedViolations.size());
		List<Double[]> features = readFeatures(unfixedViolationFeaturesFile);
		System.out.println(features.size());
		
		Map<String, Integer> map = new HashMap<>();
		for (int i = 0, size = unfixedViolations.size(); i < size; i ++) {
			Violation violation = unfixedViolations.get(i);
			String violationType = violation.getType();
			Double[] feature = features.get(i);
			
			String projectName = violation.getProjectName();
			String key = projectName + "/" + violationType;
			if (map.containsKey(key)) {
				map.put(key, map.get(key) + 1);
			} else {
				map.put(key, 1);
			}
			
			String bugInfo = "###BugInstance###\nType: " + violation.getType() + "\nProject: "
					+ projectName + "\nFileName: " + violation.getFileName()
					+ "\nPosition: @@ " + violation.getStartLine() + ", " + violation.getEndLine() + " @@"
					+ violation.getSourceCode() + "\n Matched Fix Patterns:\n===================\n\n";
			
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
				FileHelper.outputToFile(Configuration.ROOT_PATH + "LiveStudy/BugsInfo/" + key + "/" + map.get(key) + "/InnerTypeFixPatterns.list", bugInfo + matchedFixPattern, false);
			} else {
				System.err.println(i + "::" + bugInfo);
			}
			
			// Compute similarity in the whole data set.
			String matchedFixPattern = computSimilarityWithAllData(feature, fixedViolationFeatures, violationType);
			FileHelper.outputToFile(Configuration.ROOT_PATH + "LiveStudy/BugsInfo/" + key + "/" + map.get(key) + "/CrossTypeFixPatterns.list", bugInfo + matchedFixPattern, false);
		}
	}

	private List<Violation> readVioltions(String bugsInfoFile) throws IOException {
		List<Violation> violations = new ArrayList<>();
		FileInputStream fis = new FileInputStream(bugsInfoFile);
		Scanner scanner = new Scanner(fis);
		
		String singleBug = "";
		String bugInfo = "";
		int count = 0;
		
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if ("###BugInstance###".equals(line)) {
				count ++;
				// Create a new Violation
				if (!"".equals(bugInfo)) {
					String[] elements = bugInfo.split(":");
					String type = elements[1];
					String projectName = elements[2];
					String fileName = elements[3];
					int startLine = Integer.parseInt(elements[4]);
					int endLine = Integer.parseInt(elements[5]);
					
					Violation v = new Violation(type, projectName, fileName, startLine, endLine, singleBug);
					violations.add(v);
					singleBug = "";
					bugInfo = "";
				}
			} else if (line.startsWith("##Info:")) {
				bugInfo = line;
			} else {
				singleBug += line + "\n";
			}
		}
		System.out.println(count);
		scanner.close();
		fis.close();
		
		if (!"".equals(bugInfo)) {
			String[] elements = bugInfo.split(":");
			String type = elements[1];
			String projectName = elements[2];
			String fileName = elements[3];
			int startLine = Integer.parseInt(elements[4]);
			int endLine = Integer.parseInt(elements[5]);
			
			Violation v = new Violation(type, projectName, fileName, startLine, endLine, singleBug);
			violations.add(v);
			singleBug = "";
			bugInfo = "";
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
		String matchedPatternFile = Configuration.ROOT_PATH + "LiveStudy/FixPatterns/" + alarmType + "/Patches.list";
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
		String matchedPatternFile = Configuration.ROOT_PATH + "LiveStudy/FixPatterns/" + alarmType + "/Patches.list";
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
	
	@SuppressWarnings("unused")
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
