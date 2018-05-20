package edu.lu.uni.serval.violation.code.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.lu.uni.serval.config.Configuration;
import edu.lu.uni.serval.main.violations.MessageFile;
import edu.lu.uni.serval.utils.FileHelper;

/**
 * Statistics of violations' positions: code entities.
 * 
 * Class name part,
 * Field declaration,
 * Initializer,
 * Method name,
 * Method body,
 * Inner class,
 * Anonymous class.
 * 
 * @author kui.liu
 *
 */
public class ViolatePositionReader {
	
	public static void main(String[] args) throws NumberFormatException, IOException {
		List<MessageFile> msgFiles = getMessageFiles(Configuration.GUM_TREE_INPUT);
		
		Map<Integer, Integer> map1 = new HashMap<>();
		Map<String, Map<Integer, Integer>> map2 = new HashMap<>();
		
		StringBuilder zeroBuilder = new StringBuilder();
		StringBuilder minusOneBuilder = new StringBuilder();
		for (MessageFile file : msgFiles) {
			
//			if (!file.getPrevFile().getName().equals(
//				"prev_ovgu-ccd-jchess_c3f4a0_bdca7bjchess#util#PixelCoordinateNotOnBoardException.java")) continue;
			
			String fileContent = FileHelper.readFile(file.getPositionFile());
			BufferedReader reader = null;
			reader = new BufferedReader(new StringReader(fileContent));
			String line = null;
			
			while ((line = reader.readLine()) != null) {
				String[] positionStr = line.split(":");
				int startLine = Integer.parseInt(positionStr[1]);
				int endLine = Integer.parseInt(positionStr[2]);
				String violationType = positionStr[0];
				
				int range;
				if (startLine != -1) {
					ViolationSourceCodeTree violationTree = new ViolationSourceCodeTree(file.getPrevFile(), startLine, endLine);
					violationTree.locateParentNode();
					range = violationTree.getViolationFinalStartLine();
				} else {
					range = 0;
				}

				if (map1.containsKey(range)) {
					map1.put(range, map1.get(range) + 1);
				} else {
					map1.put(range, 1);
				}
				
				if (map2.containsKey(violationType)) {
					Map<Integer, Integer> map = map2.get(violationType);
					if (map.containsKey(range)) {
						map.put(range, map.get(range) + 1);
					} else {
						map.put(range, 1);
					}
				} else {
					Map<Integer, Integer> map = new HashMap<>();
					map.put(range, 1);
					map2.put(violationType, map);
				}
				
				if (range == 0) {
					zeroBuilder.append(line + ":" + file.getPrevFile().getName() + "\n");
				} else if (range == -1) {
					minusOneBuilder.append(line + ":" + file.getPrevFile().getName() + "\n");					
				}
			}
			
			reader.close();
		}
		for (Map.Entry<Integer, Integer> entry : map1.entrySet()) {
			System.out.println(entry.getKey() + " : " + entry.getValue());
		}
		FileHelper.outputToFile("Dataset/0.list", zeroBuilder, false);
		FileHelper.outputToFile("Dataset/1.list", minusOneBuilder, false);
	}

	/**
	 * Get violation-related files.
	 * 
	 * @param gumTreeInput
	 * @return
	 */
	public static List<MessageFile> getMessageFiles(String gumTreeInput) {
		String inputPath = gumTreeInput; // prevFiles  revFiles diffentryFile positionsFile
		File revFilesPath = new File(inputPath + "revFiles/");
		File[] revFiles = revFilesPath.listFiles();   // project folders
		List<MessageFile> msgFiles = new ArrayList<>();
		
		for (File revFile : revFiles) {
			if (revFile.getName().endsWith(".java")) {
				String fileName = revFile.getName();
				File prevFile = new File(gumTreeInput + "prevFiles/prev_" + fileName);// previous file
				fileName = fileName.replace(".java", ".txt");
				File positionFile = new File(gumTreeInput + "positions/" + fileName); // position file
				MessageFile msgFile = new MessageFile(null, prevFile, null);
				msgFile.setPositionFile(positionFile);
				msgFiles.add(msgFile);
			}
		}
		
		return msgFiles;
	}
}
