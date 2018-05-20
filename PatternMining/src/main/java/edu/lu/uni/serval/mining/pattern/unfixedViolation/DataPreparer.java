package edu.lu.uni.serval.mining.pattern.unfixedViolation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import edu.lu.uni.serval.config.Configuration;
import edu.lu.uni.serval.utils.FileHelper;

/**
 * Prepare data for mining common patterns of unfixed violations.
 * 
 * @author kui.liu
 *
 */
public class DataPreparer {

	public void prepare() {
//		String content = FileHelper.readFile("../FPM_Violations/AllSizes.csv");
//		BufferedReader reader = new BufferedReader(new StringReader(content));
//		String line = null;
//		Map<Integer,Integer> sizes = new HashMap<>();
//		while((line = reader.readLine()) != null) {
//			int num = Integer.parseInt(line);
//			if (sizes.containsKey(num)) {
//				sizes.put(num, sizes.get(num) + 1);
//			} else {
//				sizes.put(num, 1);
//			}
//		}
//		reader.close();
//		MapSorter<Integer, Integer> sorter = new MapSorter<>();
//		sizes = sorter.sortByKeyAscending(sizes);
//		for (Map.Entry<Integer, Integer> entry : sizes.entrySet()) {
//			System.out.println(entry.getKey() + "===" + entry.getValue());
//		}
		// Configuration.ROOT_PATH + "UnfixedViolations/" + #violationType# + "/" + (SourceCode.list/Sizes.list/Tokens.list) 
		// Configuration.ROOT_PATH + "UnfixedViolations/" + #violationType# + "/selectedData/SelectedTokens.list"
		// Configuration.ROOT_PATH + "UnfixedViolations/" + #violationType# + "/selectedData/SourceCodeIndexes.list"
		// Configuration.ROOT_PATH + "UnfixedViolations/" + #violationType# + "/selectedData/SelectedSourceCode.list"
		File violationTypesFile = new File(Configuration.ROOT_PATH + "UnfixedViolations/");
		File[] violationTypes = violationTypesFile.listFiles();
		
		String allSizes = Configuration.ROOT_PATH + "UnfixedViolations/AllSizes.csv";
		FileHelper.deleteFile(allSizes);
		for (File violationType : violationTypes) {
			if (violationType.isDirectory()) {
				String filePath = violationType.getPath() + "/";
	//			mergeData(filePath);
	//			FileHelper.outputToFile(allSizes, mergeData(filePath), true);
				
				int maxSize = 80; // The size threshold of token vectors. TODO
				selectedData(maxSize, filePath);
				try {
					selectedSourceCode(filePath);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public StringBuilder mergeData(String filePath) {
		StringBuilder sizesBuilder = new StringBuilder();
		// Sub-directory: Sizes, SourceCode, and Tokens
		String allSourceCodeFile = filePath + "SourceCode.list";
		String allSizesFile = filePath + "Sizes.list";
		String allTokensFile = filePath + "Tokens.list";
//		FileHelper.deleteFile(allSourceCodeFile);
//		FileHelper.deleteFile(allSizesFile);
//		FileHelper.deleteFile(allTokensFile);

		File sourceCodeFilesFolder = new File(filePath + "SourceCode/");
		if (!sourceCodeFilesFolder.exists()) return sizesBuilder;
		System.out.println(sourceCodeFilesFolder);
		File[] sourceCodeFiles = sourceCodeFilesFolder.listFiles();
		for (File sourceCodeFile : sourceCodeFiles) {
			String fileName = sourceCodeFile.getName();
			if (fileName.endsWith(".list")) {
				String sizes = FileHelper.readFile(filePath + "Sizes/" + fileName);
				sizesBuilder.append(sizes);
				FileHelper.outputToFile(allSourceCodeFile, FileHelper.readFile(sourceCodeFile), true);
				FileHelper.outputToFile(allSizesFile, sizes, true);
				FileHelper.outputToFile(allTokensFile, FileHelper.readFile(filePath + "Tokens/" + fileName), true);
			}
		}
		FileHelper.deleteDirectory(filePath + "SourCode/");
		FileHelper.deleteDirectory(filePath + "Sizes/");
		FileHelper.deleteDirectory(filePath + "Tokens/");
		return sizesBuilder;
	}

	public void selectedData(int maxSize, String filePath) {
		String selectedData = filePath + "selectedData/";
		FileHelper.deleteDirectory(selectedData);
		
		List<Integer> sizes = readIntegers(filePath + "Sizes.list");
		selectedData(maxSize, sizes, filePath + "Tokens.list", "SelectedTokens.list", selectedData);
	}

	private void selectedData(int maxSize, List<Integer> sizes, String inputFile, String fileName, String outputPath) {
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

	public void selectedSourceCode(String filePath) throws IOException {
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
