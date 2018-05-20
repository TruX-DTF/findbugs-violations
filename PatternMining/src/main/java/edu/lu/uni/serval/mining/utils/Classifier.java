package edu.lu.uni.serval.mining.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import edu.lu.uni.serval.config.Configuration;
import edu.lu.uni.serval.utils.Exporter;
import edu.lu.uni.serval.utils.FileHelper;
import edu.lu.uni.serval.utils.MapSorter;

/**
 * Classify violations by alarm types.
 * 
 * @author kui.liu
 *
 */
public class Classifier {
	
	private Map<String, Integer> coutersMap = new HashMap<>();
	
	/**
	 * Data: initial data before deep learning.
	 */
	public void classifyDataByAlarmTypes() {
		
		String editScriptsFile = Configuration.EDITSCRIPTS_FILE;
		String patchesSourceCodeFile = Configuration.PATCH_SOURCECODE_FILE;
		String buggyTokensFile = Configuration.BUGGY_CODE_TOKENS_FILE;
		String editScriptSizesFile = Configuration.EDITSCRIPT_SIZES_FILE;
		String alamsFile = Configuration.ALARM_TYPES_FILE;

		String parentPath = Configuration.ALARMS;
		FileHelper.deleteDirectory(parentPath);
		
		List<String> alarmTypes = readAlarmTypes(alamsFile);
		classifyEditScripts(alarmTypes, editScriptsFile, parentPath);
		classifySourceCodeTokenss(alarmTypes, buggyTokensFile, parentPath);
		classifyEditScriptsSizes(alarmTypes, editScriptSizesFile, parentPath);
		classifyPatches(alarmTypes, patchesSourceCodeFile, parentPath);
	}
		
	/**
	 * Data: learned features.
	 */
	public void classifyDataByAlarmTypes2(String fileName) {
		
		String extractedFeatures = Configuration.EXTRACTED_FEATURES + fileName;
		String patchesSourceCodeFile = Configuration.SELECTED_PATCHES_SOURE_CODE_FILE;
		String buggyTokensFile = Configuration.SELECTED_BUGGY_TOKEN_FILE;
		String alamsFile = Configuration.SELECTED_ALARM_TYPES_FILE;

		String parentPath = Configuration.ALARMS;
		FileHelper.deleteDirectory(parentPath);
		
		List<String> alarmTypes = readAlarmTypes(alamsFile);
		classifyEditScripts(alarmTypes, extractedFeatures, parentPath);
		classifySourceCodeTokenss(alarmTypes, buggyTokensFile, parentPath);
		classifyPatches(alarmTypes, patchesSourceCodeFile, parentPath);
	}
	
	/**
	 * Data: learned features.
	 */
	public void classifyDataByAlarmTypes3(String fileName) {
		
		@SuppressWarnings("unused")
		String extractedFeatures = Configuration.EXTRACTED_FEATURES + fileName;
		String patchesSourceCodeFile = Configuration.SELECTED_PATCHES_SOURE_CODE_FILE;
		String alamsFile = Configuration.SELECTED_ALARM_TYPES_FILE;

		String parentPath = Configuration.ALARMS;
		FileHelper.deleteDirectory(parentPath);
		
		List<String> alarmTypes = readAlarmTypes(alamsFile);
		classifyPatches(alarmTypes, patchesSourceCodeFile, parentPath);
	}
	
