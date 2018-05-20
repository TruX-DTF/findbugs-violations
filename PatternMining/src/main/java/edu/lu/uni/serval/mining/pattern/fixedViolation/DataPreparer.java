package edu.lu.uni.serval.mining.pattern.fixedViolation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
 * Prepare data for mining common patterns of fixed violations.
 * 
 * Pattern of fixed violation code.
 * 
 * @author kui.liu
 *
 */
public class DataPreparer {

	public void prepare() throws IOException {
		// Merger Data
		String filePath = Configuration.ROOT_PATH + "fixedViolations/";
		mergeData(filePath);
		int maxSize = 80;
		selectData(maxSize, filePath);
		selectSourceCode(filePath);
		
		selectDataTypeByType(filePath, maxSize);
	}

	private void selectDataTypeByType(String filePath, int maxSize) throws IOException {
		List<String> types = readTypes();
		
		String selectedData = filePath + "Types/";
		FileHelper.deleteDirectory(selectedData);
		List<Integer> sizes = readIntegers(filePath + "Sizes.list");
		List<String> tokens = DataPreparation.readStringList(filePath + "Tokens.list");
		
		FileInputStream fis = new FileInputStream(filePath + "SourceCode.list");
		Scanner scanner = new Scanner(fis);
		Map<String, StringBuilder> tokensMap = new HashMap<>();
		Map<String, StringBuilder> sourceCodeMap = new HashMap<>();
		StringBuilder selectedTokensBuilder = new StringBuilder();
		
		int index = -1;
		String singleViolation = "";
		boolean isViolationType = false;
		String violationType = "";
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.startsWith("##Source_Code:")) {
				isViolationType = true;
				if (index >= 0 && sizes.get(index) <= maxSize) {
					if (types.contains(violationType)) {
						if (tokensMap.containsKey(violationType)) {
							tokensMap.get(violationType).append(tokens.get(index)).append("\n");
							sourceCodeMap.get(violationType).append(singleViolation);//.append("\n");
						} else {
							StringBuilder feature = new StringBuilder();
							feature.append(tokens.get(index)).append("\n");
							tokensMap.put(violationType, feature);
							StringBuilder builder = new StringBuilder();
							builder.append(singleViolation);
							sourceCodeMap.put(violationType, builder);
						}
						selectedTokensBuilder.append(tokens.get(index)).append("\n");
					}
				}
				singleViolation = "";
				index ++;
			} else if (isViolationType) {
				violationType = line;
				isViolationType = false;
			}
			singleViolation += line + "\n";
		}
		
		if (types.contains(violationType)) {
			if (tokensMap.containsKey(violationType)) {
				tokensMap.get(violationType).append(tokens.get(index)).append("\n");
				sourceCodeMap.get(violationType).append(singleViolation);//.append("\n");
			} else {
				StringBuilder feature = new StringBuilder();
				feature.append(tokens.get(index)).append("\n");
				tokensMap.put(violationType, feature);
				StringBuilder builder = new StringBuilder();
				builder.append(singleViolation);
				sourceCodeMap.put(violationType, builder);
			}
		}
		
		scanner.close();
		fis.close();
		
		for (Map.Entry<String, StringBuilder> entry : tokensMap.entrySet()) {
			String type = entry.getKey();
			String path = Configuration.ROOT_PATH + "fixedViolations/Types/" + type;
			FileHelper.outputToFile(path + "/SourceCode.list", sourceCodeMap.get(type), false);
			FileHelper.outputToFile(path + "/Tokens.list", tokensMap.get(type), false);
		}
		FileHelper.outputToFile(Configuration.ROOT_PATH + "fixedViolations/selectedData/SelectedTokens.list", selectedTokensBuilder, false);
	}

	private List<String> readTypes() {
		String fileName = Configuration.ROOT_PATH + "fixedViolations/types.list";
		String content = FileHelper.readFile(fileName);
		List<String> types = new ArrayList<>();
		BufferedReader reader = new BufferedReader(new StringReader(content));
		try {
			String line = null;
			while ((line = reader.readLine()) != null) {
				types.add(line);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.err.println(types.size());
		return types;
	}

	public void mergeData(String filePath) {
		// Sub-directory: Sizes, SourceCode, and Tokens
		String allSourceCodeFile = filePath + "SourceCode.list";
		String allSizesFile = filePath + "Sizes.list";
		String allTokensFile = filePath + "Tokens.list";
		FileHelper.deleteFile(allSourceCodeFile);
		FileHelper.deleteFile(allSizesFile);
		FileHelper.deleteFile(allTokensFile);

		File sourceCodeFilesFolder = new File(filePath + "SourceCode/");
		File[] sourceCodeFiles = sourceCodeFilesFolder.listFiles();
		for (File sourceCodeFile : sourceCodeFiles) {
			String fileName = sourceCodeFile.getName();
			if (fileName.endsWith(".list")) {
				FileHelper.outputToFile(allSourceCodeFile, FileHelper.readFile(sourceCodeFile), true);
				FileHelper.outputToFile(allSizesFile, FileHelper.readFile(filePath + "Sizes/" + fileName), true);
				FileHelper.outputToFile(allTokensFile, FileHelper.readFile(filePath + "Tokens/" + fileName), true);
			}
		}
		FileHelper.deleteDirectory(filePath + "SourCode/");
		FileHelper.deleteDirectory(filePath + "Sizes/");
		FileHelper.deleteDirectory(filePath + "Tokens/");
	}

	public void selectData(int maxSize, String filePath) {
		String selectedData = filePath + "selectedData/";
		FileHelper.deleteDirectory(selectedData);
		
		List<Integer> sizes = readIntegers(filePath + "Sizes.list");
		selectData(maxSize, sizes, filePath + "Tokens.list", "SelectedTokens.list", selectedData);
	}

	private void selectData(int maxSize, List<Integer> sizes, String inputFile, String fileName, String outputPath) {
		FileInputStream fis = null;
		Scanner scanner = null;
		int index = -1;
		StringBuilder tokensBuilder = new StringBuilder();
		StringBuilder indexesBuilder = new StringBuilder();
		try {
			fis = new FileInputStream(inputFile);
			scanner = new Scanner(fis);
			
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				index ++;
				
				if (sizes.get(index) <= maxSize) {
					tokensBuilder.append(line + "\n");
					indexesBuilder.append(index + "\n");
					if (index % 10000 == 0) {
						FileHelper.outputToFile(outputPath + fileName, tokensBuilder, true);
						tokensBuilder.setLength(0);
					}
				}
			}
			FileHelper.outputToFile(outputPath + fileName, tokensBuilder, true);
			tokensBuilder.setLength(0);
			FileHelper.outputToFile(outputPath + "SourceCodeIndexes.list", indexesBuilder, false);
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
	}

	private List<Integer> readIntegers(String fileName) {
		List<Integer> sizes = new ArrayList<>();
		String content = FileHelper.readFile(fileName);
		BufferedReader reader = new BufferedReader(new StringReader(content));
		String line = null;
		
		try {
			while ((line = reader.readLine()) != null) {
				sizes.add(Integer.parseInt(line));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return sizes;
	}

	public void selectSourceCode(String filePath) throws IOException {
		List<Integer> sourceCodeIndexes = readIntegers(filePath + "selectedData/SourceCodeIndexes.list");
		readSourceCode(filePath, sourceCodeIndexes);
	}

	private void readSourceCode(String path, List<Integer> fixedAlarmIndexes) throws IOException {
		String file = path + "SourceCode.list";
		String outputFile = path + "selectedData/SelectedSourceCode.list";
		FileInputStream fis = new FileInputStream(file);
		Scanner scanner = new Scanner(fis);
		
		int lineNum = -1;
		String singleViolation = "";
		StringBuilder builder = new StringBuilder();
		int counter = 0;
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.startsWith("##Source_Code:")) {
				if (!"".equals(singleViolation)) {
					if (fixedAlarmIndexes.contains(lineNum)) {
						builder.append(singleViolation);
						counter ++;
						
						if (counter % 10000 == 0) {
							FileHelper.outputToFile(outputFile, builder, true);
							builder.setLength(0);
						}
					}
				}
				singleViolation = "";
			}
			singleViolation += line + "\n";
			lineNum ++;
		}
		FileHelper.outputToFile(outputFile, builder, true);
		builder.setLength(0);
		
		scanner.close();
		fis.close();
	}
}
