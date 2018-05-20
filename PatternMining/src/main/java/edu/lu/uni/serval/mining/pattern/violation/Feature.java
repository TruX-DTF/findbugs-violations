package edu.lu.uni.serval.mining.pattern.violation;

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
 * Separate learned features.
 * 
 * @author kui.liu
 *
 */
public class Feature {

	public void classifyFeatureData() throws IOException {
		FileHelper.deleteDirectory(Configuration.ROOT_PATH + "Violations_tokens/fixedViolations/");
		FileHelper.deleteDirectory(Configuration.ROOT_PATH + "Violations_tokens/unfixedViolations/");
		String inputData = Configuration.ROOT_PATH + "Violations_tokens/vectorizedData/1_CNNInput.csv";
		List<String> features = DataPreparation.readStringList(inputData);
		List<String> fixedViolationFeatures = features.subList(0, 65000);
		List<String> unfixedViolationFeatures = features.subList(65000, 130000);
		
		List<Integer> fixedViolationIndexes = readIndexes(Configuration.ROOT_PATH + "Violations_tokens/selectedData/fixedViolationsTokensIndexes.csv");
		List<Integer> unfixedViolationIndexes = readIndexes(Configuration.ROOT_PATH + "Violations_tokens/selectedData/unfixedViolationsTokensIndexes.csv");
		
		Map<String, List<Violation>> fixedViolations = readViolations(fixedViolationIndexes, fixedViolationFeatures, 
				Configuration.ROOT_PATH + "Violations_tokens/fixedViolations.list", Configuration.ROOT_PATH + "GumTreeInput/prevFiles/prev_");
		Map<String, List<Violation>> unfixedViolations = readViolations(unfixedViolationIndexes, unfixedViolationFeatures, 
				Configuration.ROOT_PATH + "Violations_tokens/unfixedViolations.list", Configuration.ROOT_PATH + "GumTreeInput/unfixViolations/unfixed_");
		
		// output violation types
		classifyDataByViolationTypes(fixedViolations, Configuration.ROOT_PATH + "Violations_tokens/fixedViolations/");
		classifyDataByViolationTypes(unfixedViolations, Configuration.ROOT_PATH + "Violations_tokens/unfixedViolations/");

	}

	private void classifyDataByViolationTypes(Map<String, List<Violation>> violations, String outputPath) {
		for (Map.Entry<String, List<Violation>> entry : violations.entrySet()) {
			String violationType = entry.getKey();
			List<Violation> violationsList = entry.getValue();
			StringBuilder violationInfoBuilder = new StringBuilder();
			StringBuilder tokensBuilder = new StringBuilder();
			StringBuilder clusterInput = new StringBuilder();
			clusterInput.append("@relation Data_for_clustering\n\n");
			for (int i = 0; i < 300; i ++) {
				clusterInput.append("@attribute attribute_" + i + " numeric\n");
			}
			clusterInput.append("\n@data\n");
			for (Violation violation : violationsList) {
				violationInfoBuilder.append("ALARM###\n");
				violationInfoBuilder.append(violation.getViolationType() + "\n");
				violationInfoBuilder.append(violation.getSourceCode());
				tokensBuilder.append(violation.getTokens() + "\n");
				clusterInput.append(violation.getFeature().replaceAll(", ", ",") + "\n");
			}

			FileHelper.outputToFile(outputPath + violationType + "/violationsInfo.list", violationInfoBuilder, false);
			FileHelper.outputToFile(outputPath + violationType + "/violationsTokens.list", tokensBuilder, false);
			FileHelper.outputToFile(outputPath + violationType + "/clusterInput.arff", clusterInput, false);
		}
	}

	private Map<String, List<Violation>> readViolations(List<Integer> indexes, List<String> features, String fileName, String filePath) throws NumberFormatException, IOException {
		
		Map<String, List<Violation>> violationsMap = new HashMap<>();
		
		String content = FileHelper.readFile(fileName);
		BufferedReader reader = new BufferedReader(new StringReader(content));
		String line = null;
		int index = -1;
		int counter = 0;
		while ((line = reader.readLine()) != null) {
			index ++;
			
			if (indexes.contains(index)) {
				int colonIndex = line.indexOf(":");
				String violationType = line.substring(0, colonIndex);
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
				
				Violation violation = new Violation(violationType, file, tokens, startLine, endLine);
				
				String sourceCode = readSourceCode(filePath + file, startLine, endLine);
				String feature = features.get(counter);
				
				violation.setSourceCode(sourceCode);
				violation.setFeature(feature);
				
				
				if (violationsMap.containsKey(violationType)) {
					violationsMap.get(violationType).add(violation);
				} else {
					List<Violation> violations = new ArrayList<>();
					violations.add(violation);
					violationsMap.put(violationType, violations);
				}
				counter ++;
			}
			
		}
		reader.close();
		return violationsMap;
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
