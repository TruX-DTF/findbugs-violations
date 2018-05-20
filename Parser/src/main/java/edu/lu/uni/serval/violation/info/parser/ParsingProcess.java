package edu.lu.uni.serval.violation.info.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.lu.uni.serval.config.Configuration;
import edu.lu.uni.serval.utils.FileHelper;

/**
 * Travel project git repository to get the Java files and positions of violations (fixed and unfixed).
 * 
 * @author kui.liu
 *
 */
public class ParsingProcess {
	
	private static Logger log = LoggerFactory.getLogger(ParsingProcess.class);

	private static final String REPO_PATH = "../data/repos/";// Path of all project repositories
	
	private List<File> repositoriesList = new ArrayList<>();
	
	public void parse() {
		String fixedViolations = "../data/fixed-violation-instances.list";
		String unfixedViolations = "../data/unFixedInstances/";
		ParsingProcess pp = new ParsingProcess();
		pp.readReposityFiles(REPO_PATH);
		pp.parseFixedViolations(fixedViolations);
		log.info("Finish of parsing fixed violations' information.");
		pp.parseUnfixedViolations(unfixedViolations);
	}
	
	public void readReposityFiles(String reposPath) {
		File repositories = new File(reposPath);
		File[] repos = repositories.listFiles();
		for (File repo : repos) {
			if (repo.isDirectory()) {
				if (repo.isDirectory()) {
					repositoriesList.add(repo);
				}
			}
		}
	}
	
	public void parseFixedViolations(String fixedViolationsFile) {
		final String previousFilesPath = Configuration.GUM_TREE_INPUT + "prevFiles/";
		final String revisedFilesPath = Configuration.GUM_TREE_INPUT + "revFiles/";
		final String positionsFilePath = Configuration.GUM_TREE_INPUT + "positions/";
		final String diffentryFilePath = Configuration.GUM_TREE_INPUT + "diffentries/";
		FileHelper.createDirectory(previousFilesPath);
		FileHelper.createDirectory(revisedFilesPath);
		FileHelper.createDirectory(positionsFilePath);
		FileHelper.createDirectory(diffentryFilePath);
		
		ViolationsInfoParser parser = new ViolationsInfoParser();
		parser.parseFixedViolationsInfo(fixedViolationsFile, repositoriesList, previousFilesPath, revisedFilesPath, positionsFilePath, diffentryFilePath);
	}
	
	public void parseUnfixedViolations(String unfixedViolations) {
		// Violation instances of single violation type
		String outputPath = Configuration.UNFIXED_VIOLATIONS_PATH;
		List<File> unfixedViolationFiles = FileHelper.getAllFilesInCurrentDiectory(unfixedViolations, ".list");
		for (File file : unfixedViolationFiles) {
			String fileName = FileHelper.getFileNameWithoutExtension(file);
			String codeFilePath = outputPath + fileName + "/CodeFiles/";
			String positionFilePath = outputPath + fileName + "/Positions/";
			FileHelper.createDirectory(codeFilePath);
			FileHelper.createDirectory(positionFilePath);
			ViolationsInfoParser parser = new ViolationsInfoParser();
			parser.parseUnfixedViolationsInfo(file, repositoriesList,codeFilePath, positionFilePath);
		}
	}

}
