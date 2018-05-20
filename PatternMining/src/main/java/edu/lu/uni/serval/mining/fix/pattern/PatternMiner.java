package edu.lu.uni.serval.mining.fix.pattern;

import java.io.File;
import java.io.IOException;
import java.util.List;

import edu.lu.uni.serval.config.Configuration;
import edu.lu.uni.serval.deeplearner.Word2VecEncoder;
import edu.lu.uni.serval.mining.utils.DataPreparation;
import edu.lu.uni.serval.mining.utils.FeatureLearner;
import edu.lu.uni.serval.mining.utils.TokenEmbedder;
import edu.lu.uni.serval.utils.FileHelper;

public class PatternMiner {

	public static void main(String[] args) {
		// Prepare data.
		DataPreparer dp = new DataPreparer();
		dp.prepare();
		
		// Embed tokens of all selected edit scripts.
		// Input data: all tokens of selected edit scripts.
		boolean isViolations = false;
		if (isViolations) {
			String gumTreeOutput = Configuration.ALARMS;
			List<File> subDirectories = FileHelper.getAllSubDirectories(gumTreeOutput);
			for (File file : subDirectories) {
				Word2VecEncoder encoder = new Word2VecEncoder();
				int windowSize = 6;
				encoder.setWindowSize(windowSize);
				try {
					File inputFile = new File(file.getPath() + "/editScripts.list");
					int minWordFrequency = 1;
					int layerSize = Configuration.VECTOR_SIZE_OF_EMBEDED_TOKEN1;
					String outputFileName = file.getPath() + "/MiningInput/FeatureLearning/embeddedEditScriptTokens.list";
					encoder.embedTokens(inputFile, minWordFrequency, layerSize, outputFileName);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			String outputFileName = Configuration.EMBEDDED_EDIT_SCRIPT_TOKENS;
			FileHelper.deleteFile(outputFileName);
			
			TokenEmbedder embedder = new TokenEmbedder();
			embedder.embedTokensOfEditScripts();
		}
		
		// Vectorize edit scripts with embedded tokens of edit scripts.
		String vectorizedEditScripts = Configuration.VECTORIED_EDIT_SCRIPTS;
		FileHelper.deleteFile(vectorizedEditScripts);
		DataPreparation.prepareDataForFeatureLearning();
		
		// Learn features of all selected edit scripts with CNN algorithm.
		// Input data: vectorized edit scripts.
		String extractedFeatures = Configuration.EXTRACTED_FEATURES;
		FileHelper.deleteDirectory(extractedFeatures);
		FeatureLearner learner = new FeatureLearner();
		learner.learnFeatures();
		
		FixPatternCluster fpc = new FixPatternCluster();
		fpc.cluster();
		
		ClusterResult cr = new ClusterResult();
		cr.analyze();
	}

}
