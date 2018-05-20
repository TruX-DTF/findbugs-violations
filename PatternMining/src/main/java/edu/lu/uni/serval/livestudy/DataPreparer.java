package edu.lu.uni.serval.livestudy;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import edu.lu.uni.serval.config.Configuration;
import edu.lu.uni.serval.utils.FileHelper;

/**
 * Select source code tokens of fixed violations.
 * 
 * Select bug instances from Live Study.
 * 
 * Five projects:
 * commons-io
 * commons-math
 * aries
 * commons-lang
 * mahout
 * camel
 * 
 * 
 * derby
 * luence
 * log4j
 * maven-plugins
 * poi
 * spring
 * struts
 * tomcat
 * 
 * @author kui.liu
 *
 */
public class DataPreparer {
	
	private static final int MAX_SIZE = 40; // Upper whisker: 38 (fixed), 28 (unfixed)

	public void prepare() throws IOException {
		
		String outputPath = Configuration.ROOT_PATH + "LiveStudy/FixPatterns/";
		FileHelper.deleteDirectory(outputPath);
		
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
		
		
		// Select bug instances from Live Study.
		String sizesFile = Configuration.ROOT_PATH + "LiveStudy/BugsInfo/sizes.list";
		List<Integer> indexes = readIndexes(sizesFile);
		String tokensFile = Configuration.ROOT_PATH + "LiveStudy/BugsInfo/tokens.list";
		String bugsInfoFile = Configuration.ROOT_PATH + "LiveStudy/BugsInfo/bugsInfo.list";
		selectTokens(indexes, tokensFile);
		selectBugs(indexes, bugsInfoFile);
		System.out.println(indexes.size());
	}

	private void selectBugs(List<Integer> indexes, String bugsInfoFile) throws IOException {
		FileInputStream fis = new FileInputStream(bugsInfoFile);
		Scanner scanner = new Scanner(fis);
		
		String singleBug = "";
		int index = -1;
		StringBuilder selectedBugInfo = new StringBuilder();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if ("###BugInstance###".equals(line)) {
				if (indexes.contains(index)) {
					selectedBugInfo.append(singleBug);
				}
				
				singleBug = "";
				index ++;
			}
			singleBug += line + "\n";
		}
		if (indexes.contains(index)) {
			selectedBugInfo.append(singleBug);
		}
		scanner.close();
		fis.close();
		
		FileHelper.outputToFile(Configuration.ROOT_PATH + "LiveStudy/BugsInfo/selectedBugsInfo.list", selectedBugInfo, false);
	}

	private void selectTokens(List<Integer> indexes, String tokensFile) throws IOException {
		FileInputStream fis = new FileInputStream(tokensFile);
		Scanner scanner = new Scanner(fis);
		
		int index = -1;
		StringBuilder tokensBuilder = new StringBuilder();
		while (scanner.hasNextLine()) {
			String tokens = scanner.nextLine();
			index ++;
			if (indexes.contains(index)) {
				tokensBuilder.append(tokens).append("\n");
			}
		}
		scanner.close();
		fis.close();
		
		FileHelper.outputToFile(Configuration.ROOT_PATH + "LiveStudy/BugsInfo/selectedTokens.list", tokensBuilder, false);
	}

	private List<Integer> readIndexes(String sizesFile) throws IOException {
		List<Integer> indexes = new ArrayList<>();
		String content = FileHelper.readFile(sizesFile);
		BufferedReader reader = new BufferedReader(new StringReader(content));
		String line = null;
		int index = -1;
		while ((line = reader.readLine()) != null) {
			index ++;
			int size = Integer.parseInt(line);
			if (size <= MAX_SIZE) {
				indexes.add(index);
			}
		}
		return indexes;
	}

}
