package edu.lu.uni.serval.fix.patterns.matching;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import edu.lu.uni.serval.config.Configuration;
import edu.lu.uni.serval.deeplearner.CNNFeatureLearner;
import edu.lu.uni.serval.utils.FileHelper;

public class FeatureLearner {

	public void learn() {
		String outputTypePath = Configuration.ROOT_PATH + "RQ3/UnfixedViolations/";
		File[] violationTypes = new File(outputTypePath).listFiles();
		for (File file : violationTypes) {
			int batchSize = 100;
			
			String filePath = file.getPath();
//			if (!filePath.endsWith("SE_NO_SERIALVERSIONID")) continue;
			String inputData = filePath + "/CNNInput.csv";
			String outputPath = filePath + "/LearnedFeatures/";
			FileHelper.deleteDirectory(outputPath);
			
			int sizeOfVector = 40;// TODO
			int sizeOfTokenVec = Configuration.VECTOR_SIZE_OF_EMBEDED_TOKEN2;
			int sizeOfFeatureVector = 300;
			
			try {
				CNNFeatureLearner learner = new CNNFeatureLearner(new File(inputData), sizeOfVector, sizeOfTokenVec, batchSize, sizeOfFeatureVector);
				learner.setNumberOfEpochs(2);//10
				learner.setSeed(123);
				learner.setNumOfOutOfLayer1(20);
				learner.setNumOfOutOfLayer2(50);
				learner.setOutputPath(outputPath);
				
				learner.extracteFeaturesWithCNN();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			FileHelper.deleteFile(inputData);
			System.err.println("===========Finished " + file.getName() + "===============");
		}
	}

}
