package edu.lu.uni.serval.fix.patterns.matching2;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import edu.lu.uni.serval.config.Configuration;
import edu.lu.uni.serval.mining.utils.DataPreparation;
import edu.lu.uni.serval.utils.FileHelper;

/**
 * Find the path of each project.
 * 
 * @author kui.liu
 *
 */
public class PathFinder {

	public static void main(String[] args) throws IOException {
		List<Integer> unfixedViolationIndexes = readIndexes(Configuration.ROOT_PATH + "Violations_tokens/selectedData/unfixedViolationsTokensIndexes.csv");
		
		List<String> projects = new ArrayList<>();
		FileInputStream fis = new FileInputStream(Configuration.ROOT_PATH + "Violations_tokens/unfixedViolations.list");
		Scanner scanner = new Scanner(fis);
		int index = -1;
		int counter = 0;
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			index ++;
			
			if (unfixedViolationIndexes.contains(index)) {
				String[] elements = line.split(":");
				String info = elements[1]; //xetorthio-jedis_ef1077_src#main#java#redis#clients#jedis#Jedis.txt
				info = info.substring(0, info.indexOf("#")); //xetorthio-jedis_ef1077_src
				info = info.substring(0, info.lastIndexOf("_")); //xetorthio-jedis_ef1077
				String projectName = info.substring(0, info.lastIndexOf("_")); //xetorthio-jedis
				if (!projects.contains(projectName)) projects.add(projectName);
					
				counter ++;
				if (counter == 65000)  break;
			}
		}
		
		scanner.close();
		fis.close();
		
		Map<String, String> maps = new HashMap<>();
		List<String> pathes = DataPreparation.readStringList("Dataset/ProjectPath.list");
		for (String path : pathes) {
			String[] elements = path.split(":");
			maps.put(elements[0], elements[1]);
		}
		
		pathes.clear();
		for (String project : projects) {
			if (maps.containsKey(project)) {
				String path = maps.get(project);
				if (!pathes.contains(path)) pathes.add(path);
			}
		}
		
		System.out.println(pathes);	
	}

	
	private static List<Integer> readIndexes(String fileName) throws IOException {
		List<Integer> indexes = new ArrayList<>();
		String content = FileHelper.readFile(fileName);
		BufferedReader reader = new BufferedReader(new StringReader(content));
		String line = null;
		int counter = 0;
		while ((line = reader.readLine()) != null) {
			indexes.add(Integer.parseInt(line));
			
			if (++ counter == 65000) break;
		}
		reader.close();
		return indexes;
	}
}
