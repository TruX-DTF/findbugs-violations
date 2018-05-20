package edu.lu.uni.serval.mining.pattern.unfixedViolation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import edu.lu.uni.serval.config.Configuration;
import edu.lu.uni.serval.deeplearner.CNNFeatureLearner;
import edu.lu.uni.serval.utils.FileHelper;

/**
 * Learn source code features of unfixed violations with CNN.
 * 
 * @author kui.liu
 *
 */
public class FeatureLearner {

	public void learn() {
		// Configuration.ROOT_PATH + "UnfixedViolations/" + #violationType# + "/selectedData/" + vectorizedTokens.csv
		// Configuration.ROOT_PATH + "UnfixedViolations/" + #violationType# + "/LearnedFeatures/" + i_CNNOutput.csv
		File violationTypesFile = new File(Configuration.ROOT_PATH + "UnfixedViolations/");
		File[] violationTypes = violationTypesFile.listFiles();
		for (File violationType : violationTypes) {
			FeatureLearner app = new FeatureLearner();
			int batchSize = 1000;
			
			String filePath = violationType.getPath() + "/selectedData/";
			String inputData = filePath + "CNNInput.csv";
			FileHelper.deleteFiles(inputData);
			app.selectData(inputData, filePath+ "vectorizedTokens.csv", batchSize);
			
			int sizeOfVector = 80;
			int sizeOfTokenVec = Configuration.VECTOR_SIZE_OF_EMBEDED_TOKEN2;
			int sizeOfFeatureVector = 300;
			
			try {
				CNNFeatureLearner learner = new CNNFeatureLearner(new File(inputData), sizeOfVector, sizeOfTokenVec, batchSize, sizeOfFeatureVector);
				learner.setNumberOfEpochs(20);//10
				learner.setSeed(123);
				learner.setNumOfOutOfLayer1(20);
				learner.setNumOfOutOfLayer2(50);
				learner.setOutputPath(violationType.getPath() + "/LearnedFeatures/");
				
				learner.extracteFeaturesWithCNN();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			FileHelper.deleteFile(inputData);
			System.err.println("===========Finished " + violationType.getName() + "===============");
		}
		
	}

	public void selectData(String inputData, String data, int batchSize) {
		if (!new File(data).exists()) {
			System.out.println(data);
			return;
		}
		FileInputStream fis = null;
		Scanner scanner = null;
		
//		int size = 0;
		
		try {
			fis = new FileInputStream(data);
			scanner = new Scanner(fis);
			StringBuilder builder = new StringBuilder();
			int counter = 0;
			
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				counter ++;
				builder.append(line + "\n");
				
//				String[] elements = line.split(",");
//				int length = elements.length;
//				if (size != length) {
//					size = length;
//					System.out.println(size);
//				}
				
				if (counter % batchSize == 0) {
					FileHelper.outputToFile(inputData, builder, true);
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
		FileHelper.deleteFile(data);
	}
	
}
