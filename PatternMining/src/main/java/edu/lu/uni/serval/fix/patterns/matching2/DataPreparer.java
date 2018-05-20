package edu.lu.uni.serval.fix.patterns.matching2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import edu.lu.uni.serval.config.Configuration;
import edu.lu.uni.serval.utils.FileHelper;

/**
 * Select source code tokens of fixed violations.
 * @author kui.liu
 *
 */
public class DataPreparer {

	public void prepare() throws IOException {
		
		String outputPath = Configuration.ROOT_PATH + "Fix_unfixedViolations/";
		FileHelper.deleteDirectory(outputPath);
		
		int maxSize = 40;
		
		String alarmPatchFile = Configuration.ROOT_PATH + "MiningInput/Embedding/tokens.list";
		String content = FileHelper.readFile(alarmPatchFile);
		BufferedReader reader = new BufferedReader(new StringReader(content));
		String line = null;
		int index = -1;
		
		StringBuilder tokenBuilder = new StringBuilder();
		StringBuilder indexBuilder = new StringBuilder();
		while ((line = reader.readLine()) != null) {
			index ++;
			String[] tokens = line.split(" ");
			if (tokens.length <= maxSize) {
				tokenBuilder.append(line + "\n");
				indexBuilder.append(index + "\n");
			}
		}
		reader.close();
		
		FileHelper.outputToFile(outputPath + "fixedAlarms.list", tokenBuilder, false);
		FileHelper.outputToFile(outputPath + "fixedAlarmsIndex.list", indexBuilder, false);
	}

}
