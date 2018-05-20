package edu.lu.uni.serval.mining.pattern.violation;

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
		String inputData = Configuration.ROOT_PATH + "Violations_tokens/vectorizedData/CNNInput.csv";
		FileHelper.deleteFiles(inputData);
		
		// Merge the data of fixed and unfixed violations.
		selectData(inputData, Configuration.ROOT_PATH + "Violations_tokens/vectorizedTokens/fixedViolationsTokens.csv");
		selectData(inputData, Configuration.ROOT_PATH + "Violations_tokens/vectorizedTokens/unfixedViolationsTokens.csv");
		
		
		int sizeOfVector = 40;
		int sizeOfTokenVec = Configuration.VECTOR_SIZE_OF_EMBEDED_TOKEN2;
		int batchSize = 100;
		int sizeOfFeatureVector = 300;
		
		try {
			CNNFeatureLearner learner = new CNNFeatureLearner(new File(inputData), sizeOfVector, sizeOfTokenVec, batchSize, sizeOfFeatureVector);
			learner.setNumberOfEpochs(10);//10
			learner.setSeed(123);
			learner.setNumOfOutOfLayer1(20);
			learner.setNumOfOutOfLayer2(50);
			learner.setOutputPath( Configuration.ROOT_PATH + "Violations_tokens/vectorizedData/");
			
			learner.extracteFeaturesWithCNN();
			
			Feature feature = new Feature();
			feature.classifyFeatureData();
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
				
				if (counter % 5000 == 0) {
					FileHelper.outputToFile(inputData, builder, true);
					builder.setLength(0);
				}
				
				if (counter == 65000) break;
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
