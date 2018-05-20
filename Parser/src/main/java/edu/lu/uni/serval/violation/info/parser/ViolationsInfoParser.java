package edu.lu.uni.serval.violation.info.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.lu.uni.serval.config.Configuration;
import edu.lu.uni.serval.git.exception.GitRepositoryNotFoundException;
import edu.lu.uni.serval.git.exception.NotValidGitRepositoryException;
import edu.lu.uni.serval.git.travel.GitRepository;
import edu.lu.uni.serval.utils.Exporter;
import edu.lu.uni.serval.utils.FileHelper;
import edu.lu.uni.serval.utils.MapSorter;
import edu.lu.uni.serval.violation.Violation;

/**
 * Travel Git repositories to get the violation information.
 * 		Violation-lcalized Java files, 
 * 		Violation-removed java files, 
 * 		violation types and positions.
 * 
 * @author kui.liu
 *
 */
public class ViolationsInfoParser {
	
	private static Logger log = LoggerFactory.getLogger(ViolationsInfoReader.class);
	
	Map<String, Integer> violationTypesCounter = new HashMap<>();
	
	/**
	 * Travel Git repositories to get the violation-localized java files, violation-removed java files, violation types and positions.
	 * 
	 * @param fixedViolationFile
	 * @param repos
	 * @param previousFilesPath
	 * @param revisedFilesPath
	 * @param positionsFilePath
	 * @param diffentryFilePath
	 */
	public void parseFixedViolationsInfo(String fixedViolationFile, List<File> repos, String previousFilesPath, String revisedFilesPath, String positionsFilePath, String diffentryFilePath) {
		ViolationsInfoReader reader = new ViolationsInfoReader();
		Map<String, List<Violation>> violationsMap = reader.readFixedViolationsInfo(fixedViolationFile);
		List<String> throwExpProjs = new ArrayList<>();
		int a = 0;
		int exceptionsCounter = 0;
		int violationsAmount = 0;
//		System.out.println(violations.size());
		for (Map.Entry<String , List<Violation>> entry : violationsMap.entrySet()) {
			String projectName = entry.getKey();
			String repoName = "";
			for (File repo : repos) {
				if (repo.getName().equals(projectName)) {
					repoName = repo.getPath() + "/";
					break;
				}
			}
			if ("".equals(repoName)) {
				a ++;
				System.out.println(projectName);
				continue;
			}
			List<Violation> violations = entry.getValue();
			
			String repoPath = repoName + "/.git";
			GitRepository gitRepo = new GitRepository(repoPath, revisedFilesPath, previousFilesPath);
			try {
				gitRepo.open();
				for (Violation violation : violations) {
					String buggyCommitId = violation.getBuggyCommitId();
					String buggyFileName = violation.getBuggyFileName();
					String buggyFileContent = gitRepo.getFileContentByCommitIdAndFileName(buggyCommitId, buggyFileName);
					if (buggyFileContent == null || "".equals(buggyFileContent)) {
//						System.out.println(projectName);
						throwExpProjs.add(projectName);
						exceptionsCounter ++;
						continue;
					}
					
					String fixedCommitId = violation.getFixedCommitId();
					String fixedFileName = violation.getFixedFileName();
					String fixedFileContent = gitRepo.getFileContentByCommitIdAndFileName(fixedCommitId, fixedFileName);
					if (fixedFileContent == null || "".equals(fixedFileContent)) {
//						System.out.println(projectName);
						throwExpProjs.add(projectName);
						exceptionsCounter ++;
						continue;
					}
					
					String diffentry = gitRepo.getDiffentryByTwoCommitIds(buggyCommitId, fixedCommitId, fixedFileName);
					if (diffentry == null) {
//						System.out.println(projectName);
						throwExpProjs.add(projectName);
						exceptionsCounter ++;
						continue;
					}
					
					String commitId = buggyCommitId.substring(0, 6) + "_" + fixedCommitId.substring(0, 6);
					String fileName = fixedFileName.replaceAll("/", "#");
					fileName = projectName + "_" + commitId + fileName;
					if (fileName.length() > 240) {
						List<File> files = FileHelper.getAllFilesInCurrentDiectory(revisedFilesPath, ".java");
						fileName = files.size() + "TooLongFileName.java";
					}
					String buggyFile = previousFilesPath + "prev_" + fileName;
					String fixedFile = revisedFilesPath + fileName;
					fileName = fileName.replace(".java", ".txt");
					String positionFile = positionsFilePath + fileName;
					String diffentryFile = diffentryFilePath + fileName;
					FileHelper.outputToFile(buggyFile, buggyFileContent, false);
					FileHelper.outputToFile(fixedFile, fixedFileContent, false);
//					FileHelper.outputToFile(positionFile, readPosition(violation.getPositions(), violation.getViolationTypes()), false);
					FileHelper.outputToFile(positionFile, readViolationTypeAndPosition(violation.getViolationTypesAndPositions()), false);
					FileHelper.outputToFile(diffentryFile, diffentry, false);

					violationsAmount += counter(violation);
				}
			} catch (GitRepositoryNotFoundException e) {
				System.out.println("Exception: " + projectName);
				exceptionsCounter ++;
				e.printStackTrace();
			} catch (NotValidGitRepositoryException e) {
				System.out.println("Exception: " + projectName);
				exceptionsCounter ++;
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("Exception: " + projectName);
				exceptionsCounter ++;
				e.printStackTrace();
			} catch (RevisionSyntaxException e) {
				System.out.println("Exception: " + projectName);
				exceptionsCounter ++;
				e.printStackTrace();
			} finally {
				gitRepo.close();
			}
		}
		System.out.println("Empty project name: " + a);
		System.out.println("Failed to get java files: " + exceptionsCounter);
		System.out.println("Failed to get diffentries: " +  throwExpProjs.size());
		System.out.println("Failed to get diffentries: " + throwExpProjs);
		
		System.out.println("### Violations amount: " + violationsAmount);
		
		MapSorter<String, Integer> sorter = new MapSorter<String, Integer>();
		violationTypesCounter = sorter.sortByKeyAscending(violationTypesCounter);
		String[] columns = { "Violation Type", "amount" };
		Exporter.exportOutliers(violationTypesCounter, new File(Configuration.GUM_TREE_INPUT + "FixedViolationTypes.xls"), 1, columns);
	}
	
