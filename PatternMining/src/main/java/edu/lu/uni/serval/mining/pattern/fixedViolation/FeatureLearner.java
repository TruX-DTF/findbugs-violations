package edu.lu.uni.serval.mining.pattern.fixedViolation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import edu.lu.uni.serval.config.Configuration;
import edu.lu.uni.serval.deeplearner.CNNFeatureLearner;
import edu.lu.uni.serval.utils.FileHelper;

/**
 * Learn source code features of fixed violations with CNN.
 * 
 * @author kui.liu
 *
 */
public class FeatureLearner {

	public void learn() {
		int batchSize = 100;
		int sizeOfVector = 80;// TODO
		int sizeOfTokenVec = Configuration.VECTOR_SIZE_OF_EMBEDED_TOKEN2;
		int sizeOfFeatureVector = 300;
		
		String filePath = Configuration.ROOT_PATH + "fixedViolations/Types/";
		File typesFile = new File(filePath);
		File[] typeFiles = typesFile.listFiles();
		for (File typeFile : typeFiles) {
			if (typeFile.isDirectory()) {
				String path = typeFile.getPath() + "/";
				String inputData = path  + "vectorizedTokens.csv";
				
				try {
					CNNFeatureLearner learner = new CNNFeatureLearner(new File(inputData), sizeOfVector, sizeOfTokenVec, batchSize, sizeOfFeatureVector);
					learner.setNumberOfEpochs(1);
					learner.setSeed(123);
					learner.setNumOfOutOfLayer1(20);
					learner.setNumOfOutOfLayer2(50);
					FileHelper.deleteDirectory(path + "LearnedFeatures/");
					learner.setOutputPath(path + "LearnedFeatures/");
					
					learner.extracteFeaturesWithCNN();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				FileHelper.deleteFile(inputData);
				System.out.println("===========Finished===============");
			}
		}
	}
	
}
