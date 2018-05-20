package edu.lu.uni.serval.fix.patterns.matching;

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

	public void embed() throws IOException {
		String fixedViolationTokens = Configuration.ROOT_PATH + "RQ3/FixPatterns/fixedViolationTokens.list";
		
		String outputTypePath = Configuration.ROOT_PATH + "RQ3/UnfixedViolations/";
		File[] violationTypes = new File(outputTypePath).listFiles();
		for (File file : violationTypes) {
			String filePath = file.getPath();
//			if (!filePath.endsWith("SE_NO_SERIALVERSIONID")) continue;
			String embedding = filePath + "/allTokens.list";
			// merge source code tokens of fixed violations.
			FileHelper.outputToFile(embedding, FileHelper.readFile(fixedViolationTokens), false);	
			// merge source code tokens of unfixed violations.
			FileHelper.outputToFile(embedding, FileHelper.readFile(filePath + "/SelectedTokens.list"), true);
			
			
			// embedding
			Word2VecEncoder encoder = new Word2VecEncoder();
			int windowSize = 4;
			encoder.setWindowSize(windowSize);
			File inputFile = new File(embedding);
			int minWordFrequency = 1;
			int layerSize = 300;
			String outputFileName = filePath + "/embeddedTokens.list";
			encoder.embedTokens(inputFile, minWordFrequency, layerSize, outputFileName);
			
			
			// vectorizing
			Map<String, String> embeddedTokens = DataPreparation.readEmbeddedTokens(filePath + "/embeddedTokens.list");
			String zeroVector = "";
			for (int i =0, length = Configuration.VECTOR_SIZE_OF_EMBEDED_TOKEN2 - 1; i < length; i ++) {
				zeroVector += "0, ";
			}
			zeroVector += "0";
			vectorizedTokens(embedding, embeddedTokens, zeroVector, filePath + "/");
		}
	}

	private void vectorizedTokens(String alarmTokens, Map<String, String> embeddedTokens, String zeroVector, String outputPath) {
		FileInputStream fis = null;
		Scanner scanner = null;
		StringBuilder builder = new StringBuilder();
		int counter = 0;
		int maxSize = 40;
		String outputFile = outputPath + "CNNInput.csv";
		
		try {
			fis = new FileInputStream(alarmTokens);
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
