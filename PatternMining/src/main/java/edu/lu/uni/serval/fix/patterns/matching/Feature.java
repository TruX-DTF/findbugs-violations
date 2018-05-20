package edu.lu.uni.serval.fix.patterns.matching;

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
import edu.lu.uni.serval.mining.utils.DataPreparation;
import edu.lu.uni.serval.utils.FileHelper;

/**
 * Separate learned features of fixed violations by violation types.
 * 
 * @author kui.liu
 *
 */
public class Feature {

	public void separateFeatures() throws IOException {
		String fixedViolationIndexes = Configuration.ROOT_PATH + "RQ3/FixPatterns/fixedViolationsIndex.list";
		List<Integer> indexes = readIndexes(fixedViolationIndexes);
		// Read the information of selected fixed violations.
		String typesFile = Configuration.SELECTED_ALARM_TYPES_FILE;
		List<String> fixedViolationTypes = readTypes(typesFile, indexes);
				
		String outputTypePath = Configuration.ROOT_PATH + "RQ3/UnfixedViolations/";
		File[] violationTypes = new File(outputTypePath).listFiles();
		for (File file : violationTypes) {
			if (file.isDirectory()) {
				String filePath = file.getPath();
				if (!filePath.endsWith("SE_NO_SERIALVERSIONID")) continue;
				String fixedViolationFeatureFile = filePath + "/FixPatterns/PatternFeatures.list";
				List<String> features = DataPreparation.readStringList(fixedViolationFeatureFile);
				
				Map<String, StringBuilder> buildersMap = new HashMap<>();
				for (int i = 0, size = fixedViolationTypes.size(); i < size; i ++) {
					String type = fixedViolationTypes.get(i);
					String feature = "";
					int index = indexes.indexOf(i);
					if (index >= 0) {
						feature = features.get(index) + "\n";
					} else {
						feature = "\n";
					}
					
					StringBuilder builder;
					if (buildersMap.containsKey(type)) {
						builder = buildersMap.get(type);
					} else {
						builder = new StringBuilder();
						buildersMap.put(type, builder);
					}
					
					builder.append(feature);
				}
				
				for (Map.Entry<String, StringBuilder> entry : buildersMap.entrySet()) {
					String key = entry.getKey();
					StringBuilder value = entry.getValue();
					FileHelper.outputToFile(filePath + "/FixPatterns/" + key + "/PatternFeatures.list", value, false);
					value.setLength(0);
				}
			}
		}
		
		Map<String, StringBuilder> buildersMap = new HashMap<>();
		

		String sourceCodeFile = Configuration.SELECTED_PATCHES_SOURE_CODE_FILE;
		FileInputStream fis = new FileInputStream(sourceCodeFile);
		Scanner scanner = new Scanner(fis);
		
		int index = -1;
		StringBuilder singleFix = new StringBuilder();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if ("PATCH###".equals(line)) {
				if (singleFix.length() > 0) {
					int positionIndex = indexes.indexOf(index);
					if (positionIndex != -1) {
						String type = fixedViolationTypes.get(positionIndex);
						StringBuilder builder;
						if (buildersMap.containsKey(type)) {
							builder = buildersMap.get(type);
						} else {
							builder = new StringBuilder();
							buildersMap.put(type, builder);
						}
						builder.append(singleFix);
					}
				}
				index ++;
				singleFix.setLength(0);
			}
			singleFix.append(line).append("\n");
		}
		if (singleFix.length() > 0) {
			int positionIndex = indexes.indexOf(index);
			if (positionIndex != -1) {
				String type = fixedViolationTypes.get(positionIndex);
				StringBuilder builder;
				if (buildersMap.containsKey(type)) {
					builder = buildersMap.get(type);
				} else {
					builder = new StringBuilder();
					buildersMap.put(type, builder);
				}
				builder.append(singleFix);
			}
		}
		scanner.close();
		fis.close();
		
		for (Map.Entry<String, StringBuilder> entry : buildersMap.entrySet()) {
			String key = entry.getKey();
			StringBuilder value = entry.getValue();
			FileHelper.outputToFile(Configuration.ROOT_PATH + "RQ3/FixPatterns/" + key + "/Patches.list", value, false);
			value.setLength(0);
		}
	}

	
	private List<String> readTypes(String typesFile, List<Integer> indexes) throws IOException {
		FileInputStream fis = new FileInputStream(typesFile);
		Scanner scanner = new Scanner(fis);
		
		List<String> types = new ArrayList<>();
		int index = -1;
		while (scanner.hasNextLine()) {
			String type = scanner.nextLine();
			index ++;
			if (indexes.contains(index)) {
				types.add(type);
			}
		}
		
		scanner.close();
		fis.close();
		return types;
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
