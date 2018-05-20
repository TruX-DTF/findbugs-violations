package edu.lu.uni.serval.livestudy;

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
 * Embed source code tokens of fixed and unfixed violations.
 * 
 * @author kui.liu
 *
 */
public class TokenEmbedder {
	
	private static final int MAX_SIZE = 40;//TODO

	public void embed() throws IOException {
		String fixedTokens = Configuration.ROOT_PATH + "LiveStudy/FixPatterns/fixedViolationTokens.list";
		String buggyTokens = Configuration.ROOT_PATH + "LiveStudy/BugsInfo/selectedTokens.list";
		
		String embedding = Configuration.ROOT_PATH + "LiveStudy/FixPatterns/allTokens.list";
		
		// merge source code tokens of fixed violations.
		FileHelper.outputToFile(embedding, FileHelper.readFile(fixedTokens), false);	
		// merge source code tokens of unfixed violations.
		FileHelper.outputToFile(embedding, FileHelper.readFile(buggyTokens), true);

		Word2VecEncoder encoder = new Word2VecEncoder();
		int windowSize = 4;
		encoder.setWindowSize(windowSize);
		try {
			File inputFile = new File(embedding);
			int minWordFrequency = 1;
			int layerSize = 300;
			String outputFileName = Configuration.ROOT_PATH + "LiveStudy/FixPatterns/embeddedTokens.list";
			encoder.embedTokens(inputFile, minWordFrequency, layerSize, outputFileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Map<String, String> embeddedTokens = DataPreparation.readEmbeddedTokens(Configuration.ROOT_PATH + "LiveStudy/FixPatterns/embeddedTokens.list");
		
		String zeroVector = "";
		for (int i =0, length = Configuration.VECTOR_SIZE_OF_EMBEDED_TOKEN2 - 1; i < length; i ++) {
			zeroVector += "0, ";
		}
		zeroVector += "0";
		
		vectorizedTokens(embedding, embeddedTokens, zeroVector);
	}

	private void vectorizedTokens(String tokensFile, Map<String, String> embeddedTokens, String zeroVector) {
		FileInputStream fis = null;
		Scanner scanner = null;
		StringBuilder builder = new StringBuilder();
		int counter = 0;
		String outputFile = Configuration.ROOT_PATH + "LiveStudy/FixPatterns/CNNInput.csv";
		
		try {
			fis = new FileInputStream(tokensFile);
			scanner = new Scanner(fis);
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				StringBuilder vectorStr = DataPreparation.convertToVector(embeddedTokens, line, MAX_SIZE, zeroVector);
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
