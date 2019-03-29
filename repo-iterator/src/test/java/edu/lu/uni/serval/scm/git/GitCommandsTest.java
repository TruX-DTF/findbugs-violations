package edu.lu.uni.serval.scm.git;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

public class GitCommandsTest
{

	@Test
	public void testCheckExists()
	{
		// This should fail;
		
		String owner = "the-domains";
		String repoName = "andrealowryphotography";
		
		if(GitCommands.checkExists(owner, repoName))
		{
			fail();
		}
	}
	
	@Test
	public void testCheckExists2()
	{
		// This should be successful;
		//http://github.com/haisum/rpcexample.git
		String owner = "haisum";
		String repoName = "rpcexample";
		
		if(!GitCommands.checkExists(owner, repoName))
		{
			fail();
		}
	}
	
	@Test
	public void testClone()
	{
		String url = "https://github.com/the-domains/andrealowryphotography.git";
		String path = "andrealowryphotography";
		String workingDir = "./tmp/the-domains";
		
		File dir = new File(workingDir);
		dir.mkdirs();
		
		if(GitCommands.cloneWithRetry(url, path, workingDir))
		{
			fail();
		}
	}
	
	@Test
	public void testClone2()
	{
		String url = "https://github.com/haisum/rpcexample.git";
		String path = "rpcexample";
		String workingDir = "./tmp/haisum";
		
		File dir = new File(workingDir);
		dir.mkdirs();
		
		if(!GitCommands.cloneWithRetry(url, path, workingDir))
		{
			fail();
		}
	}
}
