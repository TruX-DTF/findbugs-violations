package edu.lu.uni.serval.defects4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import edu.lu.uni.serval.config.Configuration;
import edu.lu.uni.serval.utils.FileHelper;

/**
 * Select source code tokens of fixed violations.
 * 
 * @author kui.liu
 *
 */
public class DataPreparer {
	
	private static final int MAX_SIZE = 40;

	public void prepare() throws IOException {
		
		String outputPath = Configuration.ROOT_PATH + "RQ3_2/FixPatterns/";
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
			if ("Block Block".equals(line)) continue;
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
		
	}

}