	private void classifyEditScripts(List<String> alarmTypes, String dataFile, String parentPath) {
		String fileName = FileHelper.getFileName(dataFile);
		List<String> data = DataPreparation.readStringList(dataFile);
		Map<String, StringBuilder> buildersMap = new HashMap<>();
		for (int i = 0, size = data.size(); i < size; i ++) {
			String alarmType = alarmTypes.get(i);
			StringBuilder builder;
			if (buildersMap.containsKey(alarmType)) {
				builder = buildersMap.get(alarmType);
			} else {
				builder = new StringBuilder();
				buildersMap.put(alarmType, builder);
			}
			builder.append(data.get(i) + "\n");
			
			if (i % 10000 == 0) {
				for (Map.Entry<String, StringBuilder> entry : buildersMap.entrySet()) {
					String key = entry.getKey();
					StringBuilder value = entry.getValue();
					FileHelper.outputToFile(parentPath + key + "/" + fileName, value, true);
					value.setLength(0);
					buildersMap.put(key, value);
				}
			}
		}
		
		for (Map.Entry<String, StringBuilder> entry : buildersMap.entrySet()) {
			String key = entry.getKey();
			StringBuilder value = entry.getValue();
			FileHelper.outputToFile(parentPath + key + "/" + fileName, value, true);
			value.setLength(0);
		}
	}
	
	private void classifySourceCodeTokenss(List<String> alarmTypes, String dataFile, String parentPath) {
		String fileName = FileHelper.getFileName(dataFile);
		List<String> data = DataPreparation.readStringList(dataFile);
		Map<String, StringBuilder> buildersMap = new HashMap<>();
		Map<String, Integer> maxTokenVectorSizes = new HashMap<>();
		for (int i = 0, size = data.size(); i < size; i ++) {
			String alarmType = alarmTypes.get(i);
			StringBuilder builder;
			if (buildersMap.containsKey(alarmType)) {
				builder = buildersMap.get(alarmType);
			} else {
				builder = new StringBuilder();
				buildersMap.put(alarmType, builder);
			}
			String singleLine = data.get(i);
			builder.append(singleLine + "\n");
			String[] tokens = singleLine.split(" ");
			addToMap(maxTokenVectorSizes, alarmType, tokens.length);
			
			if (i % 10000 == 0) {
				for (Map.Entry<String, StringBuilder> entry : buildersMap.entrySet()) {
					String key = entry.getKey();
					StringBuilder value = entry.getValue();
					FileHelper.outputToFile(parentPath + key + "/" + fileName, value, true);
					value.setLength(0);
					buildersMap.put(key, value);
				}
			}
		}
		
		for (Map.Entry<String, StringBuilder> entry : buildersMap.entrySet()) {
			String key = entry.getKey();
			StringBuilder value = entry.getValue();
			FileHelper.outputToFile(parentPath + key + "/" + fileName, value, true);
			value.setLength(0);
			buildersMap.put(key, value);
			
			FileHelper.outputToFile(parentPath + key + "/MaxTokenVectorSizeOfBuggySourceCode.list", maxTokenVectorSizes.get(key) + "", false);
		}
		
	}
	
	private void addToMap(Map<String, Integer> map, String alarmType, int size) {
		if (map.containsKey(alarmType)) {
			int oldSize = map.get(alarmType);
			if (size > oldSize) {
				map.put(alarmType, size);
			}
		} else {
			map.put(alarmType, size);
		}
	}

	private void classifyEditScriptsSizes(List<String> alarmTypes, String dataFile, String parentPath) {
		String fileName = FileHelper.getFileName(dataFile);
		List<String> data = DataPreparation.readStringList(dataFile);
		Map<String, StringBuilder> buildersMap = new HashMap<>();
		Map<String, Integer> maxTokenVectorSizes = new HashMap<>();
		for (int i = 0, size = data.size(); i < size; i ++) {
			String alarmType = alarmTypes.get(i);
			StringBuilder builder;
			if (buildersMap.containsKey(alarmType)) {
				builder = buildersMap.get(alarmType);
			} else {
				builder = new StringBuilder();
				buildersMap.put(alarmType, builder);
			}
			builder.append(data.get(i) + "\n");
			addToMap(maxTokenVectorSizes, alarmType, Integer.parseInt(data.get(i)));
			
			if (i % 10000 == 0) {
				for (Map.Entry<String, StringBuilder> entry : buildersMap.entrySet()) {
					String key = entry.getKey();
					StringBuilder value = entry.getValue();
					FileHelper.outputToFile(parentPath + key + "/" + fileName, value, true);
					value.setLength(0);
					buildersMap.put(key, value);
				}
			}
		}
		
		for (Map.Entry<String, StringBuilder> entry : buildersMap.entrySet()) {
			String key = entry.getKey();
			StringBuilder value = entry.getValue();
			FileHelper.outputToFile(parentPath + key + "/" + fileName, value, true);
			value.setLength(0);
			buildersMap.put(key, value);
			
			FileHelper.outputToFile(parentPath + key + "/MaxTokenVectorSizeOfEditScripts.list", maxTokenVectorSizes.get(key) + "", false);
		}
	}
	
