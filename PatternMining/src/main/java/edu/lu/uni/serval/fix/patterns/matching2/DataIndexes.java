package edu.lu.uni.serval.fix.patterns.matching2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.lu.uni.serval.config.Configuration;
import edu.lu.uni.serval.mining.utils.DataPreparation;
import edu.lu.uni.serval.utils.FileHelper;

public class DataIndexes {

	public void index() throws IOException {
		String violationMatching = Configuration.ROOT_PATH + "Violation_matching/";
		String violationIndexes = violationMatching + "fixedViolationsIndex.list";
		List<Integer> indexes = readIndexes(violationIndexes);
		String violationFeatureFile = violationMatching + "violationPatchFeatures.list";
		List<String> features = DataPreparation.readStringList(violationFeatureFile);
		
		String violationsFile = Configuration.SELECTED_ALARM_TYPES_FILE;
		List<String> violationTypes = DataPreparation.readStringList(violationsFile);
		
		Map<String, StringBuilder> buildersMap = new HashMap<>();
		for (int i = 0, size = violationTypes.size(); i < size; i ++) {
			String violationType = violationTypes.get(i);
			String feature = "";
			int index = indexes.indexOf(i);
			if (index >= 0) {
				feature = features.get(index) + "\n";
			} else {
				feature = "\n";
			}
			
			StringBuilder builder;
			if (buildersMap.containsKey(violationType)) {
				builder = buildersMap.get(violationType);
			} else {
				builder = new StringBuilder();
				buildersMap.put(violationType, builder);
			}
			
			builder.append(feature);
		}
		
		for (Map.Entry<String, StringBuilder> entry : buildersMap.entrySet()) {
			String key = entry.getKey();
			StringBuilder value = entry.getValue();
			FileHelper.outputToFile(Configuration.ROOT_PATH + "Violations/" + key + "/sourceCodeFeatures.list", value, false);
			value.setLength(0);
		}
		
	}

	private List<Integer> readIndexes(String fileName) throws IOException {
		List<Integer> indexes = new ArrayList<>();
		String content = FileHelper.readFile(fileName);
		BufferedReader reader = new BufferedReader(new StringReader(content));
		String line = null;
		while ((line = reader.readLine()) != null) {
			indexes.add(Integer.parseInt(line));
		}
		reader.close();
		return indexes;
	}

}
