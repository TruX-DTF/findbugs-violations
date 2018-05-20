package edu.lu.uni.serval.fix.patterns.matching2;

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
import edu.lu.uni.serval.mining.pattern.violation.Violation;
import edu.lu.uni.serval.mining.utils.DataPreparation;
import edu.lu.uni.serval.utils.FileHelper;

public class Feature {

	public void separateFeature() throws IOException {
		String inputData = Configuration.ROOT_PATH + "Alarm_matching/vectorizedData/1_CNNInput.csv";
		List<String> features = DataPreparation.readStringList(inputData);
		int size = features.size();
		int subSize = size - 65000;
		List<String> alarmPatchFeatures = features.subList(0, subSize);
		StringBuilder builder = new StringBuilder();
		for (String str : alarmPatchFeatures) {
			builder.append(str + "\n");
		}
		FileHelper.outputToFile(Configuration.ROOT_PATH + "Alarm_matching/alarmPatchFeatures.list", builder, false);
		builder.setLength(0);
		
		List<String> unfixedAlarmFeatures = features.subList(subSize, size);
		List<Integer> unfixedAlarmIndexes = readIndexes(Configuration.ROOT_PATH + "Alarms_tokens/selectedData/unfixedAlarmsTokensIndexes.csv");
		Map<String, List<Violation>> unfixedAlarms = readAlarms(unfixedAlarmIndexes, unfixedAlarmFeatures, 
				Configuration.ROOT_PATH + "Alarms_tokens/unfixedAlarms.list", Configuration.ROOT_PATH + "GumTreeInput/unfixAlarms/unfixed_");
		// output extracted features
		FileHelper.deleteDirectory(Configuration.ROOT_PATH + "Alarms_tokens/unfixedAlarms/");
		classifyDataByAlarmTypes(unfixedAlarms, Configuration.ROOT_PATH + "Alarms_tokens/unfixedAlarms/");

	}

	private void classifyDataByAlarmTypes(Map<String, List<Violation>> alarms, String outputPath) {
		for (Map.Entry<String, List<Violation>> entry : alarms.entrySet()) {
			String alarmType = entry.getKey();
			List<Violation> alarmsList = entry.getValue();
			StringBuilder alarmInfoBuilder = new StringBuilder();
			StringBuilder tokensBuilder = new StringBuilder();
			StringBuilder clusterInput = new StringBuilder();
			StringBuilder featuresBuilder = new StringBuilder();
			clusterInput.append("@relation Data_for_clustering\n\n");
			for (int i = 0; i < 300; i ++) {
				clusterInput.append("@attribute attribute_" + i + " numeric\n");
			}
			clusterInput.append("\n@data\n");
			for (Violation alarm : alarmsList) {
				alarmInfoBuilder.append("ALARM###\n");
				alarmInfoBuilder.append(alarm.getViolationType() + "\n");
				alarmInfoBuilder.append(alarm.getFileName() + " : " + alarm.getStartLine() + " : " + alarm.getEndLine() + "\n");
				alarmInfoBuilder.append(alarm.getSourceCode());
				tokensBuilder.append(alarm.getTokens() + "\n");
				clusterInput.append(alarm.getFeature().replaceAll(", ", ",") + "\n");
				featuresBuilder.append(alarm.getFeature() + "\n");
			}

			FileHelper.outputToFile(outputPath + alarmType + "/alarmsInfo.list", alarmInfoBuilder, false);
			FileHelper.outputToFile(outputPath + alarmType + "/alarmsTokens.list", tokensBuilder, false);
			FileHelper.outputToFile(outputPath + alarmType + "/alarmFeatures.list", featuresBuilder, false);
			FileHelper.outputToFile(outputPath + alarmType + "/clusterInput.arff", clusterInput, false);
		}
	}

	private Map<String, List<Violation>> readAlarms(List<Integer> indexes, List<String> features, String fileName, String filePath) throws NumberFormatException, IOException {
		
		Map<String, List<Violation>> alarmsMap = new HashMap<>();
		
		String content = FileHelper.readFile(fileName);
		BufferedReader reader = new BufferedReader(new StringReader(content));
		String line = null;
		int index = -1;
		int counter = 0;
		while ((line = reader.readLine()) != null) {
			index ++;
			
			if (indexes.contains(index)) {
				int colonIndex = line.indexOf(":");
				String alarmType = line.substring(0, colonIndex);
				line = line.substring(colonIndex + 1);
				colonIndex = line.indexOf(":");
				String file = line.substring(0, colonIndex).replace(".txt", ".java");
				line = line.substring(colonIndex + 1);
				colonIndex = line.indexOf(":");
				int startLine = Integer.parseInt(line.substring(0, colonIndex));
				line = line.substring(colonIndex + 1);
				colonIndex = line.indexOf(":");
				int endLine = Integer.parseInt(line.substring(0, colonIndex));
				String tokens = line.substring(colonIndex + 1);
				
				Violation alarm = new Violation(alarmType, file, tokens, startLine, endLine);
				
				String sourceCode = readSourceCode(filePath + file, startLine, endLine);
				String feature = features.get(counter);
				
				alarm.setSourceCode(sourceCode);
				alarm.setFeature(feature);
				
				
				if (alarmsMap.containsKey(alarmType)) {
					alarmsMap.get(alarmType).add(alarm);
				} else {
					List<Violation> alarms = new ArrayList<>();
					alarms.add(alarm);
					alarmsMap.put(alarmType, alarms);
				}
				counter ++;
			}
			
		}
		reader.close();
		return alarmsMap;
	}

	private String readSourceCode(String file, int startLine, int endLine) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		Scanner scanner = new Scanner(fis);
		
		String sourceCode = "";
		int lineNum = 0;
		while (scanner.hasNextLine()) {
			lineNum ++;
			String line = scanner.nextLine();
			if (startLine <= lineNum && lineNum <= endLine) {
				sourceCode += line + "\n";
			}
			if (lineNum == endLine) break;
		}
		
		scanner.close();
		fis.close();
		return sourceCode;
	}

	private List<Integer> readIndexes(String fileName) throws IOException {
		List<Integer> indexes = new ArrayList<>();
		String content = FileHelper.readFile(fileName);
		BufferedReader reader = new BufferedReader(new StringReader(content));
		String line = null;
		int counter = 0;
		while ((line = reader.readLine()) != null) {
			indexes.add(Integer.parseInt(line));
			
			if (++ counter == 65000) break;
		}
		reader.close();
		return indexes;
	}

}
