package edu.lu.uni.serval.mining.pattern.fixedViolation;

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
 * Embed source code tokens of unfixed violations.
 * 
 * @author kui.liu
 *
 */
public class TokenEmbedder {

	public void embed() {
		// Configuration.ROOT_PATH + "fixedViolations/selectedData/(SelectedTokens.list/embeddedTokens.list)"
		// Configuration.ROOT_PATH + "fixedViolations/selectedData/" + vectorizedTokens.csv
		String filePath = Configuration.ROOT_PATH + "fixedViolations/";
		embedTokens(filePath);
		int maxSize = 80;
//		app.vectorizedTokens(filePath, maxSize);
		vectorizedTokens2(filePath, maxSize);
	}

	private void vectorizedTokens2(String filePath, int maxSize) {
		String selectedData = filePath + "/selectedData/";
		String embeddedTokensFile = selectedData + "embeddedTokens.list";
		Map<String, String> embeddedTokens = DataPreparation.readEmbeddedTokens(embeddedTokensFile);
		String zeroVector = "";
		for (int i =0, length = Configuration.VECTOR_SIZE_OF_EMBEDED_TOKEN2 - 1; i < length; i ++) {
			zeroVector += "0, ";
		}
		zeroVector += "0";
		
		File typesFile = new File(filePath + "/Types/");
		File[] typeFiles = typesFile.listFiles();
		for (File typeFile : typeFiles) {
			if (typeFile.isDirectory()) {
				vectorizedTokens(typeFile.getPath() + "/", "Tokens.list", embeddedTokens, zeroVector, maxSize);
			}
		}
	}

	public void embedTokens(String filePath) {
		String selectedData = filePath + "selectedData/";
		
		Word2VecEncoder encoder = new Word2VecEncoder();
		int windowSize = 4;
		encoder.setWindowSize(windowSize);
		try {
			File inputFile = new File(selectedData + "SelectedTokens.list");
			int minWordFrequency = 1;
			int layerSize = Configuration.VECTOR_SIZE_OF_EMBEDED_TOKEN2;
			String outputFileName = selectedData + "embeddedTokens.list";
			encoder.embedTokens(inputFile, minWordFrequency, layerSize, outputFileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void vectorizedTokens(String filePath, int maxSize) {
		String selectedData = filePath + "/selectedData/";
		String embeddedTokensFile = selectedData + "embeddedTokens.list";
		Map<String, String> embeddedTokens = DataPreparation.readEmbeddedTokens(embeddedTokensFile);
		
		String zeroVector = "";
		for (int i =0, length = Configuration.VECTOR_SIZE_OF_EMBEDED_TOKEN2 - 1; i < length; i ++) {
			zeroVector += "0, ";
		}
		zeroVector += "0";
		
		vectorizedTokens(selectedData, "SelectedTokens.list", embeddedTokens, zeroVector, maxSize);
	}

	private void vectorizedTokens(String filePath, String fileName, Map<String, String> embeddedTokens, String zeroVector, int maxSize) {
		FileInputStream fis = null;
		Scanner scanner = null;
		StringBuilder builder = new StringBuilder();
		int counter = 0;
		String outputFile = filePath + "vectorizedTokens.csv";
		FileHelper.deleteFile(outputFile);
		try {
			fis = new FileInputStream(filePath + fileName);
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
