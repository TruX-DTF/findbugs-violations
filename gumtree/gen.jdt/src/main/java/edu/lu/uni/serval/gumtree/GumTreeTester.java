package edu.lu.uni.serval.gumtree;

import java.util.List;

import com.github.gumtreediff.actions.model.Action;

public class GumTreeTester {
	
	public static void main(String[] args) {
		String oldCodeBlock = "final TimeZone tz = TimeZone.getTimeZone(value.toUpperCase());";
		String newCodeBlock = "final TimeZone tz = TimeZone.getTimeZone(value.toUpperCase(Locale.ROOT));";
		List<Action> gumTreeResults = new GumTreeComparer().compareTwoCodeBlocksWithGumTree(oldCodeBlock, newCodeBlock);
		System.out.println(gumTreeResults);
//		List<Action> gumTreeResults = new GumTreeComparer().compareTwoFilesWithGumTree(new File("logs/a.java"), new File("logs/b.java"));
//		System.out.println(gumTreeResults);
	}

}
