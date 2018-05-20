package edu.lu.uni.serval.fix.patterns.matching2;

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
		String alarmTokens = Configuration.ROOT_PATH + "Alarm_matching/fixedAlarms.list";//Configuration.ROOT_PATH + "Alarms_tokens/selectedData/";
		
		String embedding = Configuration.ROOT_PATH + "Alarm_matching/embedding/allTokens.list";
		
		// merge source code tokens of fixed violations.
		FileHelper.outputToFile(embedding, FileHelper.readFile(alarmTokens), false);	
		// merge source code tokens of unfixed violations.
		selectUnfixedData(embedding, Configuration.ROOT_PATH + "Alarms_tokens/selectedData/unfixedAlarmsTokens.list");

		Word2VecEncoder encoder = new Word2VecEncoder();
		int windowSize = 4;
		encoder.setWindowSize(windowSize);
		try {
			File inputFile = new File(embedding);
			int minWordFrequency = 1;
			int layerSize = 300;
			String outputFileName = Configuration.ROOT_PATH + "Alarm_matching/embedding/embeddedTokens.list";
			encoder.embedTokens(inputFile, minWordFrequency, layerSize, outputFileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Map<String, String> embeddedTokens = DataPreparation.readEmbeddedTokens(Configuration.ROOT_PATH + "Alarm_matching/embedding/embeddedTokens.list");
		
		String zeroVector = "";
		for (int i =0, length = Configuration.VECTOR_SIZE_OF_EMBEDED_TOKEN2 - 1; i < length; i ++) {
			zeroVector += "0, ";
		}
		zeroVector += "0";
		
		vectorizedTokens(alarmTokens, embeddedTokens, zeroVector);
		vectorizedTokens(Configuration.ROOT_PATH + "Alarms_tokens/selectedData/unfixedAlarmsTokens.list", embeddedTokens, zeroVector);
	}

	private void selectUnfixedData(String embedding, String unfixedAlarms) throws IOException {
		FileInputStream fis = new FileInputStream(unfixedAlarms);
		Scanner scanner = new Scanner(fis);
		
		StringBuilder builder = new StringBuilder();
		int counter = 0;
		
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			counter ++;
			builder.append(line + "\n");
			
			if (counter % 5000 == 0) {
				FileHelper.outputToFile(embedding, builder, true);
				builder.setLength(0);
			}
			
			if (counter == 65000) break;
		}
		
		FileHelper.outputToFile(embedding, builder, true);
		builder.setLength(0);
		scanner.close();
		fis.close();
	}

	private void vectorizedTokens(String alarmTokens, Map<String, String> embeddedTokens, String zeroVector) {
		FileInputStream fis = null;
		Scanner scanner = null;
		StringBuilder builder = new StringBuilder();
		int counter = 0;
		int maxSize = 40;
		String outputFile = Configuration.ROOT_PATH + "Alarm_matching/vectorizedTokens/" + FileHelper.getFileNameWithoutExtension(new File(alarmTokens)) + ".csv";
		
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
				if (counter == 65000) break;
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
