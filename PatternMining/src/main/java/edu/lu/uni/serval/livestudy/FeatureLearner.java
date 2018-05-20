package edu.lu.uni.serval.livestudy;

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

	public void learner() {
		String inputData = Configuration.ROOT_PATH + "LiveStudy/FixPatterns/CNNInput.csv";
		
		int sizeOfVector = 40;  // TODO
		int sizeOfTokenVec = 300;//Configuration.VECTOR_SIZE_OF_EMBEDED_TOKEN1;
		int batchSize = 100;   // TODO
		int sizeOfFeatureVector = 300;
		
		try {
			CNNFeatureLearner learner = new CNNFeatureLearner(new File(inputData), sizeOfVector, sizeOfTokenVec, batchSize, sizeOfFeatureVector);
			learner.setNumberOfEpochs(2);//10
			learner.setSeed(123);
			learner.setNumOfOutOfLayer1(20);
			learner.setNumOfOutOfLayer2(50);
			learner.setOutputPath(Configuration.ROOT_PATH + "LiveStudy/FixPatterns/");
			
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
