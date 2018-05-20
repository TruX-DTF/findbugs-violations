package edu.lu.uni.serval.mining.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import edu.lu.uni.serval.config.Configuration;
import edu.lu.uni.serval.deeplearner.CNNFeatureLearner;
import edu.lu.uni.serval.utils.FileHelper;

public class FeatureLearner {
	
	/**
	 * Learn features of edit scripts for fix patterns mining.
	 */
	public void learnFeatures() {
		String editScriptsVectorFile = Configuration.VECTORIED_EDIT_SCRIPTS; // input
		int sizeOfVector = Integer.parseInt(FileHelper.readFile(Configuration.MAX_TOKEN_VECTORS_SIZE_OF_EDIT_SCRIPTS).trim());
		int sizeOfTokenVec = Configuration.VECTOR_SIZE_OF_EMBEDED_TOKEN1;
		int batchSize = 100;
		int sizeOfFeatureVector = 300;
		
		try {
			CNNFeatureLearner learner = new CNNFeatureLearner(new File(editScriptsVectorFile), sizeOfVector, sizeOfTokenVec, batchSize, sizeOfFeatureVector);
			learner.setNumberOfEpochs(20);//10
			learner.setSeed(123);
			learner.setNumOfOutOfLayer1(20);
			learner.setNumOfOutOfLayer2(50);
			learner.setOutputPath(Configuration.EXTRACTED_FEATURES);
			
			learner.extracteFeaturesWithCNN();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void learnFeaturesOfSourceCode() {
		int sizeOfVector = Integer.parseInt(FileHelper.readFile(Configuration.MAX_TOKEN_VECTORS_SIZE_OF_SOURCE_CODE));
		int sizeOfTokenVec = Configuration.VECTOR_SIZE_OF_EMBEDED_TOKEN2;
		int batchSize = 1000;
		int sizeOfExtractedFeatureVector = 200;
		
		try {
			CNNFeatureLearner learner = new CNNFeatureLearner(new File(Configuration.VECTORIED_ALL_SOURCE_CODE1), sizeOfVector, sizeOfTokenVec, batchSize, sizeOfExtractedFeatureVector);
			learner.setNumberOfEpochs(20);
			learner.setSeed(123);
			learner.setNumOfOutOfLayer1(20);
			learner.setNumOfOutOfLayer2(50);
			learner.setOutputPath(Configuration.EXTRACTED_FEATURES_EVALUATION);
			
			learner.extracteFeaturesWithCNN();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
