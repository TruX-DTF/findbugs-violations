package edu.lu.uni.serval.cluster;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import edu.lu.uni.serval.utils.FileHelper;

public class DataPreparer {
	
	/**
	 * 
	 * @param inputFileName
	 * @param outputFileName
	 */
	public static void prepareData(String inputFileName, String outputFileName) {
		File inputFile = new File(inputFileName);
		prepareData(inputFile, outputFileName);
	}
	
	public static void prepareData(File inputFile, String outputFileName) {
		File outputFile = new File(outputFileName);
		if (outputFile.exists()) outputFile.delete();
		
		FileInputStream fis = null;
		Scanner scanner = null;
		
		try {
			fis = new FileInputStream(inputFile);
			scanner = new Scanner(fis);
			
			StringBuilder builder = new StringBuilder();
			builder.append("@relation Data_for_clustering\n\n");
			
			int counter = 0;
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				
				if(counter == 0) {
					String[] strArr = line.split(", ");
					for (int i = 0; i < strArr.length; i ++) {
						builder.append("@attribute attribute_" + i + " numeric\n");
					}
					builder.append("\n@data\n");
				}
				
				builder.append(line.replaceAll(", ", ",") + "\n");
				counter ++;
				if (counter % 100000 == 0) {
					FileHelper.outputToFile(outputFile, builder, true);
					builder.setLength(0);
				}
			}
			
			FileHelper.outputToFile(outputFile, builder, true);
			builder.setLength(0);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (scanner != null) {
					scanner.close();
					scanner = null;
				}
				if (fis != null) {
					fis.close();
					fis = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
	}
	
	
	public static void main(String[] args) {
		
		List<File> inputFiles = FileHelper.getAllFilesInCurrentDiectory("../../OUTPUT/AllReturnTypes/features/", ".csv");
		String outputPath = "../../OUTPUT/MyCluster/input/";
		for (File inputFile : inputFiles) {
			prepareData(inputFile, outputPath);
		}
	}
}
