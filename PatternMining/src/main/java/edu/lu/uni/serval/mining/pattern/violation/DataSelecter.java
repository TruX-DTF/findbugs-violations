package edu.lu.uni.serval.mining.pattern.violation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import edu.lu.uni.serval.config.Configuration;
import edu.lu.uni.serval.utils.FileHelper;

public class DataSelecter {

	public void selectData() {
		String violationPath = Configuration.ROOT_PATH + "Violations_tokens/";
		String outputPath = violationPath + "selectedData/";
		FileHelper.deleteDirectory(outputPath);
		
		int maxSize = 40;
		
		List<Integer> fixViolationSizes = readSizes(violationPath + "fixedViolationsTokenSizes.csv");
		selectedData(maxSize, fixViolationSizes, violationPath, "fixedViolationsTokens.list", outputPath);
		
		List<Integer> unfixViolationSizes = readSizes(violationPath + "unfixedViolationsTokenSizes.csv");
		selectedData(maxSize, unfixViolationSizes, violationPath, "unfixedViolationsTokens.list", outputPath);
	}

	private void selectedData(int maxSize, List<Integer> sizes, String violationPath, String fileName,
			String outputPath) {
		FileInputStream fis = null;
		Scanner scanner = null;
		int index = -1;
		StringBuilder tokensBuilder = new StringBuilder();
		StringBuilder indexesBuilder = new StringBuilder();
		try {
			fis = new FileInputStream(violationPath + fileName);
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
			FileHelper.outputToFile(outputPath + fileName.replace(".list", "Indexes.csv"), indexesBuilder, false);
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

	private List<Integer> readSizes(String fileName) {
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

}
