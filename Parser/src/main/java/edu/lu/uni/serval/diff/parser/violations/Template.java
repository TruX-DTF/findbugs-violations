package edu.lu.uni.serval.diff.parser.violations;

import static java.lang.System.err;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import edu.lu.uni.serval.config.Configuration;
import edu.lu.uni.serval.diff.parser.RunnableParser;
import edu.lu.uni.serval.main.violations.MessageFile;

public class Template {

	public static void main(String[] args) {
		// input data
		final List<MessageFile> msgFiles = getMessageFiles(Configuration.GUM_TREE_INPUT);
		System.out.println(msgFiles.size());

		// output path
//		final String editScriptsFilePath = Configuration.EDITSCRIPTS_FILE;
//		final String patchesSourceCodeFilePath = Configuration.PATCH_SOURCECODE_FILE;
//		final String buggyTokensFilePath = Configuration.BUGGY_CODE_TOKENS_FILE;
//		final String editScriptSizesFilePath = Configuration.EDITSCRIPT_SIZES_FILE;
		
		StringBuilder astEditScripts = new StringBuilder();
		StringBuilder tokens = new StringBuilder();
		StringBuilder sizes = new StringBuilder();
		StringBuilder patches = new StringBuilder();
		
		// Read violations with Null_Violation_Hunk or Illegal_Line_Position
//		String filePath = "logs/FixedViolationCodeParseResults.log";
//		List<Violation> uselessViolations = readUselessViolations(filePath);
//		FileHelper.deleteFile("logs/testV1.txt");
//		FileHelper.deleteFile("logs/testV2.txt");
//		FileHelper.deleteFile("logs/testV3.txt");
		
		int a = 0;
		for (MessageFile msgFile : msgFiles) {
			if (!msgFile.getRevFile().getName().equals(
"finmath-finmath-lib_f862e7_ec7d11src#main#java#net#finmath#marketdata#calibration#CalibratedCurves.java")) {
				continue;
			}
			FixedViolationHunkParser parser = new FixedViolationHunkParser();
//			parser.parseFixPatterns(msgFile.getPrevFile(), msgFile.getRevFile(), msgFile.getDiffEntryFile());
//			parser.setUselessViolations(uselessViolations);
			
			final ExecutorService executor = Executors.newSingleThreadExecutor();
			// schedule the work
			final Future<?> future = executor.submit(new RunnableParser(msgFile.getPrevFile(), 
					msgFile.getRevFile(), msgFile.getDiffEntryFile(), parser));
			try {
				// where we wait for task to complete
				future.get(Configuration.SECONDS_TO_WAIT, TimeUnit.SECONDS);
				String editScripts = parser.getAstEditScripts();
				if (editScripts.equals("")) {
					continue;
				}
				astEditScripts.append(editScripts);
				tokens.append(parser.getTokensOfSourceCode());
				sizes.append(parser.getSizes());
				patches.append(parser.getPatchesSourceCode());

				a ++;
				if (a % 100 == 0) {
//					FileHelper.outputToFile(editScriptsFilePath, astEditScripts, true);
//					FileHelper.outputToFile(buggyTokensFilePath, tokens, true);
//					FileHelper.outputToFile(editScriptSizesFilePath, sizes, true);
//					FileHelper.outputToFile(patchesSourceCodeFilePath, patches, true);
					astEditScripts.setLength(0);
					tokens.setLength(0);
					sizes.setLength(0);
					patches.setLength(0);
				}
			} catch (TimeoutException e) {
				err.println("task timed out");
				future.cancel(true /* mayInterruptIfRunning */ );
			} catch (InterruptedException e) {
				err.println("task interrupted");
			} catch (ExecutionException e) {
				err.println("task aborted");
			} finally {
				executor.shutdownNow();
			}
		}
		
//		FileHelper.outputToFile(editScriptsFilePath, astEditScripts, true);
//		FileHelper.outputToFile(buggyTokensFilePath, tokens, true);
//		FileHelper.outputToFile(editScriptSizesFilePath, sizes, true);
//		FileHelper.outputToFile(patchesSourceCodeFilePath, patches, true);
		System.out.println(a);
	}
	

//	private static List<Violation> readUselessViolations(String filePath) {
//		List<Violation> uselessViolations = new ArrayList<>();
//		
//		String content = FileHelper.readFile(filePath);
//		BufferedReader reader = new BufferedReader(new StringReader(content));
//		String line = null;
//		try {
//			while ((line = reader.readLine()) != null) {
//				if (line.startsWith("#")) {
//					String[] elements = line.split(":");
//					String fileName = elements[1];
//					int startLine = Integer.parseInt(elements[2]);
//					int endLine = Integer.parseInt(elements[3]);
//					String violationType = elements[4];
//					
//					Violation violation = new Violation(startLine, endLine, violationType);
//					violation.setFileName(fileName);
//					uselessViolations.add(violation);
//				}
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			try {
//				reader.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		return uselessViolations;
//	}

	private static List<MessageFile> getMessageFiles(String gumTreeInput) {
		String inputPath = gumTreeInput; // prevFiles  revFiles diffentryFile positionsFile
		File revFilesPath = new File(inputPath + "revFiles/");
		File[] revFiles = revFilesPath.listFiles();   // project folders
		List<MessageFile> msgFiles = new ArrayList<>();
		
		for (File revFile : revFiles) {
			if (revFile.getName().endsWith(".java")) {
				String fileName = revFile.getName();
				File prevFile = new File(gumTreeInput + "prevFiles/prev_" + fileName);// previous file
				fileName = fileName.replace(".java", ".txt");
				File diffentryFile = new File(gumTreeInput + "diffentries/" + fileName); // DiffEntry file
				File positionFile = new File(gumTreeInput + "positions/" + fileName); // position file
				MessageFile msgFile = new MessageFile(revFile, prevFile, diffentryFile);
				msgFile.setPositionFile(positionFile);
				msgFiles.add(msgFile);
			}
		}
		
		return msgFiles;
	}
}
