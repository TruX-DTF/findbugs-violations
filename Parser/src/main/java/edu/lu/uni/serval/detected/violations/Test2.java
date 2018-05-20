package edu.lu.uni.serval.detected.violations;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import edu.lu.uni.serval.utils.FileHelper;
import edu.lu.uni.serval.utils.ListSorter;

public class Test2 {
	public static void main(String[] args) throws IOException {
		String projectName = "Activiti-Activiti";
		List<ViolationInstance> readUnfixedViolations = readViolations("../data/Unfixed-Violations/unfixed-"+ projectName + ".csv");
		
		File xmlFilePath = new File("../data/LiveStudy/");
		
		XmlParser xmlParser = new XmlParser();
		xmlParser.parserXml(xmlFilePath);
		ViolationCollection bugCollection = xmlParser.getBugCollection();
		List<ViolationInstance> remainedUnfixedViolations = bugCollection.getBugInstances();
		
		readUnfixedViolations.removeAll(remainedUnfixedViolations);
		
		outputBugInstances(remainedUnfixedViolations, "../data/ViolationsComparison/" + projectName + "_unfixedViolations.list");
		outputBugInstances(readUnfixedViolations, "../data/ViolationsComparison/" + projectName + "_fixedViolations.list");
	}

	private static void outputBugInstances(List<ViolationInstance> violations, String outputFileName) {
		StringBuilder builder = new StringBuilder();
		for (ViolationInstance instance : violations) {
			builder.append(instance.toString() + "\n");
//			System.out.println(bug.getType() + " : " + entry.getKey() +  " : " + 
//					bug.getSourcePath() + " : " + bug.getStartOfSourceLine() + " : " + bug.getEndOfSourceLine());
		}
		FileHelper.outputToFile(outputFileName, builder, false);
	}

	
	private static List<ViolationInstance> readViolations(String fileName) throws IOException {
		String content = FileHelper.readFile(fileName);
		BufferedReader reader = new BufferedReader(new StringReader(content));
		String line = null;
		List<ViolationInstance> bugInstances = new ArrayList<>();
		List<String> violationTypes = new ArrayList<>();
		while ((line = reader.readLine()) != null) {
		
			String[] buggyElements = line.split(",");

			String violationType = buggyElements[0];
//			String projectName = buggyElements[1];
			String buggyFile = buggyElements[3];
			int startLine = Integer.parseInt(buggyElements[4]);
			int endLine = Integer.parseInt(buggyElements[5]);
			
			ViolationInstance bugInstance = new ViolationInstance();
			bugInstance.setType(violationType);
			bugInstance.setStartOfSourceLine(startLine);
			bugInstance.setEndOfSourceLine(endLine);
			bugInstance.setSourcePath(buggyFile);
			bugInstances.add(bugInstance);
			
			if (!violationTypes.contains(violationType)) {
				violationTypes.add(violationType);
			}
		}
		reader.close();
		
		ListSorter<String> sorter = new ListSorter<String>(violationTypes);
		violationTypes = sorter.sortAscending();
		for (String type : violationTypes) {
			System.out.println(type);
		}
		return bugInstances;
	}
}
