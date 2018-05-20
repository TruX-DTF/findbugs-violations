package edu.lu.uni.serval.defects4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import edu.lu.uni.serval.config.Configuration;
import edu.lu.uni.serval.deeplearner.CNNFeatureLearner;

/**
 * Feature learning.
 * 
 * @author kui.liu
 *
 */
public class FeatureLearner {

	public void learn() {
		String inputData = Configuration.ROOT_PATH + "RQ3_2/FixPatterns/CNNInput.csv";
		
		int sizeOfVector = 40;
		int sizeOfTokenVec = Configuration.VECTOR_SIZE_OF_EMBEDED_TOKEN1;
		int batchSize = 100;
		int sizeOfFeatureVector = 300;
		
		try {
			CNNFeatureLearner learner = new CNNFeatureLearner(new File(inputData), sizeOfVector, sizeOfTokenVec, batchSize, sizeOfFeatureVector);
			learner.setNumberOfEpochs(100);
			learner.setSeed(123);
			learner.setNumOfOutOfLayer1(20);
			learner.setNumOfOutOfLayer2(50);
			learner.setIterations(1);
			learner.setOutputPath(Configuration.ROOT_PATH + "RQ3_2/FixPatterns/");
			
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
