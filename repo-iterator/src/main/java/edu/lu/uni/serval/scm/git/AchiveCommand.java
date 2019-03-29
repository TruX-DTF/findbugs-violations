package edu.lu.uni.serval.scm.git;

public class AchiveCommand {

	public static void main(String[] args) 
	{
		String gitDir = args[0];
		String outputDir = args[1];
		String hash = args[2];
		
		GitCommands.archiveCommit(gitDir, outputDir, hash);
	}

}