	private int counter(Violation violation) {
		int counter = 0;
//		Map<Integer, String> violationTypes = violation.getViolationTypes();
//		counter += violationTypes.size();
//		for (Map.Entry<Integer, String> entry : violationTypes.entrySet()) {
//			String type = entry.getValue();
//			if (this.violationTypesCounter.containsKey(entry.getValue())) {
//				this.violationTypesCounter.put(type, this.violationTypesCounter.get(type) + 1);
//			} else {
//				this.violationTypesCounter.put(type, 1);
//			}
//		}
		
		List<String> violationTypesAndPositions = violation.getViolationTypesAndPositions();
		for (String violationTypeAndPosition : violationTypesAndPositions) {
			String[] elements = violationTypeAndPosition.split(":");
			String violationType = elements[0];
			if (this.violationTypesCounter.containsKey(violationType)) {
				this.violationTypesCounter.put(violationType, this.violationTypesCounter.get(violationType) + 1);
			} else {
				this.violationTypesCounter.put(violationType, 1);
			}
		}
		counter += violationTypesAndPositions.size();
		return counter;
	}

	/**
	 * Travel Git repositories to get the violation-localized java files, violation types and positions.
	 * 
	 * @param violationInfoFile
	 * @param repos
	 * @param previousFilesPath
	 * @param revisedFilesPath
	 * @param positionsFilePath
	 * @param diffentryFilePath
	 */
	public void parseUnfixedViolationsInfo(File violationInfoFile, List<File> repos, String violationFilesPath, String positionsFilePath) {
		ViolationsInfoReader reader = new ViolationsInfoReader();
		Map<String, List<Violation>> violationsMap = reader.readUnfixedViolationsInfo(violationInfoFile.getPath());
		int a = 0;
		for (Map.Entry<String , List<Violation>> entry : violationsMap.entrySet()) {
			String projectName = entry.getKey();
			String repoName = "";
			for (File repo : repos) {
				if (repo.getName().equals(projectName)) {
					repoName = repo.getPath() + "/";
					break;
				}
			}
			if ("".equals(repoName)) {
				a ++;
				log.info("Unfound project: " + projectName);
				continue;
			}
			List<Violation> violations = entry.getValue();
			
			String repoPath = repoName + "/.git";
			GitRepository gitRepo = new GitRepository(repoPath, "", "");
			try {
				gitRepo.open();
				for (Violation violation : violations) {
					String buggyCommitId = violation.getBuggyCommitId();
					String buggyFileName = violation.getBuggyFileName();
					String buggyFileContent = gitRepo.getFileContentByCommitIdAndFileName(buggyCommitId, buggyFileName);
					if (buggyFileContent == null || "".equals(buggyFileContent)) {
						log.info("Empty code file: " + projectName);
						continue;
					}
					
					String commitId = buggyCommitId.substring(0, 6) + "_";
					String fileName = buggyFileName.replaceAll("/", "#");
					fileName = projectName + "_" + commitId + fileName;
					if (fileName.length() > 240) {
						List<File> files = FileHelper.getAllFilesInCurrentDiectory(violationFilesPath, ".java");
						fileName = files.size() + "TooLongFileName.java";
					}
					String buggyFile = violationFilesPath + "unfixed_" + fileName;
					fileName = fileName.replace(".java", ".txt");
					String positionFile = positionsFilePath + fileName;
					FileHelper.outputToFile(buggyFile, buggyFileContent, false);
					FileHelper.outputToFile(positionFile, readViolationTypeAndPosition(violation.getViolationTypesAndPositions()), false);
				}
			} catch (GitRepositoryNotFoundException e) {
				log.error("Exception: " + projectName);
				e.printStackTrace();
			} catch (NotValidGitRepositoryException e) {
				log.error("Exception: " + projectName);
				e.printStackTrace();
			} catch (IOException e) {
				log.error("Exception: " + projectName);
				e.printStackTrace();
			} catch (RevisionSyntaxException e) {
				log.error("Exception: " + projectName);
				e.printStackTrace();
			} finally {
				gitRepo.close();
			}
		}
		log.info("Empty project name: " + a);
	}

	private String readViolationTypeAndPosition(List<String> violationTypesAndPositions) {
		String positionsStr = "";
		for (String element : violationTypesAndPositions) {
			positionsStr += element + "\n";
		}
		return positionsStr;
	}
	
	@SuppressWarnings("unused")
	@Deprecated
	private String readViolationTypeAndPosition(Map<Integer, Integer> positions, Map<Integer, String> violationTypes) {
		String positionsStr = "";
		for (Map.Entry<Integer, String> entry : violationTypes.entrySet()) {
			int key = entry.getKey();
			positionsStr += key + ":" + positions.get(key) + ":" + entry.getValue() + "\n";
		}
		return positionsStr;
	}
	
}
