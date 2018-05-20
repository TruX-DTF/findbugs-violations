package edu.lu.uni.serval.mining.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import edu.lu.uni.serval.config.Configuration;
import edu.lu.uni.serval.utils.FileHelper;
import edu.lu.uni.serval.utils.ListSorter;

public class ClusterAnalyser {
	
	private List<Integer> clusterResults; // each element is a cluster number.
	
	public void readClusterResults() {
		clusterResults = DataPreparation.readClusterResults();
	}
	
	public void clusterBuggyCodeTokens() {
		String selectedTokens = Configuration.SELECTED_BUGGY_TOKEN_FILE;
		String clusteredTokens = Configuration.CLUSTERED_TOKENSS_FILE;
		
		FileInputStream fis = null;
		Scanner scanner = null;
		
		Map<Integer, StringBuilder> builderMap = new HashMap<>();
		Map<Integer, Integer> countersMap = new HashMap<>();
		try {
			fis = new FileInputStream(selectedTokens);
			scanner = new Scanner(fis);
			int index = 0;
			
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				int clusterNum = clusterResults.get(index);
				StringBuilder builder = getBuilder(builderMap, clusterNum);
				builder.append(line).append("\n");
				int counter = getCounter(countersMap, clusterNum);
				if (counter % 1000 == 0) {
					FileHelper.outputToFile(clusteredTokens + "Tokens_" + clusterNum + ".list", builder, true);
					builder.setLength(0);
					builderMap.put(clusterNum, builder);
				}
				index ++;
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
		
		for (Map.Entry<Integer, StringBuilder> entry : builderMap.entrySet()) {
			int clusterNum = entry.getKey();
			StringBuilder builder = entry.getValue();
			FileHelper.outputToFile(clusteredTokens + "Tokens_" + clusterNum + ".list", builder, true);
			builder.setLength(0);
		}
	}
	
	public void clusterPatchSourceCode() {
		String selectedPatches = Configuration.SELECTED_PATCHES_SOURE_CODE_FILE;
		String clusteredPatches = Configuration.CLUSTERED_PATCHES_FILE;
		String selectedAlarmTypes = Configuration.SELECTED_ALARM_TYPES_FILE;
		
		List<String> alarmTypes = DataPreparation.readStringList(selectedAlarmTypes);
		
		FileInputStream fis = null;
		Scanner scanner = null;
		
		Map<Integer, List<String>> clusterPatches = new HashMap<>();
		try {
			fis = new FileInputStream(selectedPatches);
			scanner = new Scanner(fis);
			String singlePatch = "";
			int index = -1;
			
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if ("".equals(line)) continue;
				if (line.startsWith(Configuration.PATCH_SIGNAL)) {
					if (!"".equals(singlePatch)) {
						int clusterNum = clusterResults.get(index);

						String alarmType = alarmTypes.get(index);
						List<String> patches = getList(clusterPatches, clusterNum);
						patches.add("Alarm Type: " + alarmType + "\n" + singlePatch);
					}
					singlePatch = "";
					index ++;
				}
				singlePatch += line + "\n";
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
		
		for (Map.Entry<Integer, List<String>> entry : clusterPatches.entrySet()) {
			int clusterNum = entry.getKey();
			List<String> list = entry.getValue();
			ListSorter<String> sorter = new ListSorter<String>(list);
			list = sorter.sortAscending();
			StringBuilder builder = new StringBuilder();
			for (String str : list) {
				builder.append(str + "\n");
			}
			FileHelper.outputToFile(clusteredPatches + "PatchesCluster_" + clusterNum + ".txt", builder, true);
		}
	}

	private List<String> getList(Map<Integer, List<String>> map, int clusterNum) {
		if (map.containsKey(clusterNum)) {
			return map.get(clusterNum);
		} else {
			List<String> list = new ArrayList<>();
			map.put(clusterNum, list);
			return list;
		}
	}

	private int getCounter(Map<Integer, Integer> countersMap, int clusterNum) {
		int counter = 1;
		if (countersMap.containsKey(clusterNum)) {
			counter += countersMap.get(clusterNum);
		}
		countersMap.put(clusterNum, counter);
		return counter;
	}

	private StringBuilder getBuilder(Map<Integer, StringBuilder> builderMap, int clusterNum) {
		if (builderMap.containsKey(clusterNum)) {
			return builderMap.get(clusterNum);
		} else {
			StringBuilder builder = new StringBuilder();
			builderMap.put(clusterNum, builder);
			return builder;
		}
	}

	public List<Integer> getClusterResults() {
		return clusterResults;
	}
	
	public void readClusterResults(String clusterResultsFile) {
		clusterResults = new ArrayList<>();
		String results = FileHelper.readFile(clusterResultsFile);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new StringReader(results));
			String line = null;
			while ((line = reader.readLine()) != null) {
				clusterResults.add(Integer.parseInt(line));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void clusterBuggyCodeTokens(String tokensFile, String outputFile) {
		String clusteredTokens = outputFile + "ClusteredTokens/";
		
		FileInputStream fis = null;
		Scanner scanner = null;
		
		Map<Integer, StringBuilder> builderMap = new HashMap<>();
		Map<Integer, Integer> countersMap = new HashMap<>();
		try {
			fis = new FileInputStream(tokensFile);
			scanner = new Scanner(fis);
			int index = 0;
			
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (index >= clusterResults.size()) break;
				int clusterNum = clusterResults.get(index);
				StringBuilder builder = getBuilder(builderMap, clusterNum);
				builder.append(line).append("\n");
				int counter = getCounter(countersMap, clusterNum);
				if (counter % 1000 == 0) {
					FileHelper.outputToFile(clusteredTokens + "Tokens_" + clusterNum + ".list", builder, true);
					builder.setLength(0);
					builderMap.put(clusterNum, builder);
				}
				index ++;
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
		
		for (Map.Entry<Integer, StringBuilder> entry : builderMap.entrySet()) {
			int clusterNum = entry.getKey();
			StringBuilder builder = entry.getValue();
			FileHelper.outputToFile(clusteredTokens + "Tokens_" + clusterNum + ".list", builder, true);
			builder.setLength(0);
		}
	}

	public void clusterPatchSourceCode(String patchFile, String outputFile, String signal) {
		
		FileInputStream fis = null;
		Scanner scanner = null;
		int size = clusterResults.size();
		
		Map<Integer, List<String>> clusterPatches = new HashMap<>();
		try {
			fis = new FileInputStream(patchFile);
			scanner = new Scanner(fis);
			String singlePatch = "";
			int index = -1;
			
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
//				if ("".equals(line)) continue;
				if (line.startsWith(signal)) {
					if (index >= size) break;
					if (index > -1 && index < size) {
						int clusterNum = clusterResults.get(index);
						List<String> patches = getList(clusterPatches, clusterNum);
						patches.add(singlePatch);
					}
					singlePatch = "";
					index ++;
				}
				singlePatch += line + "\n";
			}
			
			if (index < size) {
				int clusterNum = clusterResults.get(index);
				List<String> patches = getList(clusterPatches, clusterNum);
				patches.add(singlePatch);
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
		
		for (Map.Entry<Integer, List<String>> entry : clusterPatches.entrySet()) {
			int clusterNum = entry.getKey();
			List<String> list = entry.getValue();
			StringBuilder builder = new StringBuilder();
			for (String str : list) {
				builder.append(str + "\n");
			}
			FileHelper.outputToFile(outputFile + "Cluster_" + clusterNum + "_Quantity_" + list.size() + ".txt", builder, true);
		}
		System.out.println(outputFile + ": " + clusterPatches.size());
	}

	public void clusterBuggyCodeFeatures(String featuresFile, String outputFile) {
		String clusteredTokens = outputFile + "ClusteredFeatures/";
		
		FileInputStream fis = null;
		Scanner scanner = null;
		
		Map<Integer, StringBuilder> builderMap = new HashMap<>();
		Map<Integer, Integer> countersMap = new HashMap<>();
		try {
			fis = new FileInputStream(featuresFile);
			scanner = new Scanner(fis);
			int index = 0;
			
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (index >= clusterResults.size()) break;
				int clusterNum = clusterResults.get(index);
				StringBuilder builder = getBuilder(builderMap, clusterNum);
				builder.append(line).append("\n");
				int counter = getCounter(countersMap, clusterNum);
				if (counter % 1000 == 0) {
					FileHelper.outputToFile(clusteredTokens + "Features_" + clusterNum + ".list", builder, true);
					builder.setLength(0);
					builderMap.put(clusterNum, builder);
				}
				index ++;
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
		
		for (Map.Entry<Integer, StringBuilder> entry : builderMap.entrySet()) {
			int clusterNum = entry.getKey();
			StringBuilder builder = entry.getValue();
			FileHelper.outputToFile(clusteredTokens + "Features_" + clusterNum + ".list", builder, true);
			builder.setLength(0);
		}
	}
}
