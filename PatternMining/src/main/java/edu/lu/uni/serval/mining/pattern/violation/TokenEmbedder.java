package edu.lu.uni.serval.mining.pattern.violation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

import edu.lu.uni.serval.config.Configuration;
import edu.lu.uni.serval.deeplearner.Word2VecEncoder;
import edu.lu.uni.serval.mining.utils.DataPreparation;
import edu.lu.uni.serval.utils.FileHelper;

/**
 * Embed tokens of violation code with Word2Vec.
 * 
 * @author kui.liu
 *
 */
public class TokenEmbedder {

	public void embed() {
		String violationTokens = Configuration.ROOT_PATH + "Violations_tokens/selectedData/";
		
		String embedding = Configuration.ROOT_PATH + "Violations_tokens/" + "embedding/";
		
		FileHelper.outputToFile(embedding + "allTokens.list", FileHelper.readFile(violationTokens + "fixedViolationsTokens.list"), false);		
		FileHelper.outputToFile(embedding + "allTokens.list", FileHelper.readFile(violationTokens + "unfixedViolationsTokens.list"), true);

		Word2VecEncoder encoder = new Word2VecEncoder();
		int windowSize = 4;
		encoder.setWindowSize(windowSize);
		try {
			File inputFile = new File(embedding + "allTokens.list");
			int minWordFrequency = 1;
			int layerSize = 300;
			String outputFileName = embedding + "embeddedTokens.list";
			encoder.embedTokens(inputFile, minWordFrequency, layerSize, outputFileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Map<String, String> embeddedTokens = DataPreparation.readEmbeddedTokens(embedding + "embeddedTokens.list");
		
		String zeroVector = "";
		for (int i =0, length = Configuration.VECTOR_SIZE_OF_EMBEDED_TOKEN2 - 1; i < length; i ++) {
			zeroVector += "0, ";
		}
		zeroVector += "0";
		
		vectorizedTokens(violationTokens, "fixedViolationsTokens.list", embeddedTokens, zeroVector);
		vectorizedTokens(violationTokens, "unfixedViolationsTokens.list", embeddedTokens, zeroVector);
	}

	private void vectorizedTokens(String violationTokens, String fileName, Map<String, String> embeddedTokens, String zeroVector) {
		FileInputStream fis = null;
		Scanner scanner = null;
		StringBuilder builder = new StringBuilder();
		int counter = 0;
		int maxSize = 40;
		String outputFile = Configuration.ROOT_PATH + "Violations_tokens/vectorizedTokens/" + fileName.replace(".list", ".csv");
		
		try {
			fis = new FileInputStream(violationTokens + fileName);
			scanner = new Scanner(fis);
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				StringBuilder vectorStr = DataPreparation.convertToVector(embeddedTokens, line, maxSize, zeroVector);
				builder.append(vectorStr);
				if (++ counter % 1000 == 0) {
					FileHelper.outputToFile(outputFile, builder, true);
					builder.setLength(0);
				}
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
		
		FileHelper.outputToFile(outputFile, builder, true);
		builder.setLength(0);
	}


}
