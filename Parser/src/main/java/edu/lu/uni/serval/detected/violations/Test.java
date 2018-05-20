package edu.lu.uni.serval.detected.violations;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.lu.uni.serval.utils.FileHelper;
import edu.lu.uni.serval.utils.ListSorter;
import edu.lu.uni.serval.utils.MapSorter;

public class Test {

	public static void main(String[] args) {
		Map<String, List<ViolationInstance>> actualBugs = new Test().readPatches();
		System.out.println(actualBugs.size());
		
		File xmlFilePath = new File("../data/Defects4J/FindBugs/");
		List<File> xmlFiles = FileHelper.getAllFilesInCurrentDiectory(xmlFilePath, ".xml");
		
		Map<String, ViolationInstance> bugsSourceLine = new HashMap<>();
		Map<String, ViolationInstance> bugsType = new HashMap<>();
		Map<String, ViolationInstance> bugsMethod = new HashMap<>();
		Map<String, ViolationInstance> bugsClass = new HashMap<>();
		
		Map<String, List<ViolationInstance>> fixedBugTypes = new HashMap<>();
		for (File xmlFile : xmlFiles) {
			XmlParser xmlParser = new XmlParser();
			xmlParser.parserXml(xmlFile.getPath()); // 21
			ViolationCollection bugCollection = xmlParser.getBugCollection();
			ViolationInstance matchedBug = matchBug(actualBugs.get(bugCollection.getProjectName()), bugCollection);
			if (matchedBug != null) {
				bugsSourceLine.put(xmlFile.getName(), matchedBug);
			}
			
			// fixed bug types
			/*
			 * NP_ALWAYS_NULL
			 * NP_NULL_ON_SOME_PATH
			 * FE_FLOATING_POINT_EQUALITY
			 */
			List<ViolationInstance> violations = bugCollection.getBugInstances();
			if (!xmlFile.getName().startsWith("Chart-")) {
				for (ViolationInstance violation : violations) {
					String type = violation.getType();// NP_ALWAYS_NULL, DLS_DEAD_LOCAL_STORE
					if ("NP_NULL_ON_SOME_PATH".equals(type)){// || "NP_NULL_ON_SOME_PATH".equals(type) ||"FE_FLOATING_POINT_EQUALITY".equals(type)) {
						if (fixedBugTypes.containsKey(xmlFile.getName())) {
							fixedBugTypes.get(xmlFile.getName()).add(violation);
						} else {
							List<ViolationInstance> vlist = new ArrayList<>();
							vlist.add(violation);
							fixedBugTypes.put(xmlFile.getName(), vlist);
						}
					}
				}
			}
			for (ViolationInstance violation : violations) {
				String type = violation.getType();
				if ("NP_NULL_ON_SOME_PATH".equals(type)){// || "NP_NULL_ON_SOME_PATH".equals(type) ||"FE_FLOATING_POINT_EQUALITY".equals(type)) {
					if (fixedBugTypes.containsKey(xmlFile.getName())) {
						fixedBugTypes.get(xmlFile.getName()).add(violation);
					} else {
						List<ViolationInstance> vlist = new ArrayList<>();
						vlist.add(violation);
						fixedBugTypes.put(xmlFile.getName(), vlist);
					}
				}
			}
			
			
			xmlParser = new XmlParser();
			xmlParser.parserXml(xmlFile, "Type"); // 
			ViolationCollection typesBugCollection = xmlParser.getBugCollection();
			ViolationInstance matchedBugT = matchBug(actualBugs.get(typesBugCollection.getProjectName()), typesBugCollection);
			if (matchedBugT != null) {
				bugsType.put(xmlFile.getName(), matchedBugT);
			}
			
			xmlParser = new XmlParser();
			xmlParser.parserXml(xmlFile, "Method"); // 21
			ViolationCollection methodsBugCollection = xmlParser.getBugCollection();
			ViolationInstance matchedBugM = matchBug(actualBugs.get(methodsBugCollection.getProjectName()), methodsBugCollection);
			if (matchedBugM != null) {
				bugsMethod.put(xmlFile.getName(), matchedBugM);
			}
			
			
			xmlParser = new XmlParser();
			xmlParser.parserXml(xmlFile, "Class"); // 21
			ViolationCollection classesBugCollection = xmlParser.getBugCollection();
			ViolationInstance matchedBugC = matchBug(actualBugs.get(classesBugCollection.getProjectName()), classesBugCollection);
			if (matchedBugC != null) {
				bugsClass.put(xmlFile.getName(), matchedBugC);
			}
		}
		
		Map<String, ViolationInstance> bugsType_2 = removeRepeatedBugs(bugsType, bugsMethod, bugsClass);
		Map<String, ViolationInstance> bugsMethod_2 = removeRepeatedBugs(bugsMethod, bugsType, bugsClass);
		Map<String, ViolationInstance> bugsClass_2 = removeRepeatedBugs(bugsClass, bugsMethod, bugsType);
		
		outputMatchedBugs(bugsSourceLine, "SourceLine");
		outputMatchedBugs(bugsType, "Type");
		outputMatchedBugs(bugsMethod, "Method");
		outputMatchedBugs(bugsClass, "Class");
		outputMatchedBugs(bugsType_2, "Type");
		outputMatchedBugs(bugsMethod_2, "Method");
		outputMatchedBugs(bugsClass_2, "Class");
		
		List<String> matchedBugs = new ArrayList<>();
		matchedBugs.addAll(getMatchedBugs(bugsSourceLine));
		matchedBugs.addAll(getMatchedBugs(bugsType));
		matchedBugs.addAll(getMatchedBugs(bugsMethod));
		matchedBugs.addAll(getMatchedBugs(bugsClass));
		ListSorter<String> sorter = new ListSorter<String>(matchedBugs);
		matchedBugs = sorter.sortAscending();
		for (String bug : matchedBugs) {
			System.out.println(bug);
		}
		System.out.println(matchedBugs.size());
		
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<String, List<ViolationInstance>> entry : fixedBugTypes.entrySet()) {
			List<ViolationInstance> violations = entry.getValue();
			for (ViolationInstance v : violations) {
				builder.append(entry.getKey()).append(" : ").append(v.toString()).append("\n");
			}
		}
		FileHelper.outputToFile("inputData/bugs.list", builder, false);
	}
	
	private static Collection<? extends String> getMatchedBugs(Map<String, ViolationInstance> bugs) {
		List<String> matchedBugs = new ArrayList<>();
		for (Map.Entry<String, ViolationInstance> entry : bugs.entrySet()) {
			ViolationInstance bug = entry.getValue();
			matchedBugs.add(entry.getKey() +  " : " + bug.getType() + " : " + 
					bug.getSourcePath() + " : " + bug.getStartOfSourceLine() + " : " + bug.getEndOfSourceLine());
		}
		return matchedBugs;
	}

	private static void outputMatchedBugs(Map<String, ViolationInstance> bugs, String type) {
		System.out.println(type);
		System.out.println(bugs.size());
		MapSorter<String, ViolationInstance> sorter = new MapSorter<String, ViolationInstance>();
		bugs = sorter.sortByKeyAscending(bugs);
		for (Map.Entry<String, ViolationInstance> entry : bugs.entrySet()) {
//			System.out.println(entry.getKey());
//			BugInstance bug = entry.getValue();
//			System.out.println(bug.getType());
//			System.out.println(bug.getSourcePath());
//			System.out.println(bug.getStartOfSourceLine() + " --> " + bug.getEndOfSourceLine() + "\n\n\n");
			ViolationInstance bug = entry.getValue();
			System.out.println(bug.getType() + " : " + entry.getKey() +  " : " + 
					bug.getSourcePath() + " : " + bug.getStartOfSourceLine() + " : " + bug.getEndOfSourceLine());
		}
	}

	private static Map<String, ViolationInstance> removeRepeatedBugs(Map<String, ViolationInstance> bugs1,
			Map<String, ViolationInstance> bugs2, Map<String, ViolationInstance> bugs3) {
		Map<String, ViolationInstance> bugs = new HashMap<>();
		for (Map.Entry<String, ViolationInstance> entry : bugs1.entrySet()) {
			String key = entry.getKey();
			ViolationInstance bug = entry.getValue();
			
			if (bugs2.containsKey(key)) {
				ViolationInstance bug2 = bugs2.get(key);
				if (bug2.getEndOfSourceLine() - bug2.getStartOfSourceLine() < bug.getEndOfSourceLine() - bug.getStartOfSourceLine()) {
					continue;
				}
			}
			
			if (bugs3.containsKey(key)) {
				ViolationInstance bug3 = bugs3.get(key);
				if (bug3.getEndOfSourceLine() - bug3.getStartOfSourceLine() < bug.getEndOfSourceLine() - bug.getStartOfSourceLine()) {
					continue;
				}
			}
			
			bugs.put(key, bug);
		}
		return bugs;
	}

	private static ViolationInstance matchBug(List<ViolationInstance> actualBugs, ViolationCollection bugCollection) {
		for (ViolationInstance violation : bugCollection.getBugInstances()) {
			String fileName = violation.getSourcePath();
			int startLine = violation.getStartOfSourceLine();
			int endLine = violation.getEndOfSourceLine();
			for (ViolationInstance actualBug : actualBugs) {
				String bugFile = actualBug.getSourcePath();
				if (bugFile.endsWith(fileName)) {
					if (startLine > actualBug.getEndOfSourceLine() || endLine < actualBug.getStartOfSourceLine()) {
						continue;
					} else {
						return violation;
					}
				}
			}
		}
		return null;
	}

	public Map<String, List<ViolationInstance>> readPatches() {
		Map<String, List<ViolationInstance>> bugs = new HashMap<>();
		String patchesPath = "../defects4j/framework/projects/";
		File path = new File(patchesPath);
		File[] projects = path.listFiles();
		for (File project : projects) {
			if (project.isDirectory()) {
				String projectName = project.getName();
				if (projectName.equals("lib")) continue;
				File patchesFileFloder = new File(project.getPath() + "/patches/");
				File[] patchFiles = patchesFileFloder.listFiles();
				for (File patchFile : patchFiles) {
					String fileName = patchFile.getName();
					if (fileName.contains(".test.")) continue;
					
					List<ViolationInstance> bugInstances = new ReadBugLocalization().readLocalization(patchFile);
					
					String bugFile = projectName + "_" + fileName.substring(0, fileName.indexOf("."));
					bugs.put(bugFile, bugInstances);
				}
			}
		}

		return bugs;
	}
}
