package edu.lu.uni.serval.buildtool.maven;

public class MavenBuildCommand
{

	public static void main(String[] args)
	{
		String baseDirPath = args[0];
		String mavenTempRepoDir = args[1];
		
		MavenExecutor.buildProject(baseDirPath, mavenTempRepoDir,
				false, // no test jar 
				true,  // skip test run
				false, // skip test compile
				false, // no fail-never action
				false // verbose
				);
	}

}
