package edu.lu.uni.serval.tuningParameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import edu.lu.uni.serval.config.Configuration;
import edu.lu.uni.serval.deeplearner.Word2VecEncoder;
import edu.lu.uni.serval.mining.utils.DataPreparation;
import edu.lu.uni.serval.utils.FileHelper;

public class EmbedTokens {

	public static void main(String[] args) {
		Word2VecEncoder encoder = new Word2VecEncoder();
		int windowSize = 4;
		encoder.setWindowSize(windowSize);
		try {
			File inputFile = new File("../FPM_Violations/TuneParameters/selectedTokens.list");
			int minWordFrequency = 1;
			int layerSize = Configuration.VECTOR_SIZE_OF_EMBEDED_TOKEN2;
			String outputFileName = "../FPM_Violations/TuneParameters/EmbeddedTokens.list";
			encoder.embedTokens(inputFile, minWordFrequency, layerSize, outputFileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Map<String, String> embeddedTokens = DataPreparation.readEmbeddedTokens("../FPM_Violations/TuneParameters/EmbeddedTokens.list");
		
		String zeroVector = "";
		for (int i =0, length = Configuration.VECTOR_SIZE_OF_EMBEDED_TOKEN2 - 1; i < length; i ++) {
			zeroVector += "0, ";
		}
		zeroVector += "0";
		
		vectorizedTokens("../FPM_Violations/TuneParameters/selectedTokens.list", embeddedTokens, zeroVector);
	}
	
	private static void vectorizedTokens(String alarmTokens, Map<String, String> embeddedTokens, String zeroVector) {
		FileInputStream fis = null;
		Scanner scanner = null;
		int counter = 0;
		int maxSize = 28;
		String trainingData = alarmTokens.replace(".list", "Training.csv");
		FileHelper.deleteFile(trainingData);
		String testingData = alarmTokens.replace(".list", "Testing.csv");
		FileHelper.deleteFile(testingData);
		
		List<String> labels = DataPreparation.readStringList("../FPM_Violations/TuneParameters/labels.list");
		Map<String, Integer> map = new HashMap<>();

		StringBuilder trainingBuilder = new StringBuilder();
		StringBuilder testingBuilder = new StringBuilder();
		
		int index = -1;
		int length = 0;
		try {
			fis = new FileInputStream(alarmTokens);
			scanner = new Scanner(fis);
			while (scanner.hasNextLine()) {
				index ++;
				String line = scanner.nextLine();
				StringBuilder vectorStr = convertToVector(embeddedTokens, line, maxSize, zeroVector);
				String[] aaa = vectorStr.toString().split(", ");
				if (aaa.length != length) {
					length = aaa.length;
					System.out.println(length);
				}
				String label = labels.get(index);
				Integer amount = map.get(label);
				if (amount == null || amount < 900) {
					trainingBuilder.append(vectorStr + "," + label + "\n");
					if (map.containsKey(label)) {
						map.put(label, map.get(label) + 1);
					} else {
						map.put(label, 1);
					}
				} else {
					testingBuilder.append(vectorStr + "," + label + "\n");
				}
				
				if (++ counter % 1000 == 0) {
					FileHelper.outputToFile(trainingData, trainingBuilder, true);
					trainingBuilder.setLength(0);
					FileHelper.outputToFile(testingData, testingBuilder, true);
					testingBuilder.setLength(0);
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
		
		FileHelper.outputToFile(trainingData, trainingBuilder, true);
		trainingBuilder.setLength(0);
		FileHelper.outputToFile(testingData, testingBuilder, true);
		testingBuilder.setLength(0);
	}
	
	public static StringBuilder convertToVector(Map<String, String> embeddedTokens, String line, int maxSize, String zeroVector) {
		String[] tokens = line.split(" ");
		StringBuilder vectorStr = new StringBuilder();
		int length = tokens.length;
		if (length == maxSize) {
			for (int i = 0; i < length - 1; i ++) {
				String token = tokens[i];
				vectorStr.append(embeddedTokens.get(token) + ", ");
			}
			vectorStr.append(embeddedTokens.get(tokens[length - 1]));
		} else {
			if (length > maxSize) {
				System.out.println(length);
			}
			for (int i = 0; i < length; i ++) {
				String token = tokens[i];
				vectorStr.append(embeddedTokens.get(token) + ", ");
			}
			for (int i = length; i < maxSize - 1; i ++) {
				vectorStr.append(zeroVector + ", ");
			}
			vectorStr.append(zeroVector);
		}
		
		return vectorStr;
	}
}
