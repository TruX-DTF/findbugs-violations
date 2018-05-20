package edu.lu.uni.bug.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import edu.lu.uni.serval.utils.FileHelper;
import edu.lu.uni.serval.utils.ListSorter;
import edu.lu.uni.serval.violations.info.ViolationCollection;
import edu.lu.uni.serval.violations.info.ViolationInstance;
import edu.lu.uni.serval.violations.xml.parser.XmlParser;

/**
 * Read FindBugs results.
 * 
 * @author kui.liu
 *
 */
public class Test2 {
	public static void main(String[] args) throws IOException {
		String projectName = "Activiti-Activiti";
		List<ViolationInstance> readUnfixedAlarms = readAlarms("../FixPatternminer/Dataset/Unfixed-Alarms/unfixed-"+ projectName + ".csv");
		
		File xmlFilePath = new File("../FPM_Violations/LiveStudy/");
		
		XmlParser xmlParser = new XmlParser();
		xmlParser.parserXml(xmlFilePath);
		ViolationCollection bugCollection = xmlParser.getBugCollection();
		List<ViolationInstance> remainedUnfixedAlarms = bugCollection.getBugInstances();
		
		readUnfixedAlarms.removeAll(remainedUnfixedAlarms);
		
		outputBugInstances(remainedUnfixedAlarms, "../FPM_Violations/AlarmsComparison/" + projectName + "_unfixedAlarms.list");
		outputBugInstances(readUnfixedAlarms, "../FPM_Violations/AlarmsComparison/" + projectName + "_fixedAlarms.list");
	}

	private static void outputBugInstances(List<ViolationInstance> alarms, String outputFileName) {
		StringBuilder builder = new StringBuilder();
		for (ViolationInstance instance : alarms) {
			builder.append(instance.toString() + "\n");
//			System.out.println(bug.getType() + " : " + entry.getKey() +  " : " + 
//					bug.getSourcePath() + " : " + bug.getStartOfSourceLine() + " : " + bug.getEndOfSourceLine());
		}
		FileHelper.outputToFile(outputFileName, builder, false);
	}

	
	private static List<ViolationInstance> readAlarms(String fileName) throws IOException {
		String content = FileHelper.readFile(fileName);
		BufferedReader reader = new BufferedReader(new StringReader(content));
		String line = null;
		List<ViolationInstance> bugInstances = new ArrayList<>();
		List<String> alarmTypes = new ArrayList<>();
		while ((line = reader.readLine()) != null) {
		
			String[] buggyElements = line.split(",");

			String alarmType = buggyElements[0];
//			String projectName = buggyElements[1];
			String buggyFile = buggyElements[3];
			int startLine = Integer.parseInt(buggyElements[4]);
			int endLine = Integer.parseInt(buggyElements[5]);
			
			ViolationInstance bugInstance = new ViolationInstance();
			bugInstance.setType(alarmType);
			bugInstance.setStartOfSourceLine(startLine);
			bugInstance.setEndOfSourceLine(endLine);
			bugInstance.setSourcePath(buggyFile);
			bugInstances.add(bugInstance);
			
			if (!alarmTypes.contains(alarmType)) {
				alarmTypes.add(alarmType);
			}
		}
		reader.close();
		
		ListSorter<String> sorter = new ListSorter<String>(alarmTypes);
		alarmTypes = sorter.sortAscending();
		for (String type : alarmTypes) {
			System.out.println(type);
		}
		return bugInstances;
	}
}