	private void classifyPatches(List<String> alarmTypes, String dataFile, String parentPath) {
		String fileName = FileHelper.getFileName(dataFile);
		FileInputStream fis = null;
		Scanner scanner = null;
		Map<String, StringBuilder> buildersMap = new HashMap<>();
		try {
			fis = new FileInputStream(dataFile);
			scanner = new Scanner(fis);
			int counter = -1;
			String singleEntity = "";
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (line.equals(Configuration.PATCH_SIGNAL)) {
					if (!"".equals(singleEntity)) {
						String alarmType = alarmTypes.get(counter);
						StringBuilder builder;
						if (buildersMap.containsKey(alarmType)) {
							builder = buildersMap.get(alarmType);
						} else {
							builder = new StringBuilder();
							buildersMap.put(alarmType, builder);
						}
						builder.append(singleEntity);
						
						if (coutersMap.containsKey(alarmType)) {
							coutersMap.put(alarmType, coutersMap.get(alarmType) + 1);
						} else {
							coutersMap.put(alarmType, 1);
						}
						
						singleEntity = "";
					}
					counter ++;
					
					if (counter % 10000 == 0) {
						for (Map.Entry<String, StringBuilder> entry : buildersMap.entrySet()) {
							String key = entry.getKey();
							StringBuilder value = entry.getValue();
							FileHelper.outputToFile(parentPath + key + "/" + fileName, value, true);
							value.setLength(0);
							buildersMap.put(key, value);
						}
					}
				}
				singleEntity += line + "\n";
			}
			
			for (Map.Entry<String, StringBuilder> entry : buildersMap.entrySet()) {
				String key = entry.getKey();
				StringBuilder value = entry.getValue();
				FileHelper.outputToFile(parentPath + key + "/" + fileName, value, true);
				value.setLength(0);
				buildersMap.put(key, value);
			}
			
			String alarmType = alarmTypes.get(counter);
			if (coutersMap.containsKey(alarmType)) {
				coutersMap.put(alarmType, coutersMap.get(alarmType) + 1);
			} else {
				coutersMap.put(alarmType, 1);
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
		

		MapSorter<String, Integer> sorter = new MapSorter<String, Integer>();
		coutersMap = sorter.sortByKeyAscending(coutersMap);
		String[] columns = { "Alarm Type", "amount" };
		Exporter.exportOutliers(coutersMap, new File(parentPath + "AlarmTypes.xls"), 1, columns);
		String s = "";
		for (Map.Entry<String, Integer> entry : coutersMap.entrySet()) {
			s += entry.getKey() + " : " + entry.getValue() + "\n";

		}
		FileHelper.outputToFile(parentPath + "AlarmTypesAmount.list", s, false);
	}
	
	
	private List<String> readAlarmTypes(String alarmsFile) {
		List<String> alarmTypes = new ArrayList<>();
		
		FileInputStream fis = null;
		Scanner scanner = null;
		try {
			fis = new FileInputStream(alarmsFile);
			scanner = new Scanner(fis);
			while (scanner.hasNextLine()) {
				alarmTypes.add(scanner.nextLine());
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
		return alarmTypes;
	}
}
