package edu.lu.uni.serval.detected.violations;

import java.io.File;
import java.io.IOException;
import java.util.List;

import edu.lu.uni.serval.utils.FileHelper;

/**
 * Parse and Format FindBugs results.
 * 
 * @author kui.liu
 *
 */
public class FindBugs {
	
	public static void main(String[] args) {
		// Defects4j
		String xmlFilePath = "../data/Defects4J/FindBugs/"; 
		String outputPath = "../data/Defects4J/BugsList/";
		try {
			new FindBugs().parse(xmlFilePath, outputPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// LiveStudy
		String xmlFilePath_ = "../data/LiveStudy/FindBugs/";
		String outputPath_ = "../data/LiveStudy/BugsList/";
		try {
			new FindBugs().parse(xmlFilePath_, outputPath_);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void parse(String xmlFilePath, String outputPath) throws IOException {
		List<File> xmlFiles = FileHelper.getAllFilesInCurrentDiectory(xmlFilePath, ".xml");
		
		int i = 0;
		for (File xmlFile : xmlFiles) {
			StringBuilder builder = new StringBuilder();
			XmlParser xmlParser = new XmlParser();
			xmlParser.parserXml(xmlFile);
			ViolationCollection bugCollection = xmlParser.getBugCollection();
			
			String projectName = bugCollection.getProjectName();
			List<ViolationInstance> violations = bugCollection.getBugInstances();
			for (ViolationInstance violation : violations) {
				builder.append(projectName + " : " + violation.toString() + "\n");
				i ++;
			}
			
			FileHelper.outputToFile(outputPath + projectName + ".list", builder, false);
			builder.setLength(0);
		}
		System.out.println(i);
	}

}
