package edu.lu.uni.serval.livestudy;

import java.io.BufferedReader;
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
	
	/**
	 * Assign learned features to fixed violations and unfixed violations.
	 * @throws IOException
	 */
	public void assign() throws IOException {
		String inputData = Configuration.ROOT_PATH + "LiveStudy/FixPatterns/1_CNNoutput.csv";
		List<String> features = DataPreparation.readStringList(inputData);
		int size = features.size();
		int subSize = size - 4422; // TODO
		List<String> fixPatternFeatures = features.subList(0, subSize);
		System.out.println(subSize + " : " + fixPatternFeatures.size());
		StringBuilder builder = new StringBuilder();
		for (String str : fixPatternFeatures) {
			builder.append(str + "\n");
		}
		FileHelper.outputToFile(Configuration.ROOT_PATH + "LiveStudy/FixPatterns/fixPatternFeatures.list", builder, false);
		builder.setLength(0);
		
		List<String> unfixedViolationFeatures = features.subList(subSize, size);//Configuration.ROOT_PATH + "LiveStudy/BugsInfo/selectedBugsInfo.list"
		System.out.println(unfixedViolationFeatures.size());
		for (String str : unfixedViolationFeatures) {
			builder.append(str + "\n");
		}
		FileHelper.outputToFile(Configuration.ROOT_PATH + "LiveStudy/BugsInfo/LearnedFeatures.list", builder, false);
		builder.setLength(0);
		
		separateFeature();
	}

	public void separateFeature() throws IOException {
		String fixedViolationIndexes = Configuration.ROOT_PATH + "LiveStudy/FixPatterns/fixedViolationsIndex.list";
		List<Integer> indexes = readIndexes(fixedViolationIndexes);
		String fixedViolationFeatureFile = Configuration.ROOT_PATH + "LiveStudy/FixPatterns/fixPatternFeatures.list";
		List<String> features = DataPreparation.readStringList(fixedViolationFeatureFile);
		
		// Read the information of selected fixed violations.
		String typesFile = Configuration.SELECTED_ALARM_TYPES_FILE;
		List<String> violationTypes = readTypes(typesFile, indexes);
		
		Map<String, StringBuilder> buildersMap = new HashMap<>();
		for (int i = 0, size = violationTypes.size(); i < size; i ++) {
			String type = violationTypes.get(i);
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
			FileHelper.outputToFile(Configuration.ROOT_PATH + "LiveStudy/FixPatterns/" + key + "/sourceCodeFeatures.list", value, false);
			value.setLength(0);
		}
		

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
						String type = violationTypes.get(positionIndex);
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
				String type = violationTypes.get(positionIndex);
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
			FileHelper.outputToFile(Configuration.ROOT_PATH + "LiveStudy/FixPatterns/" + key + "/Patches.list", value, false);
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
