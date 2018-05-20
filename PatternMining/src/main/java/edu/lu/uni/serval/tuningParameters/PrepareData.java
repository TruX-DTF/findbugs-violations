package edu.lu.uni.serval.tuningParameters;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import edu.lu.uni.serval.mining.utils.DataPreparation;
import edu.lu.uni.serval.utils.FileHelper;

public class PrepareData {

	public static void main(String[] args) throws IOException {
		/*
a <- read.csv("~/Public/git/FPM_Violations/TuneParameters/1.csv", header=F)
pdf(file="~/Public/git/FPM_Violations/TuneParameters/1.pdf")
boxplot(a$V1, outline=F,names=c('sizes'), do.conf=T, do.out=T, range=1.5, varwidth = T, horizontal=F,  main="Sizes Distribution of Edit scripts", xlab="", ylab="Size")
dev.off()

 Min.   :   2.00  
 1st Qu.:   8.00  
 Median :  10.00  
 Mean   :  17.57  
 3rd Qu.:  16.00  
 Max.   :1584.00 
 
 
 28
 
 
		 */
//		Map<String, Integer> alarmTypes = new HashMap<>();
//		
//		String fixedAlarms = "../FPM_Violations/TuneParameters/fixedAlarmTokens.list";
//		FileInputStream fis = new FileInputStream(fixedAlarms);
//		Scanner scanner = new Scanner(fis);
//		while (scanner.hasNextLine()) {
//			String line = scanner.nextLine();
//			
//			int indexOfColon = line.indexOf(":");
//			String alarmType = line.substring(0, indexOfColon);
//			line = line.substring(indexOfColon + 1);
//			indexOfColon = line.indexOf(":");
//			line = line.substring(indexOfColon + 1);
//			indexOfColon = line.indexOf(":");
//			line = line.substring(indexOfColon + 1);
//			indexOfColon = line.indexOf(":");
//			String tokens = line.substring(indexOfColon + 1);
//			if (alarmTypes.containsKey(alarmType)) {
//				alarmTypes.put(alarmType, alarmTypes.get(alarmType) + 1);
//			} else {
//				alarmTypes.put(alarmType, 1);
//			}
//		}
//		scanner.close();
//		fis.close();
		
//		fixedAlarms = "../FPM_Violations/TuneParameters/unfixedAlarmTokens.list";
//		fis = new FileInputStream(fixedAlarms);
//		scanner = new Scanner(fis);
//		while (scanner.hasNextLine()) {
//			String line = scanner.nextLine();
//			
//			int indexOfColon = line.indexOf(":");
//			String alarmType = line.substring(0, indexOfColon);
//			line = line.substring(indexOfColon + 1);
//			indexOfColon = line.indexOf(":");
//			line = line.substring(indexOfColon + 1);
//			indexOfColon = line.indexOf(":");
//			line = line.substring(indexOfColon + 1);
//			indexOfColon = line.indexOf(":");
//			String tokens = line.substring(indexOfColon + 1);
//			if (alarmTypes.containsKey(alarmType)) {
//				alarmTypes.put(alarmType, alarmTypes.get(alarmType) + 1);
//			} else {
//				alarmTypes.put(alarmType, 1);
//			}
//		}
//		scanner.close();
//		fis.close();
//		String[] columns = {"AlarmType", "Count"};
//		File file = new File("../FPM_Violations/TuneParameters/alarmType.xls");
//		Exporter.exportOutliers(alarmTypes, file, 1, columns);
		
		
		List<String> alarmTypes = DataPreparation.readStringList("Dataset/Types.list");
		Map<String, Integer> alarms = new HashMap<>();
		
		StringBuilder labelsBuilder = new StringBuilder();
		StringBuilder tokensBuilder = new StringBuilder();
		
		String fixedAlarms = "../FPM_Violations/TuneParameters/fixedAlarmTokens.list";
		FileInputStream fis = new FileInputStream(fixedAlarms);
		Scanner scanner = new Scanner(fis);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			int indexOfColon = line.indexOf(":");
			String alarmType = line.substring(0, indexOfColon);
			if (!alarmTypes.contains(alarmType)) continue;
			
			line = line.substring(indexOfColon + 1);
			indexOfColon = line.indexOf(":");
			line = line.substring(indexOfColon + 1);
			indexOfColon = line.indexOf(":");
			line = line.substring(indexOfColon + 1);
			indexOfColon = line.indexOf(":");
			String tokens = line.substring(indexOfColon + 1);
			String[] tokensArray = tokens.split(" ");
			
			if (tokensArray.length <= 28) {
				Integer counter = alarms.get(alarmType);
				if (counter == null || counter < 1000) {
					if (alarms.containsKey(alarmType)) {
						alarms.put(alarmType, counter + 1);
					} else {
						alarms.put(alarmType, 1);
					}
					
					int label = alarmTypes.indexOf(alarmType);
					labelsBuilder.append(label + "\n");
					tokensBuilder.append(tokens + "\n");
				}
			}
		}
		scanner.close();
		fis.close();
		
		fixedAlarms = "../FPM_Violations/TuneParameters/unfixedAlarmTokens.list";
		fis = new FileInputStream(fixedAlarms);
		scanner = new Scanner(fis);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			int indexOfColon = line.indexOf(":");
			String alarmType = line.substring(0, indexOfColon);
			if (!alarmTypes.contains(alarmType)) continue;
			
			line = line.substring(indexOfColon + 1);
			indexOfColon = line.indexOf(":");
			line = line.substring(indexOfColon + 1);
			indexOfColon = line.indexOf(":");
			line = line.substring(indexOfColon + 1);
			indexOfColon = line.indexOf(":");
			String tokens = line.substring(indexOfColon + 1);
			String[] tokensArray = tokens.split(" ");
			
			if (tokensArray.length <= 28) {
				Integer counter = alarms.get(alarmType);
				if (counter == null || counter < 1000) {
					if (alarms.containsKey(alarmType)) {
						alarms.put(alarmType, counter + 1);
					} else {
						alarms.put(alarmType, 1);
					}
					
					int label = alarmTypes.indexOf(alarmType);
					labelsBuilder.append(label + "\n");
					tokensBuilder.append(tokens + "\n");
				}
			}
		}
		scanner.close();
		fis.close();
		
		FileHelper.outputToFile("../FPM_Violations/TuneParameters/selectedTokens.list", tokensBuilder, false);
		FileHelper.outputToFile("../FPM_Violations/TuneParameters/labels.list", labelsBuilder, false);
	}

}
