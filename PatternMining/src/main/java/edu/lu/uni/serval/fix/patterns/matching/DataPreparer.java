package edu.lu.uni.serval.fix.patterns.matching;

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
 * Select source code tokens of fixed and unfixed violations.
 * @author kui.liu
 *
 */
public class DataPreparer {

	public void prepare() throws IOException {
		
		String outputPath = Configuration.ROOT_PATH + "RQ3/FixPatterns/";
		FileHelper.deleteDirectory(outputPath);
		
		int MAX_SIZE = 40;
		
		String alarmPatchFile = Configuration.SELECTED_BUGGY_TOKEN_FILE;
		String content = FileHelper.readFile(alarmPatchFile);
		BufferedReader reader = new BufferedReader(new StringReader(content));
		String line = null;
		int index = -1;
		
		StringBuilder tokenBuilder = new StringBuilder();
		StringBuilder indexBuilder = new StringBuilder();
		StringBuilder sizesBuilder = new StringBuilder();
		int counter = 0;
		while ((line = reader.readLine()) != null) {
			index ++;
			String[] tokens = line.split(" ");
			if (tokens.length <= MAX_SIZE) {
				tokenBuilder.append(line + "\n");
				indexBuilder.append(index + "\n");
				counter ++;
			}
			sizesBuilder.append(tokens.length).append("\n");
		}
		reader.close();
		System.out.println(counter);
		FileHelper.outputToFile(outputPath + "tokenSizes.csv", sizesBuilder, false);
		FileHelper.outputToFile(outputPath + "fixedViolationTokens.list", tokenBuilder, false);
		FileHelper.outputToFile(outputPath + "fixedViolationsIndex.list", indexBuilder, false);
		

		File violationTypesFile = new File(Configuration.ROOT_PATH + "UnfixedViolations_RQ3/");
		File[] violationTypeFiles = violationTypesFile.listFiles();
		System.out.println(violationTypeFiles.length);
		
		for (File violationType : violationTypeFiles) {
			if (violationType.isDirectory()) {
				// merge data
				String filePath = violationType.getPath() + "/";
				mergeData(filePath);

				String outputTypePath = Configuration.ROOT_PATH + "RQ3/UnfixedViolations/" + violationType.getName() + "/";
				FileHelper.deleteDirectory(outputTypePath);
				selectedData(MAX_SIZE, violationType, outputTypePath);
				selectedSourceCode(violationType, outputTypePath);
			}
		}
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
		System.out.println(sourceCodeFilesFolder);
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

	public void selectedData(int maxSize, File filePath, String outputPath) {
		List<Integer> sizes = readIntegers(filePath.getPath() + "/Sizes.list");
		selectedData(maxSize, sizes, filePath.getPath() + "/Tokens.list", "SelectedTokens.list", outputPath);
	}

	private void selectedData(int maxSize, List<Integer> sizes, String inputFile, String fileName, String outputPath) {
		FileInputStream fis = null;
		Scanner scanner = null;
		int index = -1;
		StringBuilder tokensBuilder = new StringBuilder();
		StringBuilder indexesBuilder = new StringBuilder();
		int counter = 0;
		try {
			fis = new FileInputStream(inputFile);
			scanner = new Scanner(fis);
			
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				index ++;
				
				if (sizes.get(index) <= maxSize) {
					tokensBuilder.append(line + "\n");
					indexesBuilder.append(index + "\n");
					counter ++;
					if (counter % 10000 == 0) {
						FileHelper.outputToFile(outputPath + fileName, tokensBuilder, true);
						tokensBuilder.setLength(0);
						break;
					}
				}
			}
			FileHelper.outputToFile(outputPath + fileName, tokensBuilder, true);
			tokensBuilder.setLength(0);
			System.out.println(outputPath + "== " + counter);
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

	public void selectedSourceCode(File filePath, String outputPath) throws IOException {
		List<Integer> sourceCodeIndexes = readIntegers(outputPath + "SourceCodeIndexes.list");
		readSourceCode(filePath, sourceCodeIndexes, outputPath);
	}

	private void readSourceCode(File path, List<Integer> fixedAlarmIndexes, String outputPath) throws IOException {
		String file = path.getPath() + "/SourceCode.list";
		String outputFile = outputPath + "SelectedSourceCode.list";
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
							break;
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
