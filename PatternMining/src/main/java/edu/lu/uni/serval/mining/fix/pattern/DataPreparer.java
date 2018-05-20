package edu.lu.uni.serval.mining.fix.pattern;

import java.io.File;
import java.util.List;

import edu.lu.uni.serval.config.Configuration;
import edu.lu.uni.serval.mining.utils.DataPreparation;
import edu.lu.uni.serval.utils.FileHelper;

/**
 * Prepare data for tokens embedding of edit scripts.
 * 
 * Input data: parsed results of patches with GumTree.
 * 
 * @author kui.liu
 *
 */
public class DataPreparer {
	
	public void prepare() {
		boolean isViolations = false;
		if (isViolations) {
			// classify data by alarm types.
//			new Classifier().classifyDataByAlarmTypes();
			File file = new File(Configuration.BUGGY_CODE_TOKEN_FILE_PATH);
			File[] subFiles = file.listFiles();
			
			for (File subFile : subFiles) {
				String fileName = subFile.getName(); // edistScripts file
				@SuppressWarnings("unused")
				String id = fileName.substring(fileName.lastIndexOf("_"));
				FileHelper.outputToFile(Configuration.GUM_TREE_INPUT + "DEL_INS.list", FileHelper.readFile(subFile), true);
			}
			
		} else {
			boolean usedMultipleThreads = true;
			// for multiple threads
			if (usedMultipleThreads) {
				String editScriptsFile = Configuration.EDITSCRIPTS_FILE;
				String patchesSourceCodeFile = Configuration.PATCH_SOURCECODE_FILE;
				String buggyTokensFile = Configuration.BUGGY_CODE_TOKENS_FILE;
				String editScriptSizesFile = Configuration.EDITSCRIPT_SIZES_FILE;
				String alarmTypesFile = Configuration.ALARM_TYPES_FILE;
				FileHelper.deleteFile(editScriptsFile);
				FileHelper.deleteFile(patchesSourceCodeFile);
				FileHelper.deleteFile(buggyTokensFile);
				FileHelper.deleteFile(editScriptSizesFile);
				FileHelper.deleteFile(alarmTypesFile);
				DataPreparation.prepareDataForTokenEmbedding();
			}
			
			String selectedEditScripts = Configuration.SELECTED_EDITSCRIPTES_FILE;
			String selectedPatches = Configuration.SELECTED_PATCHES_SOURE_CODE_FILE;
			String selectedBuggyTokens = Configuration.SELECTED_BUGGY_TOKEN_FILE;
			String selectedAlarmTypes = Configuration.SELECTED_ALARM_TYPES_FILE;
			FileHelper.deleteFile(selectedEditScripts);
			FileHelper.deleteFile(selectedPatches);
			FileHelper.deleteFile(selectedBuggyTokens);
			FileHelper.deleteFile(selectedAlarmTypes);
//			
			DataPreparation.dataSelection();
//			dataSelection();
		}
	}
	
	public void dataSelection() {

		String s = Configuration.GUM_TREE_OUTPUT;
		File file = new File(s);
		File[] files = file.listFiles();
		for (File f : files) {
			if (f.isDirectory()) {
				List<File> listFiles = FileHelper.getAllFilesInCurrentDiectory(f, ".list");
				for (File ff : listFiles) {
					FileHelper.outputToFile(s + ff.getName(), FileHelper.readFile(ff), true);
				}
				
			}
		}
	}
}
