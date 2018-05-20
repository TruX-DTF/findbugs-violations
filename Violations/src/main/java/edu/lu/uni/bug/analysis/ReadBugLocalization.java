package edu.lu.uni.bug.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.lu.uni.serval.utils.FileHelper;
import edu.lu.uni.serval.violations.info.ViolationInstance;

public class ReadBugLocalization {
	
	public List<ViolationInstance> readLocalization(File fileName) {
		List<ViolationInstance> bugs = new ArrayList<>();
		
		String content = FileHelper.readFile(fileName);
		BufferedReader reader = new BufferedReader(new StringReader(content));
		String line = null;
		try {
			String buggyFile = "";
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("--- ")) {
					buggyFile = line.substring(4, line.lastIndexOf(".java") + 5);
				} else if (line.startsWith("@@")) {
					int plusIndex = line.indexOf("+");
					String lineNum = line.substring(plusIndex) .trim();
					lineNum = lineNum.substring(1, lineNum.indexOf("@@"));
					String[] nums = lineNum.split(",");
					int startLine = Integer.parseInt(nums[0].trim());
					int range = 0;
					if (nums.length == 2) {
						range = Integer.parseInt(nums[1].trim());
					}
					int endLine = startLine + range - 1;
					
					if (startLine == 0 || endLine <= 0) {
						System.out.println();
						continue;
					}
					
					ViolationInstance bugInstance = new ViolationInstance();
					bugInstance.setSourcePath(buggyFile);
					bugInstance.setStartOfSourceLine(startLine);
					bugInstance.setEndOfSourceLine(endLine);
					bugs.add(bugInstance);
				}
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
		
		return bugs;
	}
	
	public boolean filterSignal(String string) {
		boolean flag = false;
		
		final String REGULAR_EXPRESSION = "^@@\\s\\-\\d+,*\\d*\\s\\+\\d+,*\\d*\\s@@$"; //@@ -21,0 +22,2 @@
		Pattern pattern = Pattern.compile(REGULAR_EXPRESSION);
		Matcher res = pattern.matcher(string);
		if (res.matches()) {
			flag = true;
		}
		
		return flag;
	}
}
