package edu.lu.uni.serval.fix.patterns.matching2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import edu.lu.uni.serval.config.Configuration;
import edu.lu.uni.serval.deeplearner.CNNFeatureLearner;
import edu.lu.uni.serval.utils.FileHelper;

public class FeatureLearner {

	public void learn() {
		String inputData = Configuration.ROOT_PATH + "Alarm_matching/vectorizedData/CNNInput.csv";
		FileHelper.deleteFiles(inputData);
		
		selectData(inputData, Configuration.ROOT_PATH + "Alarm_matching/vectorizedTokens/fixedAlarms.csv");
		selectData(inputData, Configuration.ROOT_PATH + "Alarm_matching/vectorizedTokens/unfixedAlarmsTokens.csv");
		
		
		int sizeOfVector = 40;
		int sizeOfTokenVec = 300;//Configuration.VECTOR_SIZE_OF_EMBEDED_TOKEN1;
		int batchSize = 100;
		int sizeOfFeatureVector = 300;
		
		try {
			CNNFeatureLearner learner = new CNNFeatureLearner(new File(inputData), sizeOfVector, sizeOfTokenVec, batchSize, sizeOfFeatureVector);
			learner.setNumberOfEpochs(10);//10
			learner.setSeed(123);
			learner.setNumOfOutOfLayer1(20);
			learner.setNumOfOutOfLayer2(50);
			learner.setOutputPath(Configuration.ROOT_PATH + "Alarm_matching/vectorizedData/");
			
			learner.extracteFeaturesWithCNN();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void selectData(String inputData, String data) {
		FileInputStream fis = null;
		Scanner scanner = null;
		
		try {
			fis = new FileInputStream(data);
			scanner = new Scanner(fis);
			StringBuilder builder = new StringBuilder();
			int counter = 0;
			
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				counter ++;
				builder.append(line + "\n");
				
				if (counter % 1000 == 0) {
					FileHelper.outputToFile(inputData, builder, true);
					builder.setLength(0);
				}
				
			}
			
			FileHelper.outputToFile(inputData, builder, true);
			builder.setLength(0);
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
	
}
