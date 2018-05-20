package edu.lu.uni.serval.violation.info.parser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.lu.uni.serval.violation.Violation;

/**
 * Read information of violations.
 * 		Violation Type,
 * 		Project name,
 * 		Java file,
 * 		Position: start line and end line.
 * 
 * @author kui.liu
 *
 */
public class ViolationsInfoReader {
	
	private static Logger log = LoggerFactory.getLogger(ViolationsInfoReader.class);

	int counter = 0;
	
	public Map<String, List<Violation>> readFixedViolationsInfo(String fileName) {
		Map<String, List<Violation>> violationsMap = new HashMap<>();
		FileInputStream fis = null;
		Scanner scanner = null;
		try {
			fis = new FileInputStream(fileName);
			scanner = new Scanner(fis);
			
			while (scanner.hasNextLine()) {
				/*
				 * ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD:
				 * spotify-cassandra-reaper:
				 * 3fd866c688fae8c78f1628903cb6808a8a9e530a:
				 * src/main/java/com/spotify/reaper/ReaperApplication.java:
				 * 62:
				 * 62
				 * =>
				 * spotify-cassandra-reaper:6
				 * 1a3d1ca9e6cc133b6d4a1e1c7c1f4f0c4f70305:
				 * src/main/java/com/spotify/reaper/ReaperApplication.java
				 */
				String line = scanner.nextLine();
				int arrowIndex = line.indexOf("=>");
				String buggyInfo = line.substring(0, arrowIndex);
				String fixedInfo = line.substring(arrowIndex + 2);
				String[] buggyElements = buggyInfo.split(":");
				String[] fixedElements = fixedInfo.split(":");

//				String violationType = buggyElements[0];
				String projectName = buggyElements[1];
				String buggyCommitId = buggyElements[2];
				String buggyFile = buggyElements[3];
//				int startLine = Integer.parseInt(buggyElements[4]);
//				int endLine = Integer.parseInt(buggyElements[5]);
				String fixCommitId = fixedElements[1];
				String fixedFile = fixedElements[2];
				
//				if (startLine == -1 || endLine == -1 || endLine == 1) {
////					log.error("FIXED VIOLATION WRONG_POSITION: " + line);
//					builder.append(line + "\n");
//					continue;
//				}
				
				Violation violation = new Violation(buggyCommitId, buggyFile, fixCommitId, fixedFile);
//				String violationTypeAndPosition = buggyElements[0] + ":" + startLine + ":" + endLine;
				String violationTypeAndPosition = buggyElements[0] + ":" + buggyElements[4] + ":" + buggyElements[5];
				
				List<Violation> violations;
				if (violationsMap.containsKey(projectName)) {
					violations = violationsMap.get(projectName);
				} else {
					violations = new ArrayList<>();
					violationsMap.put(projectName, violations);
				}
				int index = violations.indexOf(violation);
				if (index >= 0) {
					Violation indexedViolation = violations.get(index);
//					Map<Integer, Integer> positions = indexedViolation.getPositions();
//					if (positions.containsKey(startLine)) {
//						int end = positions.get(startLine);
//						if (endLine < end) {
//							positions.put(startLine, endLine);
//							indexedViolation.getViolationTypes().put(startLine, violationType);
//						}
//					} else {
//						positions.put(startLine, endLine);
//						indexedViolation.getViolationTypes().put(startLine, violationType);
						counter ++;
//					}
					indexedViolation.getViolationTypesAndPositions().add(violationTypeAndPosition);
				} else {
//					Map<Integer, String> violationTypes = new HashMap<>();
//					violationTypes.put(startLine, violationType);
//					violation.setViolationTypes(violationTypes);
//					Map<Integer, Integer> positions = new HashMap<>();
//					positions.put(startLine, endLine);
//					violation.setPositions(positions);
					List<String> violationTypesAndPositions = new ArrayList<>();
					violationTypesAndPositions.add(violationTypeAndPosition);
					violation.setViolationTypesAndPositions(violationTypesAndPositions);
					violations.add(violation);
					counter ++;
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
		
		log.info("Num of fixed violations: " + counter);
//		FileHelper.outputToFile("../FPM_Violations/TuneParameters/WrongPosition/FixedViolations.list", builder, false);
		return violationsMap;
	}
	
	public Map<String, List<Violation>> readUnfixedViolationsInfo(String fileName) {
		Map<String, List<Violation>> violationsMap = new HashMap<>();
		FileInputStream fis = null;
		Scanner scanner = null;
		try {
			fis = new FileInputStream(fileName);
			scanner = new Scanner(fis);
			
			while (scanner.hasNextLine()) {
				/*
				 * BC_UNCONFIRMED_CAST,
				 * apache-jackrabbit,
				 * 7f4cf1314240a2560e96083fddd1906d4efe63f6,
				 * jackrabbit-core/src/main/java/org/apache/jackrabbit/core/query/lucene/ChildAxisQuery.java,
				 * 336,
				 * 336
				 */
				String line = scanner.nextLine();
				String[] buggyElements = line.split(",");

				String projectName = buggyElements[1];
				String buggyCommitId = buggyElements[2];
				String buggyFile = buggyElements[3];
//				String violationType = buggyElements[0];
//				int startLine = Integer.valueOf(buggyElements[4]);
//				int endLine = Integer.valueOf(buggyElements[5]);
				// Violation type : start line : end line.
				String violationTypeAndPosition = buggyElements[0] + ":" + buggyElements[4] + ":" + buggyElements[5]; 
				
				Violation violation = new Violation(buggyCommitId, buggyFile, "", "");
				
				List<Violation> violations;
				if (violationsMap.containsKey(projectName)) {
					violations = violationsMap.get(projectName);
				} else {
					violations = new ArrayList<>();
					violationsMap.put(projectName, violations);
				}
				int index = violations.indexOf(violation);
				if (index >= 0) {
					Violation indexedViolation = violations.get(index);
//					Map<Integer, Integer> positions = indexedViolation.getPositions();
//					if (positions.containsKey(startLine)) {
//						int end = positions.get(startLine);
//						if (endLine < end) {
//							positions.put(startLine, endLine);
//							indexedViolation.getViolationTypes().put(startLine, violationType);
//						}
//					} else {
//						positions.put(startLine, endLine);
//						indexedViolation.getViolationTypes().put(startLine, violationType);
						counter ++;
//					}
					indexedViolation.getViolationTypesAndPositions().add(violationTypeAndPosition);
				} else {
//					Map<Integer, String> violationTypes = new HashMap<>();
//					violationTypes.put(startLine, violationType);
//					violation.setViolationTypes(violationTypes);
//					Map<Integer, Integer> positions = new HashMap<>();
//					positions.put(startLine, endLine);
//					violation.setPositions(positions);
					List<String> violationTypesAndPositions = new ArrayList<>();
					violationTypesAndPositions.add(violationTypeAndPosition);
					violation.setViolationTypesAndPositions(violationTypesAndPositions);
					violations.add(violation);
					counter ++;
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
		
		log.info("Num of unfixed violations: " + counter);
		return violationsMap;
	}
	
}
